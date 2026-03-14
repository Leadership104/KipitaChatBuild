# Changelog

All notable changes to Kipita are documented here.

---

## [v0.9.0] — 2026-03-13 — Real-Time Safety AI + Time-Aware Places

### Real-Time Safety AI (Advisory Screen)
- **`SafetyAiEngine`** — new singleton that fetches three Dwaat API endpoints in parallel (`advisorySections`, `getWeatherAdvisory`, `restrictions`) and passes the structured data to Gemini for a concise, verified safety briefing
- **`AdvisoryScreen`** — GPS permission requested on open; reverse-geocodes to country name shown in header; `AiSafetyInsightCard` displays Gemini analysis with a color-coded US State Dept–style level badge (✅ Normal / 🟡 Caution / ⚠️ Reconsider / 🚫 Do Not Travel); live Dwaat advisory section cards replace static sample text; real weather line (temperature + condition) shown in the header gradient; refresh button re-runs AI analysis on demand
- **`AdvisoryViewModel`** — now injects `SafetyAiEngine`; `loadWithLocation(country, lat, lng)` fires on GPS grant; `refreshAiInsight()` available for on-demand re-analysis; exposes `safetyReport`, `aiInsight`, `weatherLine`, `dwaatSections` in `AdvisoryUiState`
- **`KipitaAIManager`** — new `analyzeSafetyContext(contextBlock, country)` method: Gemini call with a safety-analyst system persona, uses only verified Dwaat data, no filler
- **AI Screen integration** — `AiViewModel.analyzeSafety(country, lat, lng)` fetches from `SafetyAiEngine` and formats a structured safety report as a chat response; "Travel Advisories" quick action card now calls this method instead of a generic chat prompt

### Time-Aware Place Icons
- **`PlaceCategory.timeAwareEmoji(hourOfDay: Int)`** extension function added to the `PlaceCategory` enum — returns a contextually appropriate emoji based on the current hour:
  - Restaurants: 🥞 breakfast (5–10am) → 🍱 lunch (11am–3pm) → 🍽️ dinner (4–10pm) → 🌮 late night
  - Cafes: ☕ morning (5–11am) → 🧋 afternoon boba (12–4pm) → 🍵 evening tea
  - Nightlife: 🍻 happy hour (4–8pm) → 🌙 night
  - Parks: 🌅 sunrise run (5–9am) → 🌳 daytime (10am–5pm) → 🌆 evening walk
  - Shopping: 🛍️ morning → 🛒 prime hours → 🏪 late
  - Entertainment: 🎡 morning attractions → 🎭 afternoon shows → 🎬 evening cinema
  - Fitness: 🏃 morning workout → 🏋️ daytime gym → 🧘 evening yoga
  - Transport: 🚇 rush hour → 🚌 daytime → 🌙 late night
  - Hotels: 🛎️ check-in/evening → 🌙 overnight
- **`PlacesCategoryResultScreen`** — header emoji + sibling category chip strip both use `timeAwareEmoji`
- **`ExploreScreen`** category chip grid (`PlaceCategoryChip`) uses `timeAwareEmoji(currentHour)`

---

## [v0.8.0] — 2026-03-12 — Pre-Launch Release

### Live Safety Level Bar
- `SafetyLevelBar` in the top bar is now **data-driven** — level (1–4) and label text are computed from real advisory notices via `AdvisoryViewModel`
- Level mapping: 1 = Normal Precautions (green), 2 = Increased Caution (yellow), 3 = Reconsider Travel (orange), 4 = Do Not Travel (red)
- Label updates automatically when advisory data loads (e.g. "Exercise normal precautions", "Do not travel")

### Bug Fixes
- Fixed `FIREBASE_PROJECT_ID` BuildConfig: was `kipita-a1694`, corrected to `kipita-99351`
- Fixed `animateColorAsState` import in `AuthScreen` (wrong package: `animation.core` → `animation`)

