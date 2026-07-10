# BluePath — Smart Ocean AI Mobile App

BluePath is a prototype mobile platform for cultivating future ocean talent. It helps users of all ages learn ocean-related content, grow through quiz-based tier promotion, and receive AI recommendations for educational schedules, events, and NCS-based career roadmaps.

## How to Run in Android Studio

1. Open the `BluePathApp` directory in Android Studio.
2. Run Gradle Sync.
3. Run the app on an emulator or a physical Android device.

Network access is only used to open YouTube links. The app's core data and recommendation logic work offline.

## Implemented Features

- Onboarding ocean talent DNA diagnosis
- User profile storage: age group, interests, goals, level, and persona
- Bronze/Silver/Gold/Platinum/Diamond tiers with XP gauge
- Ocean video recommendations by difficulty: beginner/intermediate/advanced + recommended tier
- Video completion handling and XP increase
- Promotion quizzes: tier-based difficulty, answer explanations, and XP rewards
- Educational program schedule recommendations
- Event/movie/performance/experience recommendations
- NCS-based career discovery: job roles, required competencies, and related workplaces
- BluePath AI Agent: offline rule-based counseling
- My Page: current tier, learning history, favorites, and profile reset
- Initial database design: `backend/schema.sql`
- YouTube Data API collection script: `scripts/fetch_youtube_videos.py`

## App Structure

```text
app/src/main/java/com/bluepath/app
├── MainActivity.java                  # Prototype Activity that builds all screens
├── data/DataRepository.java           # Seed data: videos, education, events, careers, quizzes
├── model/*.java                       # Data models
├── storage/UserStore.java             # User state storage based on SharedPreferences
└── util/RecommendationEngine.java     # Recommendation scoring, persona, and Agent response logic
```

## Reflected Data Concepts

The following concepts from the attached sample datasets were reflected in the app:

- National Maritime Museum education operation data: program names, target audiences, start/end dates, participation methods, and educational content
- National Maritime Museum education/event operation data: movies, magic shows, concerts, 4D experiences, and other events
- Korea Institute of Maritime and Fisheries Technology course data: maritime officer license acquisition, basic safety training, and other professional courses
- Maritime institution status data: shipping companies, passenger ferry operators, port logistics companies, government agencies, classification societies, and other workplace types
- Maritime job NCS data: position fixing, navigation watchkeeping, ship handling, navigation equipment operation, emergency response, and other career competencies

## Video Seed Data by Difficulty

Instead of unauthorized scraping of YouTube web pages, the seed data was built based on search results from official and public institution channels.

- Beginner: videos for children, preschoolers, and elementary students about the marine environment, marine life, and maritime culture
- Intermediate: educational videos from national institutions, marine bioresources, marine cleanup activities, and museum tours
- Advanced: oil spill response centers, marine environment lectures, educational playlists, and other advanced or job-oriented content

For production use, issue a `YOUTUBE_API_KEY` and run the following command to collect updated data:

```bash
export YOUTUBE_API_KEY="YOUR_KEY"
python scripts/fetch_youtube_videos.py
```

