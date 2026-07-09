package com.bluepath.app.data;

import com.bluepath.app.model.CareerItem;
import com.bluepath.app.model.ContentItem;
import com.bluepath.app.model.EventItem;
import com.bluepath.app.model.ProgramItem;
import com.bluepath.app.model.QuizQuestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataRepository {
    public static List<ContentItem> contents() {
        List<ContentItem> list = new ArrayList<>();
        // 공식·공공기관 채널 검색 결과 기반 seed. scripts/fetch_youtube_videos.py로 YouTube Data API 재수집 가능.
        list.add(new ContentItem("v-koem-01", "[어린이 교육] 나라와 누리의 해양환경 이야기 1: 바다가 아파요", "해양환경공단", "https://www.youtube.com/watch?v=d2p-_bOLuFc", "하", "브론즈", "해양환경", "해양환경 교육", 6));
        list.add(new ContentItem("v-koem-02", "[어린이 교육] 나라와 누리의 해양환경 이야기 2: 지구가 더워지고 있어요", "해양환경공단", "https://www.youtube.com/watch?v=Vq_bja356o8", "하", "브론즈", "해양환경", "해양환경 교육", 6));
        list.add(new ContentItem("v-koem-03", "[어린이 교육] 나라와 누리의 해양환경 이야기 3: 전 세계가 바다를 청소하고 있어요", "해양환경공단", "https://www.youtube.com/watch?v=N5EUkmslDzI", "하", "브론즈", "해양환경", "해양환경 교육", 6));
        list.add(new ContentItem("v-mof-01", "아름다운 바다 속에 이렇게 많은 쓰레기가 있다고?", "해양수산부", "https://www.youtube.com/watch?v=TxZtHAJF3X4", "하", "브론즈", "해양환경", "해양환경 교육", 5));
        list.add(new ContentItem("v-mof-02", "해랑이가 소개하는 유아 해양교육 놀이교재", "해양수산부/한국해양재단", "https://www.youtube.com/watch?v=Zs-yZH5CkrE", "하", "브론즈", "해양문화", "해양교육 기획", 5));
        list.add(new ContentItem("v-mof-03", "해랑이가 초등학교에 간 이유는?! 바다친구들을 도와줘!", "해양수산부", "https://www.youtube.com/watch?v=gHGMXL0ve7I", "하", "브론즈", "해양생물", "해양환경 교육", 5));

        list.add(new ContentItem("v-science-01", "국립해양과학관 온앤오프 교육: 해양정화활동과 업사이클링", "국립해양과학관", "https://www.youtube.com/watch?v=5k_kwf5B30Q", "중", "실버", "해양환경", "해양환경 교육", 12));
        list.add(new ContentItem("v-mabik-01", "국립해양생물자원관 교육기부: 해양생명자원의 가치", "국립해양생물자원관", "https://www.youtube.com/watch?v=SARq0b_A7E8", "중", "실버", "해양생물", "해양생태 해설", 10));
        list.add(new ContentItem("v-koem-04", "해양환경 지식나눔 온라인 라이브 특강 요약", "해양환경공단", "https://www.youtube.com/watch?v=xidKs9KNrOg", "중", "실버", "해양환경", "해양환경 정책", 18));
        list.add(new ContentItem("v-inmm-01", "국립인천해양박물관 홍보영상 및 전시 탐방", "국립인천해양박물관", "https://www.youtube.com/@inmm_official", "중", "실버", "해양문화", "전시·교육 기획", 9));

        list.add(new ContentItem("v-koem-05", "해양환경공단 방제대응센터 탐방", "해양환경공단", "https://www.youtube.com/watch?v=GzMBtdH4-bU", "상", "골드", "해양안전", "방제·안전관리", 15));
        list.add(new ContentItem("v-playlist-01", "해양환경교육 영상 플레이리스트", "해양환경공단", "https://www.youtube.com/playlist?list=PL9uTkm0J3zA_tiQftfRkYmfenYeFK5wj-", "상", "골드", "해양환경", "해양환경 정책", 60));
        list.add(new ContentItem("v-edu-01", "국립인천해양박물관 교육 프로그램 플레이리스트", "국립인천해양박물관", "https://www.youtube.com/playlist?list=PLvBo1qqlBWtxbwH7DyysyvoaoZoN_F_cU", "상", "골드", "해양문화", "해양교육 기획", 45));
        return list;
    }

    public static List<ProgramItem> programs() {
        return Arrays.asList(
                new ProgramItem("p-001", "독도의 날 기념프로그램", "전체", "2025-10-25", "2025-10-25", "오프라인", "독도·해양문화", "활동지, 독도 엽서, 독도 만들기, 참여형 퀴즈로 구성된 체험 프로그램"),
                new ProgramItem("p-002", "2025 해양문화아카데미 ‘세상을 바꾼 바다X인물’", "성인", "2025-09-18", "2025-10-30", "온·오프라인", "해양문화", "바다를 매개로 역사적 인물과 세계사의 연결을 학습하는 성인 강좌"),
                new ProgramItem("p-003", "가족 교육 프로그램 ‘잠수정 이야기’", "가족", "2026-02-07", "2026-02-08", "오프라인", "선박·해양문화", "초등학생 고학년 포함 가족 대상 잠수정 내부 탐방형 교육"),
                new ProgramItem("p-004", "기획전시 [수군, 해전] 연계 대면프로그램", "가족", "2025-09-13", "2025-09-27", "오프라인", "해양역사", "활동지와 수군 투구 만들기 체험 중심 가족 프로그램"),
                new ProgramItem("p-005", "교과서 안 해양박물관: 바다를 지켜줘!", "어린이", "2025-04-21", "2025-09-23", "오프라인", "해양환경", "초등학생 고학년 대상 교과 연계 해양환경 교육"),
                new ProgramItem("p-006", "제3기 해양문화 교육사 양성 프로그램", "전문가", "2024-06-19", "2024-08-21", "오프라인", "해양교육", "관련 전공자와 교육 경험자를 대상으로 해양문화 교육 역량 양성"),
                new ProgramItem("p-007", "3급면허취득원격교육(상선항해)", "전문가", "2025-01-01", "2025-12-31", "온라인", "항해·자격", "해기사 면허시험 합격자를 위한 원격교육 과정"),
                new ProgramItem("p-008", "기초안전교육", "성인/직장인", "2025-01-06", "2025-01-10", "오프라인", "해양안전", "국제항해 승선자를 위한 선박 구조, 구명, 통신, 소화, 생존 이론 교육")
        );
    }

    public static List<EventItem> events() {
        return Arrays.asList(
                new EventItem("e-001", "월드베스트 매직 콘서트", "2012-07-09", "2012-07-31", "전체", "공연", "국가대표 마술사들이 펼치는 마술 공연"),
                new EventItem("e-002", "사운드 오브 매직아트", "2012-08-01", "2012-11-30", "전체", "공연", "사운드와 마술, 마임이 어우러진 퍼포먼스"),
                new EventItem("e-003", "해로와 미로의 시간탐험대", "2012-12-01", "2012-12-31", "전체", "어린이 공연", "마술과 뮤지컬이 어우러진 어린이 마술뮤지컬"),
                new EventItem("e-004", "[4D영상관] 4D 독도영상", "2012-08-15", "2012-08-15", "전체", "체험", "독도 주변 바다의 해양현상과 생태계를 실감형 영상으로 체험"),
                new EventItem("e-005", "무료영화상영 ‘해양가족극장’", "2012-09-08", "2012-09-29", "전체", "영화", "가족이 즐길 수 있는 해양·자연 테마 영화 상영"),
                new EventItem("e-006", "남극의 눈물 제작 체험담 강연", "2013-04-11", "2013-04-11", "전체", "강연", "해양·극지 다큐멘터리 제작 과정과 현장 경험 공유")
        );
    }

    public static List<CareerItem> careers() {
        return Arrays.asList(
                new CareerItem("c-001", "항해사", "항해·선박", "선박의 안전한 항해와 위치결정, 항해당직, 선박조종을 담당합니다.",
                        new String[]{"선위결정", "항해당직", "선박조종", "항해장비운용", "비상대응"},
                        new String[]{"해운회사", "여객선사", "해양수산연수원", "정부기관"}, "골드"),
                new CareerItem("c-002", "해양환경 교육 기획자", "해양환경", "해양오염, 생태계, 기후변화 이슈를 교육 콘텐츠와 체험 프로그램으로 설계합니다.",
                        new String[]{"해양환경 이해", "교육 콘텐츠 기획", "관람객 소통", "프로그램 평가"},
                        new String[]{"국립해양박물관", "해양환경공단", "해양교육기관", "유관기관·단체"}, "실버"),
                new CareerItem("c-003", "해양생태 해설사", "해양생물", "해양생물과 생태계를 대중에게 쉽게 설명하고 체험 교육을 운영합니다.",
                        new String[]{"해양생물 분류", "해양생태계 해설", "전시 해설", "안전한 체험 운영"},
                        new String[]{"박물관", "해양생물자원관", "교육기관", "전시기관"}, "실버"),
                new CareerItem("c-004", "선박안전관리자", "해양안전", "선박 운항 중 위험요소를 예방하고 비상대응 체계를 관리합니다.",
                        new String[]{"선박안전관리", "비상대응", "구명설비 운용", "소화·생존 이론"},
                        new String[]{"선급", "정부기관", "해운회사", "검수검정업체"}, "플래티넘"),
                new CareerItem("c-005", "항만물류 전문가", "항만·물류", "항만 물류 흐름을 관리하고 해운·항만 산업의 운영 효율을 높입니다.",
                        new String[]{"화물관리", "항만운영", "해운물류 이해", "데이터 기반 운영"},
                        new String[]{"한국항만물류업체", "해운회사", "국제해운대리점", "유관기관·단체"}, "골드")
        );
    }

    public static List<QuizQuestion> quizzes() {
        return Arrays.asList(
                new QuizQuestion("q-b-001", "브론즈", "해양환경", "바다 쓰레기를 줄이기 위한 행동으로 가장 적절한 것은?",
                        new String[]{"일회용품 사용 줄이기", "쓰레기를 모래에 묻기", "바다에 음식물 버리기", "플라스틱을 물에 씻어 버리기"}, 0, "일회용품 사용을 줄이고 분리배출하는 것이 해양쓰레기 감소에 도움이 됩니다."),
                new QuizQuestion("q-b-002", "브론즈", "독도·해양문화", "독도는 해양 영토와 해양 생태 교육에서 중요한 주제이다.",
                        new String[]{"O", "X"}, 0, "독도는 역사·영토·생태를 함께 학습할 수 있는 대표 해양 교육 주제입니다."),
                new QuizQuestion("q-b-003", "브론즈", "해양생물", "해양생태계를 이해할 때 가장 먼저 살펴볼 수 있는 것은?",
                        new String[]{"서식지와 먹이관계", "휴대폰 종류", "도시 교통량", "은행 금리"}, 0, "생물의 서식지와 먹이관계는 생태계 이해의 기본입니다."),

                new QuizQuestion("q-s-001", "실버", "항해", "선박의 위치를 정확히 측정하는 NCS 능력단위와 가장 가까운 것은?",
                        new String[]{"선위결정", "화물 판매", "음향 편집", "관광 홍보"}, 0, "선위결정은 육지물표, 천체, 전파항법장치 등을 활용해 선박 위치를 측정하는 능력입니다."),
                new QuizQuestion("q-s-002", "실버", "해양교육", "교육 프로그램 추천에서 연령·수준·관심사를 반영해야 하는 가장 큰 이유는?",
                        new String[]{"학습자에게 맞는 다음 경로를 제시하기 위해", "모든 사람에게 같은 영상을 보여주기 위해", "퀴즈를 없애기 위해", "일정을 숨기기 위해"}, 0, "맞춤형 추천은 사용자의 흥미가 다음 학습으로 이어지도록 돕습니다."),
                new QuizQuestion("q-s-003", "실버", "해양안전", "비상대응 학습이 중요한 이유로 적절한 것은?",
                        new String[]{"위험 상황에서 생존과 안전을 높이기 위해", "영상 시청 시간을 줄이기 위해", "전시장을 꾸미기 위해", "가격을 올리기 위해"}, 0, "비상대응은 선박·해양 현장에서 핵심 안전 역량입니다."),

                new QuizQuestion("q-g-001", "골드", "항해", "항해사가 되기 위한 로드맵에서 NCS와 연결하기 좋은 학습 순서는?",
                        new String[]{"선위결정 → 항해당직 → 선박조종 → 비상대응", "영화 감상 → 노래 연습 → 미술 감상", "식물 재배 → 요리 → 패션", "마케팅 → 회계 → 미용"}, 0, "항해 직무는 위치결정, 당직, 조종, 안전 대응 역량으로 연결됩니다."),
                new QuizQuestion("q-g-002", "골드", "해양환경", "해양환경 교육 콘텐츠를 설계할 때 가장 데이터 기반인 접근은?",
                        new String[]{"관람객 만족도와 관심 키워드를 분석해 수요가 높은 주제를 찾는다", "무작위로 주제를 정한다", "모든 사용자를 같은 수준으로 본다", "퀴즈 결과를 무시한다"}, 0, "관람객 만족도와 관심 키워드는 교육 수요 분석의 근거가 됩니다."),
                new QuizQuestion("q-p-001", "플래티넘", "해양안전", "전문 직무 승급에서 자격증·교육 수료·경력 인증을 일부 요구하는 이유는?",
                        new String[]{"실제 역량과 현장 적용 가능성을 검증하기 위해", "앱 사용을 막기 위해", "영상 수를 줄이기 위해", "관심 분야를 지우기 위해"}, 0, "고티어는 단순 시청이 아니라 현장 적용과 인증 기반 성장을 반영해야 합니다."),
                new QuizQuestion("q-d-001", "다이아", "전문가", "해양 인재 양성 플랫폼이 기관 관리자에게 줄 수 있는 핵심 가치는?",
                        new String[]{"교육 수요와 부족 영역을 데이터로 파악해 신규 프로그램 기획에 활용", "사용자 데이터를 삭제", "일정을 모두 숨김", "퀴즈를 무작위 삭제"}, 0, "기관은 사용자 관심·완료·퀴즈 데이터를 바탕으로 교육 프로그램을 개선할 수 있습니다.")
        );
    }
}
