#!/usr/bin/env bash
set -euo pipefail

MANIFEST="app/src/main/AndroidManifest.xml"
GRADLE="app/build.gradle.kts"

fail() { echo "[FAIL] $1"; exit 1; }
pass() { echo "[PASS] $1"; }
warn() { echo "[WARN] $1"; }

[[ -f "$MANIFEST" ]] || fail "Missing $MANIFEST"
[[ -f "$GRADLE" ]] || fail "Missing $GRADLE"

grep -q 'android.permission.INTERNET' "$MANIFEST" && pass "INTERNET permission declared" || fail "INTERNET permission missing"
grep -q 'android:allowBackup="false"' "$MANIFEST" && pass "allowBackup disabled" || fail "allowBackup should be false"
grep -q 'android:name=".MainActivity"' "$MANIFEST" && pass "MainActivity declared" || fail "MainActivity missing"
grep -q 'android:name=".MainActivity"' "$MANIFEST" && grep -q 'android:exported="true"' "$MANIFEST" && pass "Launcher activity exported explicitly" || fail "Launcher exported flag missing"

grep -q 'versionCode\s*=\s*[0-9]\+' "$GRADLE" && pass "versionCode present" || fail "versionCode missing"
grep -q 'versionName\s*=\s*"[0-9]\+\.[0-9]\+\.[0-9]\+"' "$GRADLE" && pass "versionName uses semver" || warn "versionName is not semver"

if grep -q 'signingConfigs' "$GRADLE"; then
  pass "Signing config block detected"
else
  warn "No explicit signingConfigs block in app/build.gradle.kts (required before Play upload)"
fi

if grep -q 'isMinifyEnabled = true' "$GRADLE"; then
  pass "Release minification enabled"
else
  warn "Release minification disabled; recommended to enable before production release"
fi

if grep -q 'org.gradle.java.home' gradle.properties; then
  fail "Machine-specific org.gradle.java.home still present"
else
  pass "No machine-specific org.gradle.java.home override"
fi

pass "Play Store preflight checks completed"
