# Android Studio / Emulator (AVD) readiness

This project is configured to be portable by default (no machine-specific JDK path in source control).

## Optional: pin daemon JVM locally for Android Studio only

If your local Android Studio setup requires a specific JDK, create a **local-only** file:

- `gradle/gradle-daemon-jvm.properties`

Use the template below and do **not** commit this file. It is gitignored.

See: `gradle/gradle-daemon-jvm.properties.android-studio.example`

## AVD sanity test checklist (run locally)

1. Sync project in Android Studio.
2. Run `devDebug` on an emulator (API 34+ recommended).
3. Verify flows:
   - Home -> Map -> AI prompt
   - Trips -> Trip detail -> invite
   - Social/Groups screens load
4. If Firebase config is not present, verify app still builds/runs (Google Services plugins are conditionally applied).

## Commands

```bash
./scripts/playstore_preflight.sh
./gradlew :app:assembleDevDebug
./gradlew :app:lint
```


## Firebase-enabled local build (optional)

When you are ready to use real Firebase config files, run Gradle with:

```bash
./gradlew :app:assembleDevDebug -Pkipita.enableFirebase=true
```

Without this flag, Google Services and Crashlytics plugins are skipped for local prototype builds.
