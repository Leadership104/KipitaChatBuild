# KipitaChatBuild

Kipita Android foundation with:
- Verified government travel/safety ingestion and caching
- Bitcoin merchant aggregation (BTCMap + Cash App schema)
- Nomad-style place intelligence (cost, internet, safety, walkability, weather)
- Interactive animated map with offline caching controls
- Currency conversion utility
- Group trip chat (up to 10 people) with one main AI planner
- Multi-LLM assistant routing (OpenAI, Claude, Gemini)
- Motion-first Compose map, AI, chat, and wallet surfaces

## Firebase flavor integration
Configured Android package IDs per flavor:
- `prod`: `com.mytum`
- `dev`: `com.mytum.dev`
- `staging`: `com.mytum.staging`

Place the **real** Firebase config files at:
- `app/src/prod/google-services.json`
- `app/src/dev/google-services.json`
- `app/src/staging/google-services.json`

Template stubs are included as:
- `google-services.json.template`

⚠️ Never commit service-account private keys into this repository. Use secure secret storage / CI secret managers.

- In-house error logging with support submission to info@kipita.com
