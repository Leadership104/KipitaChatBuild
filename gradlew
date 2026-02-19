#!/bin/sh
set -eu

# Lightweight text-only Gradle launcher for environments where committing
# binary wrapper artifacts is not allowed.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "ERROR: 'gradle' command not found in PATH." >&2
echo "Install Gradle 9.2.1 (as set in gradle/wrapper/gradle-wrapper.properties) and retry." >&2
exit 1
