# BluePath 1.4 Voyage Twin

## Purpose

Voyage Twin turns BluePath into an end-to-end marine talent navigator. It connects a learner's current mastery to a target career through a sequence of online learning, museum or training experience, diagnostic assessment, applied project, and NCS-oriented career exploration.

## Learner APIs

- `POST /api/v1/routes/plan`
- `POST /api/v1/routes/simulate`
- `POST /api/v1/routes/reroute`
- `POST /api/v1/routes/outcomes`
- `POST /api/v1/missions/generate`
- `POST /api/v1/missions/verify`

## Institution APIs

- `GET /api/v1/admin/analytics/outcomes`
- `POST /api/v1/admin/program-draft`

## LLM responsibility boundary

The route engine calculates topic fit, prerequisites, ordering, expected mastery gain, career-readiness gain, duration, and rerouting constraints. These values remain deterministic and auditable.

When an LLM is configured, it may:

1. Explain why the fixed route order is appropriate.
2. Rewrite recommendation reasons using retrieved institutional evidence.
3. Personalize safe family roles and a joint exhibit mission.
4. Explain a simulation without changing its numbers.
5. Draft editable 30, 60, and 90 minute institution programs with NCS links and measurement plans.

When the LLM is disabled or returns invalid JSON, every feature falls back to a deterministic implementation.

## Data and persistence

The backend adds:

- `route_plans`
- `route_nodes`
- `mission_evidence`
- `route_outcome_events`

The Android store synchronizes the learner's target career, route type, mission badges, last route activity, and topic mastery.

## Outcome control center

The administrator console measures route generation, rerouting, mission verification, program completion events, follow-up learning, career exploration, and drop-off stages. It also generates data-informed operational suggestions and editable program drafts.

## Verification

Run:

```bash
pytest -q backend/tests/test_api.py backend/tests/test_dataset.py
```

For Android, run the normal Gradle build in an environment with the Android SDK and Gradle 8.9 distribution available:

```bash
./gradlew :app:assembleDebug
```
