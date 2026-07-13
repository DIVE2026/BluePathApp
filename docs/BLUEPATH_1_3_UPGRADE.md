# BluePath 1.3 Feature Upgrade

This document summarizes the configuration and implementation requirements for integrating the marine community, unified tier home, natural-language resource search, AI career counseling, profile images, and activity heatmap into the existing BluePath project.

## User Features

- Added `AI Career Counseling` to the top of the sidebar and merged the existing career and AI counseling screens into a single experience.
- Renamed `Learning` to `Learning Resources` and added `Videos` and `Papers` sub-tabs. Existing resources remain under the Videos tab, while the Papers tab is provided as an empty placeholder for future content.
- Added an LLM-powered natural-language search field directly below the Learning Resources and Schedule introductions.
- Added free-discussion and Q&A boards to the `Marine Community`. The community supports posts, comments, nested replies, eight emoji reactions, and user following.
- Users must enter a 2–20 character nickname during sign-up and complete a duplicate check. The server performs an additional case-insensitive validation.
- Unified the tier displays on Home and My Page. The interface now shows the user's nickname, profile image, follower and following counts, a large shield styled with the tier color, and a progress gauge.
- Added a GitHub-style light-blue activity heatmap to the Home screen, showing video, paper, and community activity from the past year.
- Users can upload JPEG, PNG, or WebP profile images of up to 5 MB from My Page. Before an image is uploaded, a nickname-based default avatar is displayed.
- Added a full-screen shield, particle, and light-ray animation that plays when the user is promoted to a new tier.

## Backend Integration

When FastAPI starts, it performs a compatibility migration that adds `nickname` and `profile_image_url` to the existing `users` table. SQLAlchemy also creates the new community-related tables.

In production, the following SQL migration may be executed separately before deployment:

```bash
psql "$DATABASE_URL" -f backend/migrations/002_community_and_profiles.sql
```

If the existing PostgreSQL database contains nicknames that differ only by letter case, clean up those records before creating the `uq_users_nickname_ci` index.

Profile images are stored in `backend/uploads/` inside the API container. In production, mount this path as a persistent volume or replace it with object storage.

## Commercial LLM and Real-Time Web Search

AI Career Counseling combines the following sources when generating responses:

1. BluePath internal knowledge chunks and application data
2. An OpenAI-compatible Chat Completions API
3. Optional Tavily or Brave Search results
4. Public HTML body extraction with private-network and loopback addresses blocked

Configure the LLM and web-search provider in `backend/.env`:

```dotenv
LLM_BASE_URL=https://your-openai-compatible-provider.example.com
LLM_API_KEY=replace-me
LLM_MODEL=your-model-name

# tavily or brave
WEB_SEARCH_PROVIDER=tavily
WEB_SEARCH_API_KEY=replace-me
WEB_SEARCH_MAX_RESULTS=5
WEB_SEARCH_TIMEOUT_SECONDS=15
WEB_CRAWL_MAX_CHARS=12000
```

Verify that both `llmEnabled` and `liveWebSearchEnabled` are set to `true` in the response from `GET /health`.

If the web-search key is missing or the provider request fails, the application safely falls back to internal RAG and offline counseling. For laws, certifications, recruitment information, pricing, and schedules, users should be advised to verify the latest information through the official sources included in the response.

## Main API Endpoints

- `GET /api/v1/auth/nickname-available`
- `POST /api/v1/ai/search`
- `GET /api/v1/dashboard`
- `POST /api/v1/profile/image`
- `GET|POST /api/v1/community/posts`
- `POST /api/v1/community/posts/{post_id}/comments`
- `POST /api/v1/community/reactions`
- `POST /api/v1/community/users/{user_id}/follow`

## Validation

```bash
python -m pytest -q backend/tests/test_api.py
python -m compileall -q backend/app backend/tests
./gradlew test assembleDebug
```

The Android build requires network access to download the Gradle distribution and Maven dependencies, JDK 17, and Android SDK 35.
