# Kipita Web App — API Integration Notes

## Live APIs (Free, No Key Required)

All of the following are already integrated and working in production:

| API | Purpose | Endpoint |
|-----|---------|----------|
| **BTCMap** | Bitcoin/Lightning merchant locations | `api.btcmap.org/v2/elements` |
| **Overpass API** | Real nearby places (food, cafes, shops, ATMs) | `overpass-api.de/api/interpreter` |
| **Open-Meteo** | Live weather + 7-day forecast | `api.open-meteo.com/v1/forecast` |
| **ip-api.com** | IP-based city location fallback | `ip-api.com/json` |
| **Nominatim** | Address search + reverse geocoding | `nominatim.openstreetmap.org` |
| **CoinGecko** | Live BTC / ETH / SOL prices + 24h % | `api.coingecko.com/api/v3/simple/price` |
| **metals.live** | Gold / Silver / Platinum spot prices | `api.metals.live/v1/spot` |
| **open.er-api.com** | Live forex exchange rates (USD base) | `open.er-api.com/v6/latest/USD` |
| **Wikimedia** | Destination hero photos (800px) | `en.wikipedia.org/w/api.php` |

---

## APIs Requiring Keys (Not Yet Active)

These services provide richer place data, reviews, and advisory content but require API keys. Since Kipita is a **client-side SPA**, keys cannot be safely embedded in the public frontend. A **server-side proxy endpoint** is required before these can be activated.

| API | Purpose | Key Type | Notes |
|-----|---------|----------|-------|
| **Google Places API (New)** | Rich place data, reviews, opening hours, photos | Google Cloud (paid after free tier) | Requires backend proxy to protect key; `X-Goog-Api-Key` header |
| **Yelp Fusion API** | Restaurant/business ratings, reviews, photos | Yelp Developer key (free tier available) | Requires `Authorization: Bearer` header; CORS blocked on browser calls |
| **Dwaat API** | Travel advisories, planner sections, places | Account credentials (login/password) | Endpoint: `api.dwaat.com`; requires `action` field in POST body |

### How to Activate Google Places / Yelp
1. Create a lightweight proxy server (Node/Express, Cloudflare Worker, Vercel Edge Function)
2. Proxy receives requests from the frontend and forwards with the API key attached server-side
3. The frontend calls your proxy URL instead of the third-party API directly
4. Example proxy endpoint: `GET /api/places?query=restaurants&lat=13.75&lng=100.5`

---

## Google Sign-In Setup

Google OAuth is wired up via Google Identity Services (GIS). To activate:

1. Go to [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Credentials
2. Create an **OAuth 2.0 Client ID** (Web application type)
3. Add your domain to **Authorized JavaScript origins** (e.g. `https://leadership104.github.io`)
4. Copy the **Client ID** and set it in `index.html`:

```html
<script>window.KIPITA_GOOGLE_CLIENT_ID = "YOUR_CLIENT_ID_HERE.apps.googleusercontent.com";</script>
```

---

## Dwaat API — Available Endpoints

The full Dwaat integration is built into the Android app. Web endpoints available:

```
POST https://api.dwaat.com/
Required field: action = "Login" | "Register" | "advisorySections" | "getWeatherAdvisory"
               | "restrictions" | "plannerSections" | "placesSections" | "group"
               | "bookmarks" | "affiliates" | "imageSearch" | "airport.json" (GET)
```

All responses are wrapped: `{ status, message, data }`.

---

## Next Steps

- [ ] Set up server-side proxy (Vercel/Cloudflare) for Google Places + Yelp keys
- [ ] Add `KIPITA_GOOGLE_CLIENT_ID` to activate Google Sign-In
- [ ] Integrate Dwaat advisory sections into the Safety/Advisory tab
- [ ] Replace fallback FX/metals prices as APIs evolve
