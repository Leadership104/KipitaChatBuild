param(
    [string]$CollectionPath = "credentials/imports/Dawaat.postman_collection.json",
    [string]$LocalPropsPath = "local.properties"
)

if (!(Test-Path $CollectionPath)) {
    Write-Error "Collection not found: $CollectionPath"
    exit 1
}

$json = Get-Content -Raw -Path $CollectionPath | ConvertFrom-Json
$vars = @{}

if ($json.variable) {
    foreach ($v in $json.variable) {
        if ($v.key -and $v.value) {
            $vars[$v.key.ToString()] = $v.value.ToString()
        }
    }
}

$map = @{
    "GOOGLE_PLACES_API_KEY" = @("GOOGLE_PLACES_API_KEY", "google_places_api_key", "places_api_key")
    "MAPS_API_KEY"          = @("MAPS_API_KEY", "maps_api_key", "google_maps_api_key")
    "OPENAI_API_KEY"        = @("OPENAI_API_KEY", "openai_api_key")
    "CLAUDE_API_KEY"        = @("CLAUDE_API_KEY", "anthropic_api_key", "claude_api_key")
    "GEMINI_API_KEY"        = @("GEMINI_API_KEY", "gemini_api_key")
}

$existing = @{}
if (Test-Path $LocalPropsPath) {
    Get-Content $LocalPropsPath | ForEach-Object {
        if ($_ -match "^\s*([^#=\s]+)\s*=\s*(.*)\s*$") {
            $existing[$matches[1]] = $matches[2]
        }
    }
}

foreach ($target in $map.Keys) {
    foreach ($candidate in $map[$target]) {
        if ($vars.ContainsKey($candidate) -and [string]::IsNullOrWhiteSpace($vars[$candidate]) -eq $false) {
            $existing[$target] = $vars[$candidate]
            break
        }
    }
}

$orderedKeys = $existing.Keys | Sort-Object
$lines = @()
foreach ($k in $orderedKeys) {
    $lines += "$k=$($existing[$k])"
}

Set-Content -Path $LocalPropsPath -Value $lines -Encoding UTF8
Write-Output "Updated $LocalPropsPath from Postman variables (values not printed for safety)."
