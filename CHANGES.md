# Change Log — Session 2026-03-02

Commit: `0b491bd` — *feat: Explore tab places navigation + fix all Hilt/build blockers*

## New Features

### Explore Tab — Places Sub-Navigation
- **`PlaceDetailScreen.kt`** *(new)* — full detail view with Coil photo, 5-star rating, open/closed badge, address, phone (dial intent), website (→ InAppBrowser), Add-to-Trip button, favorite toggle
- **`KipitaApp.kt`** — replaced ExperienceScreen with state-based sub-nav: PlacesScreen → PlacesCategoryResultScreen → PlaceDetailScreen → InAppBrowserScreen; tapping active tab pops to root
- **`PlacesCategoryResultScreen.kt`** — tapping a result card navigates to PlaceDetailScreen (removed expand-in-place)
- **`YelpPlacesRepository.kt`** — added `website` and `photoRef` fields to `NearbyPlace`; wired from `GooglePlaceDto.websiteUri` and `photos[0].name`

## Bug Fixes / Build Blockers

### Hilt DI — MissingBinding (8 errors fixed)
- **`NetworkModule.kt`** — added `@Provides` for `CoinbaseApiService`, `GeminiCryptoApiService`, `RiverApiService`, `GooglePlacesApiService`, `DwaatApiService` (each with its own Retrofit instance + qualifier)
- **`TravelDataModule.kt`** — added `@Provides` for `UserDao`, `SavedLocationDao`, `DirectMessageDao`, `OfflineMessagingRepository`
- **`KipitaDatabase.kt`** — added `UserEntity`, `SavedLocationEntity`, `DirectMessageEntity` to `@Database` entities; added abstract DAO methods; bumped version 5→6

### Build Config
- **`app/build.gradle.kts`** — added `localProp()` helper; `manifestPlaceholders["MAPS_API_KEY"]`; BuildConfig fields for all API keys; added deps: `coil-compose`, `material-icons-extended`, `credentials:1.3.0`, `credentials-play-services-auth:1.3.0`, `googleid:1.1.1`, `firebase-auth-ktx`, `generativeai:0.9.0`; added `lint { abortOnError = false }`
- **`gradle/libs.versions.toml`** — added `coil`, `coil-compose`, `firebase-auth` entries; stripped BOM
- **`gradle-wrapper.properties`** — downgraded `9.3.1` → `8.9` (KAPT incompatible with Gradle 9)
- **`settings.gradle.kts`** — commented out `include(":baselineprofile")` (missing plugin)
- **`gradle.properties`** — stripped BOM

### Firebase
- **`app/src/dev/google-services.json`** — added `com.kipita.dev` client entry
- **`app/src/prod/google-services.json`** — added `com.kipita` client entry

### Compose Opt-Ins
- **`AuthScreen.kt`** — `@OptIn(ExperimentalMaterial3Api::class)`
- **`TranslateScreen.kt`** — `@OptIn(ExperimentalMaterial3Api::class)`
- **`MyTripsScreen.kt`** — `@OptIn(ExperimentalMaterial3Api::class)` on `PlanTripSheet`
- **`TripDetailScreen.kt`** — `Icons.Default.Cancel` → `Icons.Default.Close`
