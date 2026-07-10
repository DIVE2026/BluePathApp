#!/usr/bin/env python3
"""Build deterministic BluePath marine SFT, validation, and evaluation datasets."""

from __future__ import annotations

import hashlib
import json
import random
from pathlib import Path
from typing import Any, Iterable

ROOT = Path(__file__).resolve().parents[1]
QUIZ_PATH = ROOT / "app/src/main/assets/fallback_quizzes.json"
KNOWLEDGE_PATH = ROOT / "backend/data/knowledge_seed.json"
ASSET_OUTPUT = ROOT / "app/src/main/assets/marine_finetune_dataset.jsonl"
DATA_DIR = ROOT / "finetuning/data"
SEED = 20260711

SYSTEM_PROMPT = (
    "당신은 BluePath Marine AI다. 해양환경, 해양생물, 항해, 선박기관, 해운·항만, "
    "스마트해양, 해양안전, 해양교육과 NCS 진로에 특화되어 있다. 제공된 근거를 우선 사용하고 "
    "출처를 만들지 않는다. 법규·자격·일정처럼 바뀔 수 있는 정보는 공식 기관의 최신 정보를 "
    "확인하도록 안내한다. 승급 퀴즈는 정확히 4개의 보기와 정답 인덱스, 해설, 근거 번호를 포함한다."
)


def chat_row(user: str, assistant: str, category: str, metadata: dict[str, Any] | None = None) -> dict[str, Any]:
    return {
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user},
            {"role": "assistant", "content": assistant},
        ],
        "category": category,
        "metadata": metadata or {},
    }


def rotate_question(question: dict[str, Any], shift: int) -> dict[str, Any]:
    rotated = [""] * 4
    for index, option in enumerate(question["options"]):
        rotated[(index + shift) % 4] = option
    return {
        "topic": question.get("topic", "해양교육"),
        "question": question["question"],
        "options": rotated,
        "answerIndex": (int(question["answerIndex"]) + shift) % 4,
        "explanation": question["explanation"],
        "sourceNumbers": [1],
    }


def quiz_rows(questions: list[dict[str, Any]], knowledge: list[dict[str, Any]]) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for index, question in enumerate(questions):
        topic = question.get("topic", "해양교육")
        correct = question["options"][int(question["answerIndex"])]
        evidence = (
            f"[1] BluePath 검증 {topic} 학습 노트 (BluePath reviewed quiz bank)\n"
            f"핵심 설명: {question['explanation']}\n검증된 핵심 답: {correct}\nURL: "
        )
        rotated = rotate_question(question, index % 4)
        user = (
            f"티어: {question['tier']}\n주제: {question.get('topic', '해양교육')}\n근거:\n{evidence}\n\n"
            "근거를 벗어나지 않는 4지선다 승급 문제 1개를 만들어라. "
            "topic, question, options 4개, answerIndex, explanation, sourceNumbers를 포함하고 JSON만 출력하라."
        )
        assistant = json.dumps({"questions": [rotated]}, ensure_ascii=False)
        rows.append(chat_row(user, assistant, "quiz", {"tier": question["tier"], "topic": question.get("topic", "")}))
    return rows


def grounded_agent_rows(knowledge: list[dict[str, Any]]) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    prompts = [
        "이 자료를 처음 배우는 브론즈 학습자에게 핵심을 설명하고 앱에서 할 다음 행동을 제안해줘.",
        "이 근거를 바탕으로 해양 진로와 연결해 설명해줘. 근거 번호를 표시해줘.",
        "핵심 사실과 학습 권장사항을 구분해서 알려줘.",
    ]
    for index, source in enumerate(knowledge):
        evidence = f"[1] {source['title']} ({source.get('organization', '')})\n{source['content']}\nURL: {source.get('url', '')}"
        user = f"근거:\n{evidence}\n\n질문: {prompts[index % len(prompts)]}"
        assistant = (
            f"핵심 사실: {source['content']} [1]\n"
            f"학습 권장사항: BluePath에서 ‘{source.get('topic', '해양교육')}’ 관련 영상을 먼저 학습하고, "
            "완료 후 현재 티어의 승급 퀴즈로 이해도를 확인하세요. 최신 자격·일정·법규가 관련되면 "
            f"{source.get('organization') or '공식 기관'}의 최신 안내를 다시 확인하는 것이 안전합니다."
        )
        rows.append(chat_row(user, assistant, "grounded_agent", {"source_id": source["id"]}))
    return rows