### Next Steps Required Before Public Launch
> **Google Sign-In** — `oauth_client` is currently empty in all `google-services.json` files.
> Google Sign-In button shows a "not configured" toast until this is resolved.
> Steps:
> 1. Firebase Console → **kipita-99351** → Authentication → Sign-in method → Google → **Enable**
> 2. Download the updated `google-services.json` and replace `app/src/dev/`, `app/src/staging/`, `app/src/prod/`
> 3. Re-run `./gradlew assembleDevDebug` — the `default_web_client_id` string will be auto-populated

> **Release Signing** — Set `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` in `local.properties` before running `assembleProdRelease` for Play Store submission.

> **SHA-1 Fingerprint** — Register the debug keystore SHA-1 (`debug.keystore`) in Firebase Console → Project Settings → Android apps → `com.kipita.dev` to enable Firebase Auth on the emulator.

> **Dwaat API Auth** — Confirm `DWAAT_BASE_URL=https://api.dwaat.com/` is reachable and any required auth tokens are provisioned for prod flavor.

> **Maps Billing** — Ensure Google Cloud billing is enabled for the `MAPS_API_KEY` and `GOOGLE_PLACES_API_KEY` projects before launch (Places API + Maps SDK require billing).

---

## [v0.7.0] — 2026-02-28

### 📍 GPS-Sorted Destinations (Explore)
- The 8 nomad-city destination cards are now sorted by proximity when GPS is active
- Closest city gets a green **"Near You ✦"** badge and a "sorted by distance from you" label
- Falls back to default order when location is unavailable

### 🗂️ Category Result Pages (Places)
- Tapping any category chip in the Explore → Places tab now opens a **dedicated full-screen result page**
- Each location card uses round curves (`RoundedCornerShape(20dp)`) and shadow (`4dp elevation`) for a uniform, polished look
- **Safety & Health categories** (Safety, Urgent Care, Pharmacies, Fitness): tapping a result opens Google Maps search **iframed in the in-app WebView** — user never leaves the app
- **All other categories**: tapping a card expands it in place to reveal phone number, distance, and review count
- GPS is auto-requested on entry to the result page; results fetch from the user's real location

### ₿ BTCMap in Finance & Services (Places)
- A dedicated orange **BTCMap** chip now appears in the Finance & Services category group
- Tapping it opens `btcmap.org/map` iframed inside the in-app WebView

### 🗺️ Map — Geolocation + Address Search
- Map screen now **auto-requests GPS permission** on first launch
- A **search bar** overlaid at the top of the map lets users type any city, address, or country
- On submit, the address is geocoded and the map **re-centers** to the new location

### ₿ BTC Source Toggle (Map)
- When the ₿ BTC filter is active in the map bottom sheet, three sub-pills appear: **₿ BTCMap**, **⚡ Cash App**, and **🌐 Both**
- Tapping a pill filters map markers and the merchant list to show only that source

### 🆘 SOS — Hospital & Fire Station Now Iframed
- "Navigate to Hospital" and "Navigate to Fire Station" in the SOS Emergency sheet now open **inside the in-app WebView** instead of launching an external maps app
- Keeps users fully within Kipita during emergencies
- "Call Emergency Services" (911) remains a native phone dialer intent

---

## [v0.6.0] — 2026-02-27

### ✈️ Destination Detail View (Explore)
- Tapping any of the 8 destination cards in Explore opens a rich detail sheet
- Sheet includes: hero image, stat pills (avg cost, WiFi, safety, weather), full travel description, and destination tags
- **Share** button fires a system share sheet with destination summary text
- **Add to My Trips** button creates an upcoming trip from the destination and navigates to it
- Users can also type any city, state, country, or address in the Explore search bar

### 🤖 AI Trip Planning → Upcoming Trips
- After the AI generates a trip plan, an **"Add to Trips"** button appears in the response card
- Tapping it creates a real upcoming trip from the AI output (same flow as manual trip creation)
- `AiViewModel` now exposes `lastPlanDestination` and `lastPlanDays` StateFlows
- `clearLastPlan()` resets the state after the trip is added to prevent duplicate creation

