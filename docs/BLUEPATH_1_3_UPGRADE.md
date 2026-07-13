# BluePath 1.3 기능 업그레이드

이 문서는 해양 커뮤니티, 통합 티어 홈, 자연어 자료 검색, AI 진로 상담, 프로필 사진과 활동 히트맵을 기존 BluePath 프로젝트에 반영할 때 필요한 설정을 정리합니다.

## 사용자 기능

- 사이드바 최상단에 `AI 진로 상담`을 배치하고 기존 진로·AI 상담 화면을 하나로 통합했습니다.
- `학습`을 `학습 자료`로 변경하고 `영상`·`논문` 하위 탭을 추가했습니다. 기존 자료는 영상 탭에 유지되며 논문 탭은 비어 있는 준비 화면입니다.
- 학습 자료와 일정 소개 바로 아래에 LLM 기반 자연어 검색 입력란을 추가했습니다.
- `해양 커뮤니티`에 자유 게시판과 질문 게시판을 추가했습니다. 글, 댓글, 대댓글, 8종 이모지 공감과 팔로우를 지원합니다.
- 회원가입 시 2~20자의 닉네임을 입력하고 중복 확인을 완료해야 합니다. 서버도 대소문자를 구분하지 않고 다시 검증합니다.
- 홈과 MY의 티어를 하나로 통합했습니다. 닉네임, 프로필 사진, 팔로워·팔로잉 수, 티어 색상의 큰 방패와 진행 게이지를 표시합니다.
- 홈에 최근 1년간 영상·논문 및 커뮤니티 활동을 표시하는 GitHub 방식의 하늘색 활동 히트맵을 추가했습니다.
- MY에서 JPEG, PNG, WebP 프로필 이미지를 최대 5 MB까지 업로드할 수 있습니다. 업로드 전에는 닉네임 기반 기본 아바타가 표시됩니다.
- 승급 시 전체 화면 방패·파티클·광선 애니메이션을 재생합니다.

## 백엔드 적용

FastAPI가 시작될 때 기존 `users` 테이블에 `nickname`, `profile_image_url`을 추가하는 호환 마이그레이션을 수행하고 SQLAlchemy가 신규 커뮤니티 테이블을 생성합니다. 운영 환경에서는 배포 전에 다음 SQL을 별도로 실행해도 됩니다.

```bash
psql "$DATABASE_URL" -f backend/migrations/002_community_and_profiles.sql
```

기존 PostgreSQL 인스턴스에 대소문자만 다른 닉네임이 이미 존재하면 `uq_users_nickname_ci` 인덱스 생성 전에 데이터를 정리해야 합니다.

프로필 이미지는 API 컨테이너의 `backend/uploads/`에 저장됩니다. 운영 환경에서는 이 경로를 영속 볼륨으로 마운트하거나 객체 스토리지로 교체하십시오.

## 상용 LLM 및 실시간 웹 검색

AI 진로 상담은 다음 근거를 합쳐 답변합니다.

1. BluePath 내부 지식 청크와 앱 데이터
2. OpenAI 호환 Chat Completions API
3. 선택적으로 Tavily 또는 Brave Search 검색 결과
4. 사설망·루프백 주소를 차단한 공개 HTML 본문 추출

`backend/.env`에 LLM과 웹 검색 공급자를 설정합니다.

```dotenv
LLM_BASE_URL=https://your-openai-compatible-provider.example.com
LLM_API_KEY=replace-me
LLM_MODEL=your-model-name

# tavily 또는 brave
WEB_SEARCH_PROVIDER=tavily
WEB_SEARCH_API_KEY=replace-me
WEB_SEARCH_MAX_RESULTS=5
WEB_SEARCH_TIMEOUT_SECONDS=15
WEB_CRAWL_MAX_CHARS=12000
```

`GET /health`의 `llmEnabled`와 `liveWebSearchEnabled`가 모두 `true`인지 확인하십시오. 웹 검색 키가 없거나 공급자 호출이 실패하면 앱 내부 RAG와 오프라인 상담으로 안전하게 폴백합니다. 법령, 자격, 채용, 가격과 일정은 답변에 포함된 공식 출처에서 최신 내용을 다시 확인하도록 안내합니다.

## 주요 API

- `GET /api/v1/auth/nickname-available`
- `POST /api/v1/ai/search`
- `GET /api/v1/dashboard`
- `POST /api/v1/profile/image`
- `GET|POST /api/v1/community/posts`
- `POST /api/v1/community/posts/{post_id}/comments`
- `POST /api/v1/community/reactions`
- `POST /api/v1/community/users/{user_id}/follow`

## 검증

```bash
python -m pytest -q backend/tests/test_api.py
python -m compileall -q backend/app backend/tests
./gradlew test assembleDebug
```

Android 빌드는 Gradle 배포 파일과 Maven 의존성을 내려받을 네트워크, JDK 17, Android SDK 35가 필요합니다.
