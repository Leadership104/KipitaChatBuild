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
