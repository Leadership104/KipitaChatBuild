# GitHub Note - 2026-03-08 Live API Check

## Dwaat endpoint probes
- api.dwaat.com root: ERR (The underlying connection was closed: An unexpected error occurred on a receive.)
- affiliates.php GetAllList: ERR (The underlying connection was closed: An unexpected error occurred on a receive.)
- airport.json: ERR (The underlying connection was closed: An unexpected error occurred on a receive.)

## Private import status
- Postman collection loaded; top-level variables=0
- local.properties key status:
  - GOOGLE_PLACES_API_KEY=SET
  - MAPS_API_KEY=SET
  - OPENAI_API_KEY=SET
  - CLAUDE_API_KEY=SET
  - GEMINI_API_KEY=SET

## Dwaat URL config
- DWAAT_BASE_URL is configured
- DWAAT_FALLBACK_BASE_URL is configured

## Notes
- Key values are intentionally not printed to keep secrets private.
- App runtime can only use live APIs whose keys are present in local.properties/keystore.
