## Change summary
- stabilized Gradle setup (8.9 wrapper, Kotlin plugin, new `gradle.properties` flags, conditional google services plugin, local signing config, `debug.keystore` generator) so builds/tests run without needing global AndroidX flags or unlocked keystores.
- repaired app code (theme/resources, launcher icons, activity/app setup, ViewModels/screens, repositories) to compile cleanly and respect error handling/overlays.
- synced critical domain/test files (clean DB definition, test assertions, `TravelDataEngine` scoring) so `:app:testDevDebugUnitTest` passes when run with Kotlin in-process mode.

## Tests
- `./gradlew -Pandroid.injected.invoked.from.ide=false :app:assembleDevDebug`
- `./gradlew :app:testDevDebugUnitTest`

## Remaining items
- `:app:testStagingDebugUnitTest` and `:app:testProdDebugUnitTest` were interrupted before completion; rerun only if necessary.
- Keep the recently generated `debug.keystore` out of commits (ignored) and update secrets before prod signing.
