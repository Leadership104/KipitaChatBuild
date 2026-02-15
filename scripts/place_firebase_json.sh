#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   scripts/place_firebase_json.sh /path/to/prod.json /path/to/dev.json /path/to/staging.json
#
# Copies files to:
#   app/src/prod/google-services.json
#   app/src/dev/google-services.json
#   app/src/staging/google-services.json

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" || $# -ne 3 ]]; then
  echo "Usage: $0 <prod_google_services_json> <dev_google_services_json> <staging_google_services_json>"
  exit $([[ $# -eq 3 ]] && echo 0 || echo 1)
fi

PROD_SRC="$1"
DEV_SRC="$2"
STAGING_SRC="$3"

for file in "$PROD_SRC" "$DEV_SRC" "$STAGING_SRC"; do
  if [[ ! -f "$file" ]]; then
    echo "Error: file not found: $file" >&2
    exit 2
  fi
  if ! rg -q '"package_name"' "$file"; then
    echo "Error: missing package_name in $file" >&2
    exit 3
  fi
done

mkdir -p app/src/prod app/src/dev app/src/staging
cp "$PROD_SRC" app/src/prod/google-services.json
cp "$DEV_SRC" app/src/dev/google-services.json
cp "$STAGING_SRC" app/src/staging/google-services.json

if ! rg -q '"package_name"\s*:\s*"com\.mytum"' app/src/prod/google-services.json; then
  echo "Warning: prod package_name is not com.mytum" >&2
fi
if ! rg -q '"package_name"\s*:\s*"com\.mytum\.dev"' app/src/dev/google-services.json; then
  echo "Warning: dev package_name is not com.mytum.dev" >&2
fi
if ! rg -q '"package_name"\s*:\s*"com\.mytum\.staging"' app/src/staging/google-services.json; then
  echo "Warning: staging package_name is not com.mytum.staging" >&2
fi

echo "Placed Firebase configs into app/src/{prod,dev,staging}/google-services.json"
