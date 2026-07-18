# BluePath 1.4 Security and Integration Release

## Route ownership isolation

`POST /api/v1/routes/simulate` now resolves both `routeId` and `nodeId` through the authenticated user's `RoutePlan.user_id`. A foreign route, a foreign node, or a node that does not belong to the requested route returns 404 without exposing route data.

## Signed one-time exhibit QR

An administrator issues a short-lived QR payload through `POST /api/v1/admin/missions/qr-token`. The QR JSON contains:

- `exhibitCode`
- `exhibitTitle`
- `sessionId`
- `issuedAt`
- `expiresAt`
- `nonce`
- `signature`

The signature is HMAC-SHA256 using the server-only `QR_SIGNING_SECRET`. Mission generation binds the nonce to exactly one user's mission. Mission verification checks the signature, issued record, exhibit, session, expiry, mission binding, and unused nonce before atomically consuming it.

Example administrator request:

```bash
curl -sS -X POST "$BLUEPATH_API_BASE_URL/api/v1/admin/missions/qr-token" \
  -H "Authorization: Bearer $ADMIN_ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"exhibitCode":"submersible","exhibitTitle":"잠수정 전시","validMinutes":10}'
```

Render the returned `qrJson` string as the physical exhibit QR. Do not place `QR_SIGNING_SECRET` in the Android app.

## Idempotent mission verification

- `completionNote` requires at least 10 characters after trimming.
- A verified mission returns the existing result with `newlyVerified=false`.
- The server creates the completion event only on the first verification.
- Android applies skill gains, records learning, and completes the route node only when `newlyVerified=true`.
- Badge activity is locally idempotent.

## Automatic rerouting

A daily WorkManager task checks for at least three inactive days, asks the server to pre-generate a pending shorter route, and sends a notification. The current active route is not replaced until the learner accepts the prepared route in the app. Manual rerouting remains available and is labeled as manual.

## Operational education outcomes

`POST /api/v1/program-participation` stores one record per user and program with enrollment, attendance, completion, and optional pre/post assessment scores. The admin outcome endpoint now reports:

- unique enrolled users as `participants`
- attendance and completion rates from timestamps/status
- average gain only from paired pre/post assessments
- the number of assessment pairs

Route-node creation and event counts remain available separately as `prototypeProgramSignals`; they are not presented as participant counts.

## LLM submission gate and integration test

Set real server credentials in the deployment environment and enable the required gates:

```dotenv
LLM_BASE_URL=https://provider.example/v1
LLM_API_KEY=...
LLM_MODEL=...
EMBEDDING_MODEL=...
WEB_SEARCH_PROVIDER=tavily
WEB_SEARCH_API_KEY=...
REQUIRE_LLM=true
REQUIRE_EMBEDDINGS=true
REQUIRE_WEB_SEARCH=true
```

Production startup fails when a required integration is missing. Run the real integration assertion separately:

```bash
RUN_LLM_INTEGRATION=1 pytest -q backend/tests/test_llm_integration.py
```

The test requires route responses with `generatedBy=llm_grounded` and mission responses with `generatedBy=llm_guardrailed`.

## Clean submission archive

```bash
./scripts/create_clean_submission.sh
```

The script excludes `.git`, `.idea`, `.gradle`, build outputs, macOS metadata, `.env`, `local.properties`, and `developer.properties`. Rotate any JWT or database credential that was previously included in an external archive.
