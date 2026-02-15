# KipitaChatBuild

Production-oriented Android foundation for Kipita with:
- Verified government travel/safety ingestion and caching
- Bitcoin merchant aggregation (BTCMap + Cash App schema)
- Nomad-style place intelligence (cost, internet, safety, walkability, weather)
- Interactive animated map with offline caching controls
- Currency conversion utility
- Group trip chat (up to 10 people) with one main AI planner
- Multi-LLM assistant routing (OpenAI, Claude, Gemini)
- Motion-first Compose map, AI, chat, and wallet surfaces
- In-house error logging and support submission to `info@kipita.com`

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

## Gemini 2.5 Flash-Lite foundation
- Gemini provider is configured to use `gemini-2.5-flash-lite` for low-cost/free-tier usage.
- System instruction is set to a "Kipita Discovery Concierge" profile for concise mobile responses.
- Structured output mode is supported (`application/json`) for UI/action parsing.
- Grounding tool flag is wired in request payload for map/business-aware responses.
- Local usage limiter foundation enforces free-tier style request caps (RPM/RPD) in app logic.
- API key is intentionally left as placeholder via `LlmTokenProvider` so you can inject later securely.
- Backend proxy recommended for mobile web/public clients; do not expose raw Gemini keys in client code.


## Official Kipita experience components
- Smart Navigation
- Trip Planning
- Safety First
- Travel Community


## Place newly added Firebase JSON files
If you have new Firebase config JSON files, place them with:

```bash
scripts/place_firebase_json.sh /path/prod-google-services.json /path/dev-google-services.json /path/staging-google-services.json
```

This copies files to:
- `app/src/prod/google-services.json`
- `app/src/dev/google-services.json`
- `app/src/staging/google-services.json`
