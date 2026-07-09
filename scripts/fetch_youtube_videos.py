"""
BluePath seed video collector

YouTube 웹페이지를 무단 스크래핑하지 않고, YouTube Data API v3로 공식 채널/키워드 영상을 수집해
app/src/main/assets/seed_videos.json 스키마로 저장하는 스크립트입니다.

사용법:
  export YOUTUBE_API_KEY="YOUR_KEY"
  python scripts/fetch_youtube_videos.py

기본 검색어는 공공·교육기관 채널 중심입니다. 결과는 수동 검수 후 앱 seed로 반영하세요.
"""
import json
import os
import urllib.parse
import urllib.request
from pathlib import Path

API_KEY = os.getenv("YOUTUBE_API_KEY")
SEARCH_TERMS = [
    "해양환경공단 어린이 교육 해양환경",
    "해양수산부 어린이 해양교육",
    "국립해양과학관 해양 교육",
    "국립해양생물자원관 교육 해양생물",
    "국립해양박물관 교육 프로그램",
    "한국해양수산연수원 항해 교육",
]

DIFFICULTY_RULES = [
    ("하", ["어린이", "유아", "초등", "놀이", "만화", "애니메이션"]),
    ("상", ["자격", "면허", "직무", "특강", "방제", "항해", "안전교육"]),
]

def call_api(params):
    url = "https://www.googleapis.com/youtube/v3/search?" + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=20) as response:
        return json.loads(response.read().decode("utf-8"))

def infer_difficulty(title, description):
    text = f"{title} {description}"
    for diff, keywords in DIFFICULTY_RULES:
        if any(k in text for k in keywords):
            return diff
    return "중"

def tier_for(difficulty):
    return {"하": "브론즈", "중": "실버", "상": "골드"}.get(difficulty, "브론즈")

def topic_for(title, description):
    text = f"{title} {description}"
    for topic in ["해양환경", "해양생물", "항해", "선박", "독도", "해양안전", "항만", "해양문화"]:
        if topic in text:
            return topic
    return "해양문화"

def main():
    if not API_KEY:
        raise SystemExit("YOUTUBE_API_KEY 환경변수가 필요합니다.")
    seen = set()
    results = []
    for term in SEARCH_TERMS:
        data = call_api({
            "part": "snippet",
            "q": term,
            "type": "video",
            "maxResults": 8,
            "order": "relevance",
            "regionCode": "KR",
            "relevanceLanguage": "ko",
            "key": API_KEY,
        })
        for item in data.get("items", []):
            vid = item.get("id", {}).get("videoId")
            if not vid or vid in seen:
                continue
            seen.add(vid)
            sn = item.get("snippet", {})
            title = sn.get("title", "")
            desc = sn.get("description", "")
            diff = infer_difficulty(title, desc)
            topic = topic_for(title, desc)
            results.append({
                "id": f"yt-{vid}",
                "title": title,
                "source": sn.get("channelTitle", "YouTube"),
                "url": f"https://www.youtube.com/watch?v={vid}",
                "difficulty": diff,
                "requiredTier": tier_for(diff),
                "topic": topic,
                "careerTag": "해양교육" if topic in ["해양문화", "독도"] else topic,
                "minutes": 8,
            })
    out = Path(__file__).resolve().parents[1] / "app/src/main/assets/seed_videos.generated.json"
    out.write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"saved {len(results)} videos -> {out}")

if __name__ == "__main__":
    main()
