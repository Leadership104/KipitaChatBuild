param(
    [string]$OutFile = "docs/github-note-2026-03-08-live-api-check.md"
)

function Safe-InvokeWeb {
    param([string]$Name, [scriptblock]$Action)
    try {
        $result = & $Action
        return [pscustomobject]@{
            Name = $Name
            Ok = $true
            Detail = $result
        }
    } catch {
        return [pscustomobject]@{
            Name = $Name
            Ok = $false
            Detail = $_.Exception.Message
        }
    }
}

$ErrorActionPreference = "Stop"
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
} catch {}

$checks = @()

$checks += Safe-InvokeWeb "api.dwaat.com root" {
    $r = Invoke-WebRequest -UseBasicParsing -Uri "https://api.dwaat.com/"
    "HTTP $($r.StatusCode)"
}

$checks += Safe-InvokeWeb "affiliates.php GetAllList" {
    $body = @{ action = "GetAllList" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "https://api.dwaat.com/affiliates.php" -Method Post -ContentType "application/json" -Body $body
    if ($null -eq $r) { "Empty body" }
    elseif ($r.data) { "status=$($r.status); items=$($r.data.Count)" }
    else { "Response received (no data array field)" }
}

$checks += Safe-InvokeWeb "airport.json" {
    $r = Invoke-RestMethod -Uri "https://api.dwaat.com/airport.json" -Method Get
    if ($r -is [System.Array]) { "items=$($r.Count)" } else { "Response type=$($r.GetType().Name)" }
}

$postmanPath = "credentials/imports/Dawaat.postman_collection.json"
$postmanSummary = if (Test-Path $postmanPath) {
    try {
        $json = Get-Content -Raw $postmanPath | ConvertFrom-Json
        $varCount = if ($json.variable) { $json.variable.Count } else { 0 }
        "Postman collection loaded; top-level variables=$varCount"
    } catch {
        "Postman collection read failed: $($_.Exception.Message)"
    }
} else {
    "Postman collection missing at $postmanPath"
}

$localKeys = @("GOOGLE_PLACES_API_KEY","MAPS_API_KEY","OPENAI_API_KEY","CLAUDE_API_KEY","GEMINI_API_KEY")
$localPropsPath = "local.properties"
$localLines = if (Test-Path $localPropsPath) { Get-Content $localPropsPath } else { @() }
$keyState = foreach ($k in $localKeys) {
    $exists = $localLines | Where-Object { $_ -match "^\s*$k=" } | Select-Object -First 1
    if ($exists) { "$k=SET" } else { "$k=MISSING" }
}

$lines = @()
$lines += "# GitHub Note - 2026-03-08 Live API Check"
$lines += ""
$lines += "## Dwaat endpoint probes"
foreach ($c in $checks) {
    $status = if ($c.Ok) { "OK" } else { "ERR" }
    $lines += "- $($c.Name): $status ($($c.Detail))"
}
$lines += ""
$lines += "## Private import status"
$lines += "- $postmanSummary"
$lines += "- local.properties key status:"
foreach ($k in $keyState) { $lines += "  - $k" }
$lines += ""
$lines += "## Dwaat URL config"
$baseLine = $localLines | Where-Object { $_ -match "^DWAAT_BASE_URL=" } | Select-Object -First 1
$fallbackLine = $localLines | Where-Object { $_ -match "^DWAAT_FALLBACK_BASE_URL=" } | Select-Object -First 1
if ($baseLine) { $lines += "- DWAAT_BASE_URL is configured" } else { $lines += "- DWAAT_BASE_URL missing (default BuildConfig used)" }
if ($fallbackLine) { $lines += "- DWAAT_FALLBACK_BASE_URL is configured" } else { $lines += "- DWAAT_FALLBACK_BASE_URL missing (default BuildConfig used)" }
$lines += ""
$lines += "## Notes"
$lines += "- Key values are intentionally not printed to keep secrets private."
$lines += "- App runtime can only use live APIs whose keys are present in local.properties/keystore."

Set-Content -Path $OutFile -Value $lines -Encoding UTF8
Write-Output "Wrote $OutFile"
