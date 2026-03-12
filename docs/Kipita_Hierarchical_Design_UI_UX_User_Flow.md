# Kipita Design Hierarchy, UI/UX, and User Flow

## 1) Product Structure (Hierarchical IA)

```text
Kipita App
1. Launch + Session Gate
   1.1 SplashActivity
   1.2 Session check (ProjectUtil SESSION)
   1.3 Route:
       - Logged out -> PreLoginActivity
       - Logged in  -> MainActivity

2. Authentication Funnel
   2.1 PreLoginActivity
       - Entry options: Login, Register, social
   2.2 LoginActivity
       - Email/password
       - Facebook sign-in
       - Google sign-in
       - OTP/Code fallback flow
   2.3 RegisterActivity
       - First/Last name
       - Mobile verify (OTP)
       - Email verify (code)
       - Password + ZIP

3. Main Shell (MainActivity)
   3.1 Top header
       - User avatar initials
       - Greeting/title
       - Change location action
       - Weather chip
   3.2 Secondary advisory header
       - Country/region state + advisory level
   3.3 Primary content region
       - Fragment container
   3.4 Bottom navigation (4 pillars)
       - Places
       - Advisory
       - Travel
       - Perks

4. Places Pillar (default landing after auth)
   4.1 RecommadationFragment (category switch)
       - Restaurants
       - Entertainment
       - Shopping
   4.2 RestaurantMainFragment
       - Search + voice search
       - Category cards/list
       - Settings/reorder affordance
       - Branch to results fragment
   4.3 RestaurantsNewFragment
       - Search result list
       - Place detail bottom sheet/dialog

5. Advisory Pillar
   5.1 ApplicationsMainFragment and advisory detail routing
   5.2 Country and travel restriction contexts

6. Travel Pillar
   6.1 PlanerMainFragment
   6.2 Planner details and travel planner workflow

7. Perks Pillar
   7.1 PerksMainFragment
   7.2 Offer/deal browsing

8. Support and Secondary
   8.1 Profile + update profile/password
   8.2 Notification settings
   8.3 Group management and invites
   8.4 Gallery and media flows
```

## 2) UI Design Breakdown (Screen-by-Screen)

## 2.1 Splash (`activity_splash.xml`)
- Centered logo with brand statement and low cognitive load.
- 1.5-second branded delay before routing to auth or app shell.
- UX intent: establish recognition and avoid immediate hard transition.

## 2.2 Pre-Login (`activity_pre_login.xml`)
- Single-screen brand-first entry with strong logo and tagline.
- CTA hierarchy:
  - Primary: Login
  - Secondary: Social login options
  - Tertiary: Create account
- UX note: strong conversion focus, but dense informational copy above form can increase scan time on small screens.

## 2.3 Login (`activity_login.xml`)
- Email + password fields at top of action stack.
- "Forgot password" adjacent to auth controls.
- Social auth strip uses dummy buttons to trigger provider flows.
- UX note: clear primary action, but iconography and labels are compact and could hurt accessibility on smaller devices.

## 2.4 Register (`activity_register.xml`)
- Multi-step-like single form (name, phone verify, email verify, password, zip).
- Inline verify buttons for phone/email create explicit trust checkpoints.
- UX risk: long vertical form + repeated validation toasts can feel heavy without step progress indication.

## 2.5 Main Shell (`activity_main.xml` + `app_bar_main.xml` + `content_main.xml`)
- Three-level shell:
  - Top app bar with profile + location/weather context.
  - Secondary advisory bar (risk/status context).
  - Main fragment viewport + bottom nav.
- Bottom nav semantics:
  - `first`: Places (red active state default)
  - `second`: Advisory
  - `third`: Travel
  - `fourth`: Perks
- UX strength: persistent mode switching, color-coded active state, contextual travel status.
- UX risk: header is information-dense; "change location" and weather controls compete for attention.

