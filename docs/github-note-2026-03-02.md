# Kipita UI + Readiness Note (2026-03-02)

## Completed in this update
- Wallet toggle order switched to **Currency first**, **Crypto second**.
- Wallet top balance switch added with manual entry:
  - Currency Balance (default first)
  - BTC Balance
- Wallet manual entry fields added:
  - Currency code + amount
  - BTC amount
- Perks and external/legal/affiliate links routed to in-app web view in prior patch set.
- Map in-screen back button aligned to top-left for easier tap area.
- Bottom navigation spacing increased for better touch comfort.

## Fast validation performed
- `:app:compileDevDebugKotlin` passed.
- `:app:installDevDebug` passed and app launched on emulator.
- Targeted unit-test compile surfaced pre-existing test-suite issues (see blockers below).

## Store-readiness blockers found
- iOS build target is missing in this repo (no Xcode project / Podfile / iOS app module).
- Unit test suite currently fails to compile in multiple files under `app/src/test`.
- Affiliate URLs in Settings still use placeholders:
  - `AFFILIATE_FOLD_URL`
  - `AFFILIATE_SWAN_URL`
  - `AFFILIATE_KINESIS_URL`
  - `AFFILIATE_UPSIDE_URL`
- API/config placeholders still present:
  - `OPENAI_API_KEY` and `CLAUDE_API_KEY` in `AiModule.kt`
  - `default_web_client_id` empty in `res/values/strings.xml`
  - `MAPS_API_KEY` must be supplied for manifest substitution
- `API_READY` sections remain in auth/trips/common docs/comments for production hardening.

## JSON/config status
- `google-services.json` exists for:
  - `app/src/dev/debug/google-services.json`
  - `app/src/staging/google-services.json`
  - `app/src/prod/google-services.json`
- `google-services.json.template` still referenced in README for setup workflow.

## Next recommended hardening steps
1. Replace all affiliate placeholders with production referral URLs.
2. Populate production API keys/secrets via secure CI + local secrets plugin.
3. Fix failing unit tests in `app/src/test` and run full test matrix.
4. Add release checks: lint, baseline profile, ProGuard/R8 verify, Play pre-launch report.
5. Add iOS project/build pipeline if App Store (iOS) delivery is required.
