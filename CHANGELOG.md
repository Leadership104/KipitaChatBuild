# Changelog

All notable changes to Kipita are documented here.

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
