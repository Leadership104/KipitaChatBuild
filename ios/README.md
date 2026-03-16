# Kipita iOS (Swift / SwiftUI)

iOS companion app for **Kipita** — nomad-focused travel intelligence with Bitcoin merchant maps, AI safety briefings, and group trip planning.

## Architecture

- **Language**: Swift 5.10+
- **UI**: SwiftUI
- **Pattern**: MVVM + Swift Concurrency (`async/await`)
- **Persistence**: SwiftData (iOS 17+)
- **Networking**: URLSession
- **DI**: Constructor injection + `@EnvironmentObject`

## Feature Parity with Android

| Feature | Android | iOS |
|---|---|---|
| Home (transport + SOS) | ✅ | scaffold |
| Trip planning (manual + AI) | ✅ | scaffold |
| Explore destinations | ✅ | scaffold |
| BTC Merchant Map | ✅ | scaffold |
| Safety AI Advisory | ✅ | scaffold |
| AI Chat (Gemini) | ✅ | scaffold |
| Google Sign-In | ✅ | scaffold |
| Group trips | ✅ | scaffold |

## Getting Started

### Prerequisites
- Xcode 16+
- iOS 17+ deployment target
- CocoaPods or Swift Package Manager

### Setup

1. Open `KipitaApp/` in Xcode (File → Open → select `KipitaApp.xcodeproj` after generating)
2. Or use the Swift Package: `File → Add Package → local path → KipitaApp/`
3. Copy `Config.xcconfig.template` → `Config.xcconfig` and fill in keys:

```
GEMINI_API_KEY = your_key_here
GOOGLE_PLACES_API_KEY = your_key_here
MAPS_API_KEY = your_key_here
FIREBASE_PROJECT_ID = kipita-99351
```

4. Add `GoogleService-Info.plist` from Firebase Console → Project Settings → iOS app

### Swift Package Dependencies (add in Xcode)
```
https://github.com/firebase/firebase-ios-sdk          (FirebaseAuth, FirebaseFirestore)
https://github.com/google/GoogleSignIn-iOS             (GoogleSignIn)
https://github.com/googlemaps/google-maps-ios-utils    (Google Maps)
```

## Folder Structure

```
ios/KipitaApp/Sources/Kipita/
├── App/              KipitaApp.swift, AppCoordinator, AppState
├── Models/           Trip, Place, SafetyReport, BTCMerchant, User
├── ViewModels/       One VM per screen, @Observable
├── Views/
│   ├── Home/         HomeView, SosSheet, PackingListSheet
│   ├── Trips/        TripsView, TripDetailView, PlanTripSheet
│   ├── Explore/      ExploreView, DestinationDetailSheet
│   ├── Map/          MapView, PlaceCard
│   ├── Advisory/     AdvisoryView, AiSafetyInsightCard
│   ├── AI/           AIAssistantView
│   ├── Auth/         AuthView, ProfileSetupView
│   └── Settings/     SettingsView
├── Services/
│   ├── GeminiService.swift        (Gemini 2.0 Flash-Lite)
│   ├── DwaatAPIService.swift      (advisories, weather, restrictions)
│   ├── BTCMapService.swift        (BTC merchant data)
│   └── GooglePlacesService.swift  (nearby places)
├── Persistence/      SwiftData models + KipitaDatabase
└── Utils/            LocationManager, ErrorLogger, Extensions
```

## API Endpoints

| Service | Base URL |
|---|---|
| Dwaat | `https://api.dwaat.com/` |
| BTCMap | `https://api.btcmap.org/v2/elements` |
| Google Places | `https://places.googleapis.com/v1/places:searchNearby` |
| Gemini | `https://generativelanguage.googleapis.com/v1beta/` |

## Database Schema (SwiftData)

Mirrors Android Room schema (DB v11):

- `TripModel`: id, destination, country, startDate, endDate, status, isSample, cancelledAt, cancellationReason
- `SavedLocationModel`: id, name, address, lat, lng, category, savedAt
- `BTCMerchantModel`: id, name, lat, lng, source
- `UserModel`: id, email, displayName, avatarUrl, createdAt

## Contact

info@kipita.com
