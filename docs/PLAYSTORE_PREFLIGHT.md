# Play Store preflight checklist

This checklist is intended to be run before creating a production Play release.

## Mandatory checks

- [ ] `./scripts/playstore_preflight.sh` passes without FAIL lines.
- [ ] `google-services.json` is provided for the target flavor (`prod`) if Firebase features are expected.
- [ ] Release signing config/keystore is configured in CI or local secure properties.
- [ ] Privacy policy URL is prepared for Play Console declaration.
- [ ] Data Safety form is completed for location, analytics, and microphone usage.

## Build checks

- [ ] `./gradlew :app:assembleProdRelease`
- [ ] `./gradlew :app:lintProdRelease`
- [ ] `./gradlew :app:testProdDebugUnitTest`

## Functional checks

- [ ] All major CTA buttons route to a screen, action, or webview flow.
- [ ] Login/guest flow works.
- [ ] Wallet, Map, AI, Translate, Trips, and Social entry points are reachable.
- [ ] Group/user data persists across app restarts.

## Notes

- In this prototype, Google Services and Crashlytics are applied only when a valid `google-services.json` exists.
- Replace destructive migrations with explicit Room migrations before launch.
