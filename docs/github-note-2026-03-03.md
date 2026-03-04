# Kipita Restore Note (2026-03-03)

## Scope
- Restored core UI/UX and feature behavior to the earlier clean build style (`b186ed7`) for key screens where functionality had drifted.

## Restored Screens
- `app/src/main/java/com/kipita/presentation/map/MapScreen.kt`
- `app/src/main/java/com/kipita/presentation/map/MapViewModel.kt`
- `app/src/main/java/com/kipita/presentation/main/KipitaApp.kt`
- `app/src/main/java/com/kipita/presentation/explore/ExploreScreen.kt`
- `app/src/main/java/com/kipita/presentation/home/HomeScreen.kt`
- `app/src/main/java/com/kipita/presentation/trips/MyTripsScreen.kt`
- `app/src/main/java/com/kipita/presentation/trips/TripDetailScreen.kt`
- `app/src/main/java/com/kipita/presentation/settings/SettingsScreen.kt`
- `app/src/main/java/com/kipita/presentation/auth/AuthScreen.kt`

## Feature Areas Recovered
- Map screen returned to prior clean Google Map layout with:
  - Search bar geocoding and camera recenter behavior
  - BTC overlay toggle and BTC source filters
  - Nearby category filtering (BTC/Food/Cafe/Shops)
  - Offline cache action and travel notice cards
- Trips flow preserved:
  - Upcoming/Past/Cancelled views
  - Plan trip bottom sheet, transport links, quick tools
  - Trip details with notes, invite, cancel, and mark-complete flows
- Home/Explore/Settings/Auth screens restored to prior UI behavior and button routes used in earlier builds.

## Compatibility Fixes Applied
- Aligned restored navigation with current AI screen signature in `KipitaApp.kt`.
- Added required Material3 opt-ins for restored composables using experimental APIs.

## Validation
- `gradlew.bat -g .gradle-local :app:compileDevDebugKotlin` passed.
- Emulator launch/install run attempted after restore (see commit terminal logs for final run status).

## Follow-up (fast demo readiness patch)
- Fixed My Trips: `✈️ Plan a new trip` quick action now opens the same Plan Trip flow as the `+` FAB (manual or AI path).
- Restored original AI tab UI flow and wired navigation prefill/trip handoff again.
- Updated AI tab model badge display to Gemini-only for current live setup.
- Added missing `app/src/dev/google-services.json` (copied from staging config) so dev/demo flavor resolves Firebase config consistently.
- Improved live API reliability and responsiveness:
  - Removed blocking placeholder certificate pin enforcement from runtime HTTP client path.
  - Enabled connection retry + explicit timeouts in `NetworkModule`.
  - Increased wallet live price responsiveness:
    - Price poll interval: 30s -> 15s
    - Price cache TTL: 30s -> 10s
- Map UX updates for live demo:
  - Added explicit location-enable popup on map entry.
  - Kept geolocation + manual search-bar flow together.
  - Added extra top spacing so back button is easier to tap.
- Bottom nav AI center icon now has additional whitespace/padding for cleaner touch spacing.

## Final Fast UI + Live Data pass
- AI bottom nav center button resized smaller so it fits cleanly within available white space.
- Crypto live prices set to near-live refresh behavior:
  - poll interval: 5s
  - cache window: 2s
- Map back arrow moved further down for easier tap access.

## API/JSON live readiness status
- Gemini API integrated and active through `KipitaAIManager` and `LlmRouter`.
- Google Maps + Places integrated; map key fallback uses `GOOGLE_PLACES_API_KEY` if `MAPS_API_KEY` is not explicitly set.
- CoinGecko live pricing integrated for BTC/ETH/SOL in Wallet.
- Dev flavor Firebase JSON now present:
  - `app/src/dev/google-services.json`
  - `app/src/staging/google-services.json`
  - `app/src/prod/google-services.json`
- Runtime networking tuned for demo reliability/latency:
  - connection retry enabled
  - explicit connect/read/write timeouts

## Play Store readiness: required keys/config before release
Put these in `local.properties` (for local/release build pipeline secrets injection), or CI secret manager equivalents:
1. `MAPS_API_KEY=`  
   Where used: Android manifest placeholder in `app/build.gradle.kts` (`manifestPlaceholders["MAPS_API_KEY"]`).
2. `GOOGLE_PLACES_API_KEY=`  
   Where used: `BuildConfig.GOOGLE_PLACES_API_KEY` via `app/build.gradle.kts`.
3. `GEMINI_API_KEY=`  
   Where used: `BuildConfig.GEMINI_API_KEY` (AI generation and chat).
