#!/usr/bin/env bash
set -euo pipefail

# Lightweight, cross-platform oriented preflight checks for Kipita Android builds.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[Kipita] Environment preflight"

echo "- OS: $(uname -s)"

echo "- Java:"
java -version 2>&1 | head -n 2 || true

echo "- Gradle:"
gradle -v | head -n 20 || true

# Check merge conflict markers in core files.
echo "- Checking conflict markers..."
if rg -n "<<<<<<<|=======|>>>>>>>" README.md app build.gradle.kts settings.gradle.kts gradle/libs.versions.toml .gitignore >/tmp/kipita_conflicts.txt 2>/dev/null; then
  echo "  ERROR: conflict markers found"
  cat /tmp/kipita_conflicts.txt
  exit 2
else
  echo "  OK: no conflict markers in key paths"
fi

# Check Firebase files layout.
echo "- Checking Firebase file layout..."
for flavor in prod dev staging; do
  if [[ -f "app/src/${flavor}/google-services.json" ]]; then
    echo "  OK: app/src/${flavor}/google-services.json present"
  elif [[ -f "app/src/${flavor}/google-services.json.template" ]]; then
    echo "  WARN: app/src/${flavor}/google-services.json missing (template present)"
  else
    echo "  WARN: app/src/${flavor}/google-services.json and template missing"
  fi
done

# Check required package names for real files if present.
check_pkg() {
  local file="$1"
  local pkg="$2"
  if [[ -f "$file" ]]; then
    if rg -q '"package_name"\s*:\s*"'"$pkg"'"' "$file"; then
      echo "  OK: $file package_name=$pkg"
    else
      echo "  WARN: $file package_name does not match expected $pkg"
    fi
  fi
}

check_pkg "app/src/prod/google-services.json" "com.mytum"
check_pkg "app/src/dev/google-services.json" "com.mytum.dev"
check_pkg "app/src/staging/google-services.json" "com.mytum.staging"

# AGP resolution attempt (often blocked in restricted environments).
echo "- Running Gradle smoke check (tasks)..."
if [[ -d "/root/.local/share/mise/installs/java/21.0.2" ]]; then
  export JAVA_HOME=/root/.local/share/mise/installs/java/21.0.2
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if gradle tasks >/tmp/kipita_gradle_tasks.log 2>&1; then
  echo "  OK: gradle tasks"
else
  echo "  WARN: gradle tasks failed (likely network/plugin resolution). See /tmp/kipita_gradle_tasks.log"
  tail -n 25 /tmp/kipita_gradle_tasks.log || true
fi

echo "[Kipita] Preflight complete"
