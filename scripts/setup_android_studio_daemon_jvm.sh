#!/usr/bin/env bash
set -euo pipefail

TARGET="gradle/gradle-daemon-jvm.properties"

cat > "$TARGET" <<'PROP'
# Local Android Studio override (generated).
# Keep local-only; file is gitignored.
toolchainVersion=21
PROP

echo "Wrote $TARGET"
echo "If Android Studio still uses another JDK, set Gradle JDK in IDE settings."
