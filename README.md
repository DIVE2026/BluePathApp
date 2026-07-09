# BluePath — 스마트 해도 AI 모바일 앱

전 연령 사용자가 해양 콘텐츠를 학습하고, 퀴즈 기반 티어 승급을 통해 성장하며, 교육 일정·이벤트·NCS 직무 로드맵을 AI로 추천받는 모바일 해양 인재 양성 플랫폼 prototype입니다.

## Android Studio 실행 방법

1. Android Studio에서 `BluePathApp` 디렉토리를 엽니다.
2. Gradle Sync를 실행합니다.
3. 에뮬레이터 또는 실제 Android 기기에서 Run 합니다.

네트워크 연결은 YouTube 링크 실행에만 사용됩니다. 앱의 핵심 데이터와 추천 로직은 오프라인으로 동작합니다.

## 구현된 기능

- 온보딩 해양 인재 DNA 진단
- 사용자 프로필 저장: 연령대, 관심 분야, 목표, 수준, 페르소나
- 브론즈/실버/골드/플래티넘/다이아 티어와 XP 게이지
- 난도별 해양 영상 추천: 하/중/상 + 권장 티어
- 영상 완료 처리 및 XP 증가
- 승급 퀴즈: 티어별 난도, 정답 해설, XP 보상
- 교육 프로그램 스케줄 추천
- 이벤트/영화/공연/체험 추천
- NCS 기반 진로 찾기: 직무, 필요 역량, 관련 근무지
- BluePath AI Agent: 오프라인 룰 기반 상담
- 마이페이지: 현재 티어, 학습 기록, 찜, 프로필 초기화
- DB 설계 초안: `backend/schema.sql`
- YouTube Data API 수집 스크립트: `scripts/fetch_youtube_videos.py`

## 앱 구조

```text
app/src/main/java/com/bluepath/app
├── MainActivity.java                  # 모든 화면을 구성하는 prototype Activity
├── data/DataRepository.java           # seed 데이터: 영상, 교육, 이벤트, 진로, 퀴즈
├── model/*.java                       # 데이터 모델
├── storage/UserStore.java             # SharedPreferences 기반 유저 상태 저장
└── util/RecommendationEngine.java     # 추천 점수, 페르소나, Agent 답변 로직
```

## 데이터 반영 내용

첨부 샘플데이터에서 다음 개념을 앱에 반영했습니다.

- 국립해양박물관 교육 운영 데이터: 교육명, 대상, 시작/종료일, 참가방법, 교육내용
- 국립해양박물관 교육행사 운영 데이터: 영화, 매직쇼, 음악회, 4D 체험 등 이벤트
- 한국해양수산연수원 교육 과정 데이터: 해기사 면허취득, 기초안전교육 등 전문 과정
- 해양기관 현황 데이터: 해운회사, 여객선사, 항만물류업체, 정부기관, 선급 등 근무지 유형
- 해양직무 NCS 데이터: 선위결정, 항해당직, 선박조종, 항해장비운용, 비상대응 등 진로 역량

## 난도별 영상 seed

YouTube 웹페이지 무단 스크래핑 대신, 공식·공공기관 채널 검색 결과 기반으로 seed를 구성했습니다.

- 하: 어린이/유아/초등 대상 해양환경·해양생물·해양문화 영상
- 중: 국립기관 교육 영상, 해양생물자원, 해양정화 활동, 박물관 탐방
- 상: 방제대응센터, 해양환경 특강, 교육 플레이리스트 등 심화·직무형 콘텐츠

운영 단계에서는 `YOUTUBE_API_KEY`를 발급한 뒤 아래 명령으로 재수집할 수 있습니다.

```bash
export YOUTUBE_API_KEY="YOUR_KEY"
python scripts/fetch_youtube_videos.py
```

## 실제 서비스화 시 권장 확장

1. FastAPI/NestJS 백엔드 생성
2. PostgreSQL + pgvector 또는 Supabase 연결
3. 콘텐츠/교육/이벤트/퀴즈 관리자 대시보드 구현
4. YouTube Data API, 기관 CSV/Excel 업로드 파이프라인 연결
5. LLM API + RAG로 AI Agent 교체
6. Firebase Cloud Messaging으로 교육 일정·시험·퀴즈 알림 추가
7. 미성년자 보호자 동의, 개인정보 최소 수집, 로그 익명화 적용
