# Kipita – Developer Handoff & Pre-Launch Checklist

**Status:** Pre-launch | **Date:** March 2026 | **Platform:** Android (Kotlin/Compose) + Web PWA

---

## Repositories

| Repo | URL | Branch |
|---|---|---|
| Android + Web | `github.com/Leadership104/KipitaChatBuild` | `main` |
| iOS (Swift) | `github.com/Leadership104/KipitaIOS` | `main` |

---

## Project Structure

```
KipitaChatBuild/
├── app/                         Android app module
│   ├── src/dev/                 Dev flavor (package: com.kipita.dev)
│   ├── src/staging/             Staging flavor (package: com.kipita.staging)
│   └── src/prod/                Prod flavor (package: com.kipita)
├── web/                         PWA web app (index.html + sw.js + manifest.json)
├── docs/                        Design docs & architecture notes
└── credentials/                 🔒 gitignored — OAuth / service account keys
```

---

## Android Build Instructions

### Prerequisites
- Android Studio Meerkat or later
- JDK 17 (bundled in Android Studio JBR)
- `local.properties` at project root with real API keys (see Keys section)

### Build commands (Git Bash / terminal)
```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

# Dev debug (daily testing)
./gradlew assembleDevDebug

# Staging
./gradlew assembleStagingDebug

# Production release (needs keystore)
./gradlew assembleProdRelease
```

### Run on emulator
```bash
emulator.exe -avd Pixel_9_Pro -no-snapshot-load
adb install -r app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

---

## Required API Keys (`local.properties`)

```properties
# Gemini AI (Google AI Studio — free tier available)
GEMINI_API_KEY=YOUR_KEY_HERE

# Google Places API (New) — enable in Google Cloud Console
GOOGLE_PLACES_API_KEY=YOUR_KEY_HERE

# Google Maps SDK for Android
MAPS_API_KEY=YOUR_KEY_HERE
```

> All keys are injected via `BuildConfig` — never hardcode or log them.

---

## Firebase Configuration ✅

| Item | Status |
|---|---|
| Project ID | `kipita-99351` |
| Package names | `com.kipita` / `com.kipita.dev` / `com.kipita.staging` |
| google-services.json | ✅ Deployed to all 7 flavor directories |
| Google Sign-In | ✅ Enabled — OAuth client ID in strings.xml |
| Firestore | ⬜ Not yet used — Room DB only |
| FCM Push | ⬜ Token registration wired, backend needed |

### SHA-1 Fingerprint — REQUIRED before Google Sign-In works on device
Run and add to Firebase Console → Project Settings → Your Apps:
```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android -keypass android
```
Then: **Firebase Console → kipita-99351 → Project Settings → Your apps → com.kipita.dev → Add fingerprint**

---

## Web PWA (`/web/`)

| Feature | Status |
|---|---|
| Offline support (service worker) | ✅ |
| Installable (manifest + icons) | ✅ Icons needed (192px, 512px PNGs) |
| Mobile bottom nav bar | ✅ |
| PWA install banner | ✅ |
| Live BTC prices (CoinGecko) | ✅ |
| BTCMap iframe | ✅ |
| Explore / Places (OpenStreetMap) | ✅ |
| Offline phrase cards | ✅ 15 languages |
| Itinerary planner (localStorage) | ✅ |
| SOS modal | ✅ |

### Deploy
The `web/` folder is a static site — deploy to:
- **GitHub Pages:** push `web/` contents to `gh-pages` branch
- **Netlify/Vercel:** point root to `web/` directory
- **Firebase Hosting:** `firebase deploy` with `web/` as public dir

> ⚠️ PWA icons at `web/icons/icon-192.png` and `web/icons/icon-512.png` are referenced but not yet created. Must add before publishing.

---

## iOS

iOS codebase is a separate repo: `github.com/Leadership104/KipitaIOS`

Refer to that repository's own README for build instructions.

---

## Pre-Launch Developer Checklist

### Android
- [ ] Add debug SHA-1 fingerprint to Firebase Console
- [ ] Add release SHA-1 fingerprint to Firebase Console (for production APK)
- [ ] Set real `GEMINI_API_KEY`, `GOOGLE_PLACES_API_KEY`, `MAPS_API_KEY` in `local.properties`
- [ ] Enable Google Maps SDK in Google Cloud Console for project
- [ ] Enable Google Places API (New) in Google Cloud Console
- [ ] Test Google Sign-In on physical device (emulator may fail)
- [ ] Test BTC merchant map loads (BTCMap + CashApp sources)
- [ ] Test Explore → Places screen with GPS permission
- [ ] Test Advisory screen (SafetyAiEngine + Dwaat API)
- [ ] Test AI assistant with real Gemini key
- [ ] Verify Room DB migrations run cleanly on fresh install (v1→11)
- [ ] Test trip creation, cancellation, recreation flow
- [ ] Test SOS sheet — hospital / fire station links open correctly
- [ ] Run `./gradlew assembleProdRelease` and test signed APK
- [ ] Proguard/R8 — verify no class stripping issues (Moshi, Retrofit, Room)

### Web PWA
- [ ] Create `web/icons/icon-192.png` and `web/icons/icon-512.png` (Kipita branding)
- [ ] Deploy to hosting (GitHub Pages / Netlify / Firebase Hosting)
- [ ] Test "Add to Home Screen" prompt on Android Chrome
- [ ] Test "Add to Home Screen" on iOS Safari (Share → Add to Home Screen)
- [ ] Verify offline mode works (disable WiFi, reload)
- [ ] Test CoinGecko price feed loads
- [ ] Verify BTCMap iframe loads on mobile

### Google Play
- [ ] Create release keystore (store in password manager — never commit)
- [ ] Sign prod APK / AAB with release key
- [ ] Add release SHA-1 to Firebase Console
- [ ] Prepare store listing: screenshots (phone + tablet), icon, description
- [ ] Set up Play Console internal testing track first
- [ ] Privacy Policy URL: `https://kipita.com/privacy` — page must be live
- [ ] Terms of Service URL: `https://kipita.com/terms` — page must be live