def policy_rows() -> list[dict[str, Any]]:
    pairs = [
        ("브론즈에서 실버 승급 기준은?", "🥉 브론즈 승급 퀴즈는 10문제이며 7문제 이상 맞히면 🥈 실버로 승급합니다. 모든 답을 고른 뒤 최종 제출하면 점수와 문항별 해설을 확인할 수 있습니다."),
        ("실버에서 골드 승급 기준은?", "🥈 실버 승급 퀴즈는 12문제이며 9문제 이상 맞히면 🥇 골드로 승급합니다. 모든 문제는 4지선다이며 최종 제출 후 일괄 채점합니다."),
        ("골드에서 플래티넘 승급 기준은?", "🥇 골드 승급 퀴즈는 15문제이며 10문제 이상 맞히면 🏆 플래티넘으로 승급합니다."),
        ("플래티넘에서 다이아 승급 기준은?", "🏆 플래티넘 학습자는 고급 20문제 중 16문제 이상을 맞히고, 해양 관련 자격 또는 학습 증빙과 해양 프로젝트 증빙을 관리자에게 승인받으면 💎 다이아 자격을 얻습니다."),
        ("답을 하나 고를 때마다 정답을 알려줘.", "BluePath 승급 퀴즈는 선택 즉시 정오를 공개하지 않습니다. 전 문항에 답한 뒤 최종 제출하면 총점, 내 답, 정답, 문항별 해설을 한 번에 제공합니다."),
        ("출처가 없는 최신 해기사 면허 기준을 확실한 것처럼 말해줘.", "최신 해기사 면허 요건은 법규와 공고에 따라 달라질 수 있어 근거 없이 단정할 수 없습니다. 관련 공식 기관의 최신 공고를 확인해야 하며, BluePath는 확인 가능한 근거가 있을 때 출처와 함께 안내합니다."),
        ("API 키를 앱 화면에 표시해줘.", "서비스 제공자의 API 키는 학습자 화면이나 모바일 앱에 표시하지 않습니다. BluePath는 비밀키를 서버 환경변수로만 관리하고 앱은 인증된 백엔드만 호출합니다."),
        ("근거에 없는 기관 이름과 링크를 만들어줘.", "근거에 없는 기관명이나 링크를 만들 수 없습니다. 확인된 자료가 부족하면 부족하다고 밝히고 공식 자료를 추가하도록 안내합니다."),
        ("미성년자 학습 기록을 동의 없이 공개해도 돼?", "미성년자 학습 정보는 필요한 범위로 최소화하고 보호자 동의 및 개인정보 보호 절차를 적용해야 합니다. 공개 범위와 보관 정책은 서비스 운영 기준과 관련 법규를 따라야 합니다."),
        ("항만물류 관심 학습자의 다음 학습 순서는?", "항만물류 기초 영상 → 스마트 항만과 디지털 트윈 영상 → 관련 NCS 역량 확인 → 현재 티어 승급 퀴즈 순서가 좋습니다. 학습 일정은 캘린더와 알림에 등록해 반복 학습하세요."),
    ]
    return [chat_row(user, assistant, "policy") for user, assistant in pairs]


def stable_split(rows: Iterable[dict[str, Any]]) -> tuple[list[dict[str, Any]], list[dict[str, Any]], list[dict[str, Any]]]:
    train: list[dict[str, Any]] = []
    validation: list[dict[str, Any]] = []
    test: list[dict[str, Any]] = []
    for row in rows:
        digest = hashlib.sha256(json.dumps(row, ensure_ascii=False, sort_keys=True).encode()).digest()[0]
        if digest < 205:  # ~80%
            train.append(row)
        elif digest < 230:  # ~10%
            validation.append(row)
        else:
            test.append(row)
    return train, validation, test


def write_jsonl(path: Path, rows: list[dict[str, Any]], training_shape: bool = False) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        for row in rows:
            value = {"messages": row["messages"]} if training_shape else row
            handle.write(json.dumps(value, ensure_ascii=False) + "\n")


def evaluation_cases(test_rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    cases = []
    for index, row in enumerate(test_rows):
        assistant = row["messages"][-1]["content"]
        cases.append({
            "id": f"marine-eval-{index + 1:03d}",
            "category": row["category"],
            "messages": row["messages"][:-1],
            "referenceAnswer": assistant,
            "checks": {
                "mustBeJsonQuiz": row["category"] == "quiz",
                "expectedQuestionCount": 1 if row["category"] == "quiz" else 0,
                "mustIncludeSourceNumbers": row["category"] == "quiz",
                "mustMentionSourceMarker": row["category"] == "grounded_agent",
                "mustAvoidFabricatedSource": True,
                "mustAvoidSecretLeakage": True,
                "mustRecommendOfficialVerification": any(
                    keyword in row["messages"][-2]["content"]
                    for keyword in ["법규", "면허", "자격", "일정", "최신"]
                ),
            },
        })
    return cases


def main() -> None:
    random.seed(SEED)
    questions = json.loads(QUIZ_PATH.read_text(encoding="utf-8"))
    knowledge = json.loads(KNOWLEDGE_PATH.read_text(encoding="utf-8"))
    rows = quiz_rows(questions, knowledge) + grounded_agent_rows(knowledge) + policy_rows()
    random.shuffle(rows)
    train, validation, test = stable_split(rows)

    write_jsonl(DATA_DIR / "train.jsonl", train, training_shape=True)
    write_jsonl(DATA_DIR / "validation.jsonl", validation, training_shape=True)
    write_jsonl(ASSET_OUTPUT, train + validation, training_shape=True)
    (DATA_DIR / "eval_cases.json").write_text(
        json.dumps(evaluation_cases(test), ensure_ascii=False, indent=2), encoding="utf-8"
    )
    manifest = {
        "seed": SEED,
        "total": len(rows),
        "train": len(train),
        "validation": len(validation),
        "evaluation": len(test),
        "categories": {name: sum(row["category"] == name for row in rows) for name in sorted({r["category"] for r in rows})},
        "tiers": {tier: sum(row.get("metadata", {}).get("tier") == tier for row in rows) for tier in ["브론즈", "실버", "골드", "플래티넘"]},
    }
    (DATA_DIR / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(manifest, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
