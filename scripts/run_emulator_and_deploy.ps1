param(
    [string[]]$PreferredAvds = @("Pixel_9_Pro", "Medium_Phone"),
    [switch]$PrepareOnly,
    [switch]$DeployOnly
)

$ErrorActionPreference = "Stop"

function Read-SdkDirFromLocalProperties {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return $null }
    $line = Get-Content $Path | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
    if (-not $line) { return $null }
    $raw = $line.Substring("sdk.dir=".Length)
    $normalized = $raw -replace '\\\\', '\'
    $normalized = $normalized -replace '^([A-Za-z])\\:', '$1:'
    return $normalized
}

function Ensure-LocalAvd {
    param(
        [string]$AvdName,
        [string]$SourceRoot,
        [string]$TargetRoot
    )

    $srcIni = Join-Path $SourceRoot "$AvdName.ini"
    $srcDir = Join-Path $SourceRoot "$AvdName.avd"
    if (-not (Test-Path $srcIni) -or -not (Test-Path $srcDir)) {
        return $false
    }

    $dstIni = Join-Path $TargetRoot "$AvdName.ini"
    $dstDir = Join-Path $TargetRoot "$AvdName.avd"

    if (-not (Test-Path $dstDir)) {
        New-Item -ItemType Directory -Path $dstDir -Force | Out-Null
        robocopy $srcDir $dstDir /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 | Out-Null
    }

    $iniLines = @(
        "avd.ini.encoding=UTF-8",
        "path=$dstDir",
        "path.rel=avd\$AvdName.avd",
        "target=android-36"
    )
    Set-Content -Path $dstIni -Value $iniLines -Encoding ASCII
    return $true
}

function Find-SourceAvdName {
    param(
        [string]$Hint,
        [string]$SourceRoot
    )
    $ini = Join-Path $SourceRoot "$Hint.ini"
    if (Test-Path $ini) { return $Hint }

    $all = Get-ChildItem $SourceRoot -Filter "*.ini" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty BaseName
    $match = $all | Where-Object { $_ -like "*$Hint*" } | Select-Object -First 1
    return $match
}

$projectRoot = (Resolve-Path "$PSScriptRoot\..").Path
$sdkDir = Read-SdkDirFromLocalProperties -Path (Join-Path $projectRoot "local.properties")
if (-not $sdkDir) {
    $sdkDir = "$env:LOCALAPPDATA\Android\Sdk"
}

$emulatorExe = Join-Path $sdkDir "emulator\emulator.exe"
$adbExe = Join-Path $sdkDir "platform-tools\adb.exe"
if (-not (Test-Path $emulatorExe)) { throw "Missing emulator executable: $emulatorExe" }
if (-not (Test-Path $adbExe)) { throw "Missing adb executable: $adbExe" }

$localAndroidRoot = Join-Path $projectRoot ".codex_android"
$localUserHome = Join-Path $localAndroidRoot "home"
$localAvdHome = Join-Path $localAndroidRoot "avd"
New-Item -ItemType Directory -Path $localUserHome -Force | Out-Null
New-Item -ItemType Directory -Path $localAvdHome -Force | Out-Null

$env:ANDROID_USER_HOME = $localUserHome
$env:ANDROID_AVD_HOME = $localAvdHome
$env:ANDROID_EMULATOR_HOME = $localUserHome
$env:ANDROID_SDK_ROOT = $sdkDir

$sourceAvdRoot = Join-Path $env:USERPROFILE ".android\avd"
$availableAvds = @()
foreach ($hint in $PreferredAvds) {
    $sourceName = Find-SourceAvdName -Hint $hint -SourceRoot $sourceAvdRoot
    if (-not $sourceName) { continue }

    $synced = Ensure-LocalAvd -AvdName $sourceName -SourceRoot $sourceAvdRoot -TargetRoot $localAvdHome
    if ($synced) { $availableAvds += $sourceName }
}

if ($availableAvds.Count -eq 0) {
    throw "None of the requested AVDs were found under $sourceAvdRoot. Requested: $($PreferredAvds -join ', ')"
}
$primaryAvd = $availableAvds[0]

Write-Host "Prepared local Android home:"
Write-Host "  ANDROID_USER_HOME=$env:ANDROID_USER_HOME"
Write-Host "  ANDROID_AVD_HOME=$env:ANDROID_AVD_HOME"
Write-Host "  AVDs=$($availableAvds -join ', ')"

if ($PrepareOnly) {
    Write-Host "PrepareOnly enabled. Skipping emulator boot and deploy."
    exit 0
}

if (-not $DeployOnly) {
    Write-Host ""
    Write-Host "Next step 1: launch emulator in this terminal (foreground):"
    Write-Host "& `"$emulatorExe`" -avd $primaryAvd -no-window -no-snapshot -wipe-data -gpu swiftshader_indirect -accel off -no-boot-anim -no-metrics"
    Write-Host ""
    Write-Host "Next step 2: in another terminal, deploy to connected emulator:"
    Write-Host "powershell -ExecutionPolicy Bypass -File scripts\\run_emulator_and_deploy.ps1 -DeployOnly"
    exit 0
}

& $adbExe start-server | Out-Null

$bootComplete = "0"
for ($i = 0; $i -lt 300; $i++) {
    Start-Sleep -Seconds 2
    $lines = & $adbExe devices 2>$null
    $deviceReady = ($lines | Where-Object { $_ -match '^emulator-\d+\s+device$' } | Select-Object -First 1)
    if (-not $deviceReady) { continue }
    try {
        $bootComplete = (& $adbExe shell getprop sys.boot_completed 2>$null).Trim()
    } catch {
        $bootComplete = "0"
    }
    if ($bootComplete -eq "1") { break }
}

if ($bootComplete -ne "1") {
    throw "No booted emulator found. Start emulator first, then rerun with -DeployOnly."
}

foreach ($pkg in @("com.mytum", "com.mytum.dev", "com.mytum.staging")) {
    try { & $adbExe uninstall $pkg | Out-Null } catch { }
}

Push-Location $projectRoot
try {
    & ".\gradlew.bat" ":app:installDevDebug"
} finally {
    Pop-Location
}

Write-Host "Deploy complete."