## 2.6 Places Hub (`fragment_home.xml`)
- Horizontal category switch (`btn1`, `btn2`, `btn3`) for Restaurants/Entertainment/Shopping.
- Embedded child fragment container for category-specific browsing.
- UX strength: no route jump for category changes; low interaction cost.

## 2.7 Restaurant Discovery (`restaurant_mian_fragment.xml`)
- Search bar with voice + manual submit.
- Recycler list for dynamic category/data sections.
- Empty state panel with "No Data Found" guidance.
- UX pattern: list-first discovery with fast query refinement.

## 3) UX Behavior and Interaction Model

## 3.1 Navigation Mental Model
- Global context: bottom nav (mode).
- Local context: top category buttons inside Places.
- In-content exploration: search + lists + detail sheets.

## 3.2 Feedback and State
- Toast-driven validation and error feedback across auth and forms.
- Visual active-state changes (tab background color and icon tint).
- Empty-state fallback for no-result discovery.

## 3.3 Context Persistence
- Session + identity + location cached via `ProjectUtil` shared preferences.
- Supports instant relaunch into contextualized browsing experience.

## 4) User Flow (Practical)

## 4.1 Entry Flow
1. Open app.
2. `SplashActivity` checks `SESSION`.
3. If logged out: `PreLoginActivity`.
4. If logged in: `MainActivity` and default Places route.

## 4.2 Auth Flow
1. Pre-Login screen.
2. Choose Login, Register, or social.
3. Submit credentials/verification.
4. Session saved.
5. Route to Main shell.

## 4.3 Places Discovery Flow
1. Main shell opens at Places by default.
2. User selects Restaurants/Entertainment/Shopping.
3. User searches or taps a category.
4. Results list appears.
5. User opens place detail.

## 5) "How does a user get pizza?" (2-3 Click Analysis)

## Scenario A: Already logged in, on default Places route (2 clicks + typing)
1. Click search field in Restaurants view and type `pizza`.
2. Click search icon.
3. (Optional 3rd click) Click a pizza result card to open details.

Why this is fast:
- App defaults to `RecommadationFragment -> RestaurantMainFragment`.
- No extra mode-switch click is required for the most common local intent.

## Scenario B: User is in another bottom-tab section (3 clicks + typing)
1. Click `Places` bottom tab.
2. Click `Restaurants` category chip (if not already selected).
3. Click search icon after entering `pizza`.

Optimization opportunity:
- Preserve last-used subcategory and query hint to reduce click count back to 2.

## Scenario C: Cold start, logged out user (auth required first)
1. Login/Register path.
2. Land in Places default route.
3. Use Scenario A flow.

## 6) UX Recommendations for Your Next Implementation Pass

1. Add a compact "Quick Find" chip row in Restaurants: `Pizza`, `Coffee`, `Breakfast`, `Late Night`.
2. Autofocus search field when Restaurants tab opens and keyboard policy allows it.
3. Persist last successful query and show one-tap "Search again: pizza".
4. Convert long registration into staged steps with progress indicator.
5. Unify copy and spelling across screens (`Recommendation`, `Location`, `Planner`).
6. Add explicit loading and error containers (not only toasts) for network-dependent lists.
7. Add analytics events for each step in pizza flow to measure friction:
   - tab_switch_places
   - category_restaurants
   - search_submit
   - result_open

## 7) Technical Mapping (Where This Comes From)
- `SplashActivity.java`: launch gate and session routing.
- `PreLoginActivity.java`, `LoginActivity.java`, `RegisterActivity.java`: auth funnel.
- `MainActivity.kt`: shell orchestration, bottom nav click behavior.
- `app_bar_main.xml`, `content_main.xml`: header and nav layout.
- `RecommadationFragment.kt`, `fragment_home.xml`: category hub.
- `RestaurantMainFragment.kt`, `restaurant_mian_fragment.xml`: restaurant search and list UX.
- `APIService.java`, `RetrofitClient.java`, `YelpFusionApi.java`: network layers backing behavior.
