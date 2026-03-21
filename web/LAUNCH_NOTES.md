# Kipita Web App — Changelog & Launch Notes

**Last updated:** 2026-03-21
**Live at:** http://localhost:3000 · http://localhost:3001
**Production:** https://leadership104.github.io/KipitaChatBuild/

---

## Latest Changes (2026-03-21)

### Explore Photos — Real Images on All Category Cards
- **Place category result cards** now show real Wikimedia Commons photos (open-licensed) per category instead of random color gradients
- 12 category types mapped to curated photos: Food, Café, Hotel, Transit, BTC ATM, Shopping, Gym, Beach, Nightlife, Hospital, Pharmacy
- `CATEGORY_PHOTOS` map added — stable Wikimedia Commons thumbnail URLs, no API key required
- `generateDemoPlaces` now returns a `photo` property; address now uses actual detected location city name instead of hardcoded "CA 91354"
- Phone numbers now generated as valid 11-digit international format

### Destination Photos — Retry on Explore Tab Open
- `initExploreScreen` now checks if any destination photos are missing and re-triggers `fetchAllDestPhotos()` if so
- Ensures photos load even if Wikipedia API was slow on initial app load
- Added `console.warn` on photo fetch failure for production debugging

### API / Go-Live Audit
- All 14 external API endpoints verified: Wikimedia, ip-api, Nominatim, Open-Meteo, CoinGecko, metals.live, BTCMap, Overpass, open.er-api — all HTTPS, no localhost, no API keys required
- **Confirmed working without keys**: CoinGecko (free tier), Open-Meteo (free), Nominatim (free), BTCMap (free), Overpass (free), Wikimedia (CORS enabled with `origin=*`)
- Fallback FX rates updated to Mar 2026 values
- Fallback metals prices updated to Mar 2026 values (Gold ~$3,100, Silver ~$34, Platinum ~$990)
- No hardcoded localhost or dev-only URLs — all production-ready

### Reviews Layout Fix
- `groups-list-view` and `reviews-view` changed to `flex:1` (was `height:100%`) — prevents bottom content being clipped by subtab-row height
- Desktop: `social-subtab-row` positioned absolutely at top; screen gets `padding-top:47px` to preserve side-by-side groups layout
- `server2.js` added for running parallel test server on port 3001

## Latest Changes (2026-03-20)

### Affiliate Links — Full Integration
- **UTM tracking** added to every partner URL (`?utm_source=kipita&utm_medium=app`) so clicks are attributable in analytics:
  - Home → Book Transport: Skyscanner, Booking.com, Rentalcars, CruiseCritic, Uber, Lyft
  - Settings → Partners: BTC Map, Booking.com, Skyscanner
- **Perks / Deals modal** — each perk now has a **"Claim Deal →"** button linking to the partner site with campaign UTM:

| Partner | Code | Discount | Campaign UTM |
|---------|------|----------|-------------|
| Skyscanner | KIPITA10 | 10% off flights | `utm_campaign=KIPITA10` |
| Booking.com | KIPITAGENI | Genius L2 — up to 20% off | `utm_campaign=KIPITAGENI` |
| NomadList Pro | KIPITANOMAD | 3 months free | `utm_campaign=KIPITANOMAD` |
| Airalo eSIM | KIPITA5 | 5% off eSIM plans | `utm_campaign=KIPITA5` |
| ClassPass | KIPITAFIT | First month free | `utm_campaign=KIPITAFIT` |
| NordVPN | KIPITAVPN | 70% off 2-year plan | `utm_campaign=KIPITAVPN` |

### Map — Popup & Filter Overhaul (commit 49a43c3)
- **Rich OSM popups** — markers now show description, opening hours, phone (tap-to-call), Website or Directions button
- No more "Open in Google Maps" as the primary CTA
- **BTC source toggle removed** — BTC pill shows all BTCMap merchants directly
- **Cash App** added as its own filter pill (💚) replacing Nomad Hub

---

## Earlier Session Highlights (Mar 2026)

### Live Data — All Sections Pull Real APIs
| Section | API | Data |
|---------|-----|------|
| Wallet prices | CoinGecko | BTC, ETH, SOL (live + 24h %) |
| Gold/Silver/Platinum | metals.live | Spot prices in USD |
| Forex | open.er-api.com | EUR, GBP, JPY, CNY vs USD |
| Weather | Open-Meteo | Temperature + conditions |
| Nearby places | Overpass API | Real food/cafe/shop/ATM within 2km |
| BTC merchants | BTCMap API | Up to 500 merchants, 50km radius |
| IP location fallback | ip-api.com | City-level when GPS denied |
| Destination photos | Wikimedia | 800px hero images |

### AI — Context-Aware Trip Planning
- `buildAiContext()` reads user location, weather, live BTC price, and upcoming trips
- Trip-aware responses: checks saved trips before giving generic answers
- Plan Trip: auto-fills destination, dates, and day-by-day notes; live photo preview as you type
- "Add to Trips" flow from AI response

### Maps Tab
- Leaflet.js map with GPS auto-request on load
- Address search bar → Nominatim geocoding → camera re-centers
- 5 filter pills: ₿ BTC | 💚 Cash App | 🍜 Food | ☕ Cafe | 🛍 Shops
- Hover tooltip on every marker: place photo + name
- Real Overpass API fallback for food/cafe/shop layers

### Motion Design
- `@keyframes fadeSlideUp` — spring card animations
- `@keyframes fadeIn` — screen transitions
- `@keyframes shimmer` — loading skeletons
- Staggered delays on destination and category cards

### Trips Section
- Large 200px hero photo per trip card (Wikimedia or gradient fallback)
- Trip detail: 260px hero with gradient overlay + status badge
- "Plan with Kipita AI" banner in trips tab
- Recreate cancelled trips; Mark complete flow

### Home
- "Kipita AI" full-width gradient button → AI tab
- Quick actions: Translate, Perks/Deals, Plan Trip, Weather, Flights, Hotels, Car Rental, Cruise, Uber, Lyft
- Upcoming trips mini-cards below AI button

### Wallet
- Live crypto: BTC, ETH, SOL
- Live metals: Gold (XAU), Silver (XAG), Platinum (XPT)
- Live forex: EUR, GBP, JPY, CNY
- Currency converter with swap button
- Wallet map: BTCMap merchants near user

### Social / Groups
- Travel groups with chat interface
- Nearby travelers list
- Create group flow

---

## Tech Stack
- **Frontend**: Vanilla JS SPA, Leaflet.js, Material Symbols, Montserrat
- **Storage**: localStorage via `LS.get/set` wrapper
- **Maps**: OpenStreetMap tiles, BTCMap API, Overpass API
- **PWA**: `manifest.json` + `sw.js` (cache-first static, network-first API)
- **Server**: Node.js `server.js` on port 3000

---

## Next Steps (Planned)
- [ ] Replace UTM placeholders with real partner affiliate IDs
- [ ] Gemini API live AI integration
- [ ] User auth (Firebase or Supabase)
- [ ] Destinations tab (currently hidden — future build)
- [ ] Deploy to production (Vercel / Netlify / Firebase Hosting)
