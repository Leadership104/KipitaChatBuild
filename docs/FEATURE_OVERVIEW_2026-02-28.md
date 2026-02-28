# Kipita Feature Overview (Recent Changes)

Scope: major feature changes shipped in the last several days, primarily **February 27-28, 2026**.

## 1) Android App: Major New Features

### Explore + Places + Map (Feb 28, 2026)
- GPS-sorted destination cards in Explore, including a **"Near You"** indicator for closest destination.
- Category chips in Places now open a **full-screen category results page**.
- Safety/health category results open Google Maps searches in the **in-app WebView** (no app exit).
- New BTC chip in Places category groups for direct BTCMap access.
- Map now supports:
  - auto location permission request
  - text address search + recenter
  - BTC source filter pills (**BTCMap / Cash App / Both**).

How to use:
1. Open `Explore` tab and allow location.
2. Tap any Places category chip to open full-screen results.
3. For emergency-related categories, tap a card to open in-app map search.
4. Open `Map`, use the top search bar for city/address recenter.
5. Toggle BTC and choose source pill to filter merchants.

### Trips + AI + Safety (Feb 27-28, 2026)
- Destination detail view with richer trip context.
- AI plans can be pushed directly into Trips via **Add to Trips**.
- Trip lifecycle upgrades:
  - **Cancel Trip** flow with optional reason
  - cancellation state persisted in DB
  - cancellation email notification to invited members
  - **Cancelled Trips** section with **Recreate** action
  - **Mark Complete** action in Trip Detail.
- New date picker UX for trip dates in My Trips.
- SOS emergency bottom sheet on Home with:
  - alert all invited members
  - navigate to nearest hospital/fire station in in-app WebView
  - direct emergency dial action.

How to use:
1. Generate plan in `AI` tab, tap **Add to Trips**.
2. Open `Trips` -> select trip -> use **Mark Complete** or **Cancel Trip** as needed.
3. In `My Trips`, scroll to **Cancelled Trips** and tap **Recreate** to clone as upcoming.
4. From `Home`, tap SOS button to send alerts or open emergency navigation actions.

### Auth, Profile, and Access Guarding (Feb 28, 2026)
- Google authentication flow integrated (Credential Manager/Firebase path).
- Profile setup supports display name + profile photo.
- Social travel groups route is now gated for guests (prompts sign-in/profile first).

How to use:
1. Tap profile avatar (top-right) -> `Sign In / Create Profile`.
2. Use Google sign-in or continue guest mode.
3. Open profile setup to set name/photo.
4. In `Social`, `Join travel groups` requires non-guest profile.

Prerequisites for Google auth:
- Valid `google-services.json` in flavor path.
- Google Sign-In enabled in Firebase project.

## 2) Web App (PWA): Major New Features

### Core Experience Expansion (Feb 27, 2026)
- Wallet connect flow (MetaMask) with persisted connection and balance display.
- Real places search improvements with location + category grid behavior.
- Offline-capable language phrases.
- SOS modal and itinerary interactions.
- Booking tiles and crypto affiliate tiles added.
- BTCMap-focused tile/content and expanded places presentation.
- External perks/content links shifted to **iframe viewer** to keep users inside web app context.
- Multiple web bug fixes (promise handling, selectors, bounds, JSON flow).

How to use:
1. Open `web/index.html` in browser.
2. Click **Connect Wallet** (MetaMask) in nav to connect/disconnect and view balance.
3. Use Places search by city/address or GPS to fetch nearby categories.
4. Use floating SOS button for emergency contact guidance.
5. Use Book/Crypto tiles; external links open in in-app iframe modal when supported.
6. Install as PWA from browser install prompt/menu.

## 3) Supporting Platform/Release Work

- Changelog updated and expanded through `v0.7.0` on **February 28, 2026**.
- Build/runtime hardening and lint/build fixes landed alongside feature work.

## 4) Commit Clusters Referenced

- `8546314` (2026-02-28): mark complete, date pickers, profile photo, Google auth, groups guard
- `84fb559` (2026-02-28): GPS destinations, category result pages, map search, BTC toggle
- `086ed03` (2026-02-28): SOS hospital/fire in-app navigation + changelog update
- `c4669a8` (2026-02-27): destination details, trip cancellation, AI->trips, cancelled trips, SOS button
- `19f4035` (2026-02-27): Google Maps, BTCMap, favorites, in-app WebView, PWA foundation
- `0f4d24b` / `8d7747b` / `46efc21` / `932a2fb` (2026-02-27): web wallet/places/offline/SOS/booking/affiliate/iframe fixes