### ❌ Trip Cancellation with Group Notification
- **Cancel Trip** button added to Trip Detail screen (only shown for UPCOMING / ACTIVE trips)
- Tapping shows a confirmation dialog: *"Are you sure you would like to cancel?"* with optional reason field
- On confirm: trip status set to `CANCELLED`, all invited group members automatically notified by email via a mailto Intent
- Email includes trip name, destination, and a clear cancellation message
- New Room columns: `cancelledAt` (epoch ms) and `cancellationReason` (text)
- DB migrated from **v10 → v11** (SQL `ALTER TABLE` migration, non-destructive)

### 🔄 Cancelled Trips Section (Profile / My Trips)
- Cancelled trips now appear in a dedicated **"Cancelled Trips"** section below Past Trips in My Trips screen
- Each row shows the trip title, destination, cancellation reason, and a **Recreate** button
- Recreate clones the trip as a new UPCOMING trip starting 14 days from today, preserving all original details
- Upcoming trips query updated to exclude CANCELLED status

### 🆘 SOS Emergency Button (Safety & Help)
- New **Safety & Help** section added to the Home screen
- Red gradient **SOS Emergency Alert** button opens an emergency bottom sheet
- Sheet features:
  - Lists all invited trip members with their email addresses (sourced from active/upcoming trips)
  - **Alert All Members** — fires a pre-composed emergency email to all trip contacts
  - **Navigate to Hospital** — opens Google Maps routing to the nearest hospital
  - **Navigate to Fire Station** — opens Google Maps routing to the nearest fire station
  - **Call Emergency Services** — opens the phone dialer pre-filled with 911

---

## [v0.5.0] — 2026-02-21

### 🔗 In-App WebView & Perk Links
- All external transport, booking, and perk links now open inside an in-app WebView
- Prevents users from leaving the app for external content
- `HomeScreen` and `PackingListSheet` both pass `onOpenWebView` callbacks through to `KipitaApp`

### 🗺️ Google Maps + BTCMap Integration
- Replaced Canvas-based map with a live `GoogleMap` composable (maps-compose 4.3.3)
- BTC merchant markers loaded from BTCMap API (`v2/elements`) and filtered to ~50 km radius
- API key read from `local.properties → MAPS_API_KEY`

### 📍 Google Places API (New)
- Replaced Yelp Fusion with Google Places API v1
- `PlaceCategory` enum uses `googleTypes` list for type-based text search
- Auth via `X-Goog-Api-Key` header; base URL: `https://places.googleapis.com/`

### ❤️ Favorites / Saved Locations
- Heart button on Explore place cards toggles saved state
- `SavedLocationEntity` + `SavedLocationDao` backed by Room
- `ExploreViewModel.toggleSaved()` + `savedPlaceIds: StateFlow<Set<String>>`

---

## [v0.4.0] — 2026-02-14

### 🔒 Privacy & Legal Compliance
- GDPR-compliant account deletion flow
- Dark theme support
- ProGuard rules configured for release builds

### 🤖 Gemini AI Integration
- `KipitaAIManager` wraps `gemini-2.0-flash-lite` via native Android SDK
- Supports: `chat()`, `planTrip()`, `parseNlpSearch()`
- `isAiTyping: StateFlow<Boolean>` exposed for UI typing indicators
- API key loaded securely via secrets-gradle-plugin from `local.properties`

### 🗄️ Room DB Migrations
- **v7→v8**: username/auth fields + invites table
- **v8→v9**: `userSentiment` and `pastPreferences` on trips
- **v9→v10**: `isSample` flag on trips + `saved_locations` table
- Sample trip seeding (Tokyo) on first launch with `isSample=true`

---

## [v0.3.0] — earlier

### 🌐 Companion Web App (PWA)
- `/web/index.html` — live CoinGecko prices, BTCMap iframe, Google Maps embed
- `/web/manifest.json` — installable PWA with Kipita branding
- `/web/sw.js` — service worker: cache-first for static, network-first for API

### 💬 Group Trip Chat & Invites
- Up to 10 members per trip
- Invite list stored as JSON in `inviteListJson` column
- `parsedInvites()` extension parses emails from trip entity

---

*For full git history see: `git log --oneline`*