### App Store (iOS — separate repo)
- [ ] Enroll in Apple Developer Program ($99/yr)
- [ ] Configure App Store Connect listing
- [ ] Run TestFlight internal test before submission

---

## Known Gaps / TODOs

| Area | Issue | Priority |
|---|---|---|
| Backend API (`api.kipita.app`) | Endpoints return 404 — backend not deployed | High |
| FCM Push Notifications | Token registered in app but no backend to send | Medium |
| CashApp Pay | OAuth flow incomplete — placeholder endpoints | Low |
| NomadList API | Public API removed — falls back to Room cache | Low |
| Coinbase / River wallet | OAuth flows not fully implemented | Low |
| Web PWA icons | `icons/` directory missing actual PNG files | High |
| Production keystore | Not yet created | High (before release) |

---

## Key Contacts

| Role | Contact |
|---|---|
| Project Owner | leadership104@gmail.com |
| Support email | info@kipita.com |

---

## Architecture Summary

```
Android Stack:
  Kotlin + Jetpack Compose (UI)
  Hilt (DI)
  Room + SQLCipher AES-256 (local DB, v11)
  Retrofit + OkHttp + Moshi (networking)
  Gemini 2.0 Flash-Lite (AI, native SDK)
  Firebase Auth (Google Sign-In)
  WorkManager (background sync)

APIs:
  Google Places (New) v1 — nearby places
  BTCMap v2 — Bitcoin merchants
  Dwaat — travel advisories / safety
  CoinGecko — crypto prices
  Frankfurter — fiat exchange rates
  Open-Meteo — weather
  Google Maps SDK — map display

Web PWA:
  Vanilla HTML/CSS/JS (no framework)
  Service Worker v3 (offline-first)
  Web App Manifest (installable)
  CoinGecko REST — live prices
  BTCMap iframe — merchant map
  Overpass/Nominatim — place search
```