4. `OPENAI_API_KEY=`  
   Where used: `BuildConfig.OPENAI_API_KEY` (multi-model router path, if enabled).
5. `CLAUDE_API_KEY=`  
   Where used: `BuildConfig.CLAUDE_API_KEY` (multi-model router path, if enabled).
6. `default_web_client_id` (Google Sign-In)  
   Where to set: generated from Firebase `google-services.json` + console OAuth client; referenced in `app/src/main/res/values/strings.xml`.

Also ensure Firebase package registration and SHA-1/SHA-256 are configured for release app id variants.

## Emulator clean-run validation (fast)
- Removed all Kipita app instances from emulator:
  - `com.kipita.dev`
  - `com.kipita.baselineprofile`
- Reinstalled fresh `devDebug` and relaunched:
  - `:app:installDevDebug` passed
  - launch intent: `com.kipita.dev/com.kipita.MainActivity`
- Post-launch package check confirms only active app instance is:
  - `com.kipita.dev`
- Quick log sanity check after cold launch:
  - no immediate fatal crash patterns observed
  - no immediate API exception signatures observed

## Live API/JSON demo readiness summary
- Gemini API key is configured in `local.properties` and injected into `BuildConfig`.
- Google Places key is configured in `local.properties`; Maps placeholder falls back to Places key if `MAPS_API_KEY` is absent.
- Firebase JSON is present for `dev`, `staging`, and `prod` source sets.
- Wallet live price pipeline is configured for near-live refresh and short cache TTL.

## Latest patch (packing list + Gemini + messaging + live feeds)
- Packing List UI verified and retained in original style:
  - checklist section
  - USPS "Put Mail on Hold" row
  - section tab switch (Checklist / Visa Tips)
- AI orchestration switched to Gemini-first for both:
  - normal assistant prompts
  - trip instant-message AI planner replies
  This avoids failures when OpenAI/Claude keys are not configured.
- Live crypto robustness improved:
  - retry on transient CoinGecko fetch failure
  - preserve last good value instead of replacing with zeros
  - explicit refresh on wallet screen entry
- Weather now uses live Open-Meteo API in Home weather sheet with refresh action.

## Emulator instance cleanup + rerun
- Force-stopped and removed `com.kipita.dev`, then fresh installed `:app:installDevDebug`.
- `com.kipita.baselineprofile` uninstall returned `DELETE_FAILED_INTERNAL_ERROR` on emulator package manager, but active app instance is clean.
- Relaunched with launcher intent (`monkey`) and verified active package:
  - `com.kipita.dev`

## Affiliate Link Expansion (fast pass)
- Updated transport booking links to affiliate routes in:
  - Home (`Book Transport`)
  - My Trips (`Book Transport`)
  - Trip Detail (`Book & Manage`)
- Expedia affiliate applied for Flights, Car Rental, and Cruise:
  - `https://expedia.com/affiliate/eA2cKky`
- Hotels affiliate applied for Hotels:
  - `https://www.hotels.com/affiliate/RrZ7bmg`
- Updated Finance/Perks affiliate links in Wallet and Settings:
  - Kinesis: `https://kms.kinesis.money/signup/KM00083150`
  - Swan Bitcoin: `https://www.swanbitcoin.com/kipita/`
  - Fold: `https://use.foldapp.com/r/MAJL4MYU`

## Accessibility + Places UX refresh
- Increased baseline touch target and visual size in main navigation icons/labels.
- Home:
  - Removed home-page `Nomad Tips` block.
  - Removed `Safety & Help` section card from Home feed.
  - Added top-bar-driven SOS trigger support via app state (`openSosSignal`) so emergency entry point is now in top controls.
  - Increased key hero/search/section typography and quick-tool size for easier readability.
- Top bar:
  - Added emergency icon button (left of weather/account controls).
  - Added weather indicator (emoji + current temperature) beside account avatar.
- Explore/Places:
  - Added `onOpenWebView` support in `ExploreScreen`.
  - Default tab now opens Places first while keeping Places on the right tab position.
  - Refactored category UI from small horizontal chips into larger boxed grid cards for simpler selection.
  - Added BTC button under `Finance & Services` that opens in-app webview to `https://btcmap.org/map`.
  - Increased icon/text size across Places cards and controls for easier usability.

## Wallet live-price reliability hotfix
- Root-cause mitigation from prior working builds applied:
  - Wallet open signal is now passed from app navigation to Wallet screen.
  - Every time user opens/clicks Wallet tab, Wallet forces:
    - Crypto sub-tab selection
    - immediate live price refresh call
- Polling cadence normalized to 30s for stable live updates and reduced API throttling risk.
- Live prices remain visible when entering Wallet so users can verify feed quickly.
