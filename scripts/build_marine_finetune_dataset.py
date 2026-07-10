#!/usr/bin/env python3
"""Build a balanced, provider-agnostic JSONL dataset for the BluePath marine LLM.

The output follows the common chat fine-tuning shape:
{"messages": [{"role": "system", ...}, {"role": "user", ...}, {"role": "assistant", ...}]}
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
QUIZ_PATH = ROOT / "app/src/main/assets/fallback_quizzes.json"
OUTPUT_PATH = ROOT / "app/src/main/assets/marine_finetune_dataset.jsonl"

SYSTEM_PROMPT = (
    "당신은 BluePath의 해양교육 전문 AI다. 해양환경, 해양생물, 항해, 선박기관, "
    "해기사, 해운, 조선해양, 스마트항만, 자율운항선박, 해양안전과 NCS 진로교육에 "
    "특화되어 있다. 정확한 한국어로 학습자 수준에 맞게 답하고, 불확실한 최신 일정·법규·"
    "자격 요건은 추측하지 말고 공식 기관 확인이 필요하다고 안내한다."
)


def rotate_question(question: dict[str, Any], shift: int) -> dict[str, Any]:
    options = question["options"]
    rotated = [""] * 4
    for index, option in enumerate(options):
        rotated[(index + shift) % 4] = option
    return {
        "topic": question.get("topic", "해양교육"),
        "question": question["question"],
        "options": rotated,
        "answerIndex": (int(question["answerIndex"]) + shift) % 4,
        "explanation": question["explanation"],
    }


def quiz_example(question: dict[str, Any], index: int) -> dict[str, Any]:
    tier = question["tier"]
    rotated = rotate_question(question, index % 4)
    user = (
        f"{tier} 수준의 해양 승급용 4지선다 문제 1개를 JSON으로 만들어줘. "
        f"주제는 {rotated['topic']}이고, 보기 4개·정답 인덱스·해설을 포함해. JSON만 출력해."
    )
    assistant = json.dumps({"questions": [rotated]}, ensure_ascii=False)
    return {
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user},
            {"role": "assistant", "content": assistant},
        ]
    }


def agent_examples() -> list[dict[str, Any]]:
    pairs = [
        (
            "브론즈에서 실버로 승급하려면?",
            "🥉 브론즈 승급 퀴즈는 10문제이며 7문제 이상 맞히면 🥈 실버로 승급합니다. "
            "선택할 때마다 정답을 공개하지 않고, 모든 문항을 고른 뒤 최종 제출하면 점수와 문항별 해설을 확인할 수 있습니다."
        ),
        (
            "실버에서 골드로 승급하려면?",
            "🥈 실버 승급 퀴즈는 12문제이며 9문제 이상 맞히면 🥇 골드로 승급합니다. "
            "모든 문제는 4지선다이고 전 문항 제출 후 한 번에 채점합니다."
        ),
        (
            "골드에서 플래티넘으로 승급하려면?",
            "🥇 골드 승급 퀴즈는 15문제이며 10문제 이상 맞히면 🏆 플래티넘으로 승급합니다. "
            "채점 결과 화면에서 총점, 합격 여부, 내 답, 정답, 해설을 확인할 수 있습니다."
        ),
        (
            "항해사는 어떤 일을 해?",
            "항해사는 선박의 안전한 운항을 위해 항로를 계획하고 선위를 확인하며 항해당직과 선박조종을 수행합니다. "
            "주요 역량은 항해장비 운용, 기상·해상 판단, 비상대응, 승무원 협업입니다. 면허와 교육 요건은 변동될 수 있으므로 공식 기관 정보를 확인해야 합니다."
        ),
        (
            "스마트 항만이 뭐야?",
            "스마트 항만은 센서, 자동화 장비, 데이터 분석, 디지털 트윈 등을 활용해 화물 흐름과 장비 운영을 효율화하는 항만입니다. "
            "학습 경로는 항만물류 기초, ICT·데이터, 자동화 시스템, 안전 운영 순서가 적절합니다."
        ),
        (
            "자율운항선박을 공부하려면?",
            "선박 운항 기초와 해사안전을 먼저 익힌 뒤 센서, 제어, 통신, 인공지능, 사이버보안을 연결해 학습하세요. "
            "BluePath에서는 상 난도 자율운항선박 영상과 NCS 기반 항해·선박 직무 로드맵을 함께 보는 것이 좋습니다."
        ),
        (
            "해양쓰레기를 줄이는 실천은?",
            "일회용품 사용을 줄이고 올바르게 분리배출하며 해변 정화 활동에 참여하는 것이 도움이 됩니다. "
            "버려진 어구와 플라스틱이 하천을 통해 바다로 유입되지 않도록 생활 속 배출을 줄이는 것이 중요합니다."
        ),
        (
            "내 관심사는 항만·물류이고 진로탐색이 목표야. 무엇부터 보면 돼?",
            "중 난도의 해운산업 기초 영상을 먼저 보고, 다음으로 상 난도의 스마트 항만·디지털 트윈 영상을 학습하세요. "
            "그 뒤 승급 퀴즈와 항만물류 전문가 NCS 로드맵을 연결하면 학습 기록과 진로 준비를 함께 관리할 수 있습니다."
        ),
        (
            "퀴즈 답을 고르자마자 정답을 알려줘.",
            "BluePath 승급 퀴즈는 공정한 최종 평가를 위해 선택 즉시 정오를 공개하지 않습니다. "
            "모든 문제의 답을 선택하고 최종 제출한 뒤 점수, 정답, 문항별 해설을 한 번에 제공합니다."
        ),
        (
            "해기사 면허 기준을 정확히 알려줘.",
            "해기사 면허 요건은 직종, 등급, 승무경력, 교육과정과 최신 법규에 따라 달라질 수 있습니다. "
            "일반적인 학습 방향은 안내할 수 있지만, 실제 응시·면허 요건은 한국해양수산연수원과 관련 공식 기관의 최신 공고를 확인해야 합니다."
        ),
    ]
    return [
        {
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": user},
                {"role": "assistant", "content": assistant},
            ]
        }
        for user, assistant in pairs
    ]


def main() -> None:
    questions = json.loads(QUIZ_PATH.read_text(encoding="utf-8"))
    rows = [quiz_example(question, index) for index, question in enumerate(questions)]
    rows.extend(agent_examples())
    OUTPUT_PATH.write_text(
        "".join(json.dumps(row, ensure_ascii=False) + "\n" for row in rows),
        encoding="utf-8",
    )
    print(f"Wrote {len(rows)} examples to {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
