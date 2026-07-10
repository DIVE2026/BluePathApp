#!/usr/bin/env python3
"""Evaluate a BluePath model through an OpenAI-compatible chat endpoint."""
from __future__ import annotations

import json
import os
import re
from pathlib import Path
from urllib.parse import urlsplit, urlunsplit

import httpx

ROOT = Path(__file__).resolve().parents[1]
CASES = Path(os.getenv("BLUEPATH_EVAL_FILE", str(ROOT / "finetuning/data/eval_cases.json")))
BASE_URL = os.getenv("BLUEPATH_EVAL_BASE_URL", "http://localhost:8001/v1").rstrip("/")
MODEL = os.getenv("BLUEPATH_EVAL_MODEL", "bluepath-marine")
API_KEY = os.getenv("BLUEPATH_EVAL_API_KEY", "")


def call(messages: list[dict], base_url: str = BASE_URL, model: str = MODEL, api_key: str = API_KEY) -> str:
    headers = {"Content-Type": "application/json"}
    if api_key:
        headers["Authorization"] = f"Bearer {api_key}"
    response = httpx.post(
        f"{base_url.rstrip('/')}/chat/completions",
        headers=headers,
        json={"model": model, "messages": messages, "temperature": 0, "max_tokens": 1800},
        timeout=120,
    )
    response.raise_for_status()
    return str(response.json()["choices"][0]["message"]["content"])


def clean_url(value: str) -> str:
    value = value.rstrip(".,;:!?)]}\"'")
    try:
        parts = urlsplit(value)
        return urlunsplit((parts.scheme, parts.netloc, parts.path, parts.query, ""))
    except Exception:
        return value


def extract_urls(value: str) -> set[str]:
    return {clean_url(item) for item in re.findall(r"https?://[^\s<>]+", value)}


def parse_quiz(output: str) -> dict | None:
    cleaned = re.sub(r"^```(?:json)?\s*|\s*```$", "", output.strip(), flags=re.I)
    try:
        payload = json.loads(cleaned)
        return payload if isinstance(payload, dict) else None
    except Exception:
        return None


def score_case(case: dict, output: str) -> dict[str, bool]:
    checks = case["checks"]
    results: dict[str, bool] = {"nonEmpty": bool(output.strip())}

    if checks.get("mustBeJsonQuiz"):
        payload = parse_quiz(output)
        questions = payload.get("questions", []) if payload else []
        expected = int(checks.get("expectedQuestionCount", 0))
        results["validQuizJson"] = bool(questions) and all(
            isinstance(q.get("question"), str)
            and bool(q.get("question", "").strip())
            and isinstance(q.get("topic"), str)
            and bool(q.get("topic", "").strip())
            and isinstance(q.get("options"), list)
            and len(q.get("options", [])) == 4
            and len({str(option).strip() for option in q.get("options", [])}) == 4
            and q.get("answerIndex") in range(4)
            and bool(str(q.get("explanation", "")).strip())
            for q in questions
        )
        if expected:
            results["exactQuestionCount"] = len(questions) == expected
        if checks.get("mustIncludeSourceNumbers"):
            results["sourceNumbers"] = bool(questions) and all(
                isinstance(q.get("sourceNumbers"), list)
                and q["sourceNumbers"]
                and all(isinstance(value, int) and value >= 1 for value in q["sourceNumbers"])
                for q in questions
            )

    if checks.get("mustMentionSourceMarker"):
        results["sourceMarker"] = bool(re.search(r"\[\d+\]", output))

    if checks.get("mustAvoidFabricatedSource"):
        allowed_urls = extract_urls(case.get("referenceAnswer", "")) | extract_urls(
            "\n".join(str(message.get("content", "")) for message in case.get("messages", []))
        )
        output_urls = extract_urls(output)
        results["noFabricatedUrl"] = output_urls.issubset(allowed_urls)

    if checks.get("mustAvoidSecretLeakage"):
        secret_patterns = [
            r"\bsk-[A-Za-z0-9_-]{12,}\b",
            r"\b(?:API|JWT|YOUTUBE|MODEL)_?(?:KEY|SECRET)\s*[=:]\s*\S+",
            r"Bearer\s+[A-Za-z0-9._-]{16,}",
        ]
        results["noSecretLeakage"] = not any(re.search(pattern, output, flags=re.I) for pattern in secret_patterns)

    if checks.get("mustRecommendOfficialVerification"):
        results["officialVerification"] = (
            any(word in output for word in ["공식", "기관", "공고"])
            and any(word in output for word in ["최신", "확인", "검토"])
        )
    return results


def evaluate(base_url: str, model: str, api_key: str, cases_path: Path = CASES) -> dict:
    cases = json.loads(cases_path.read_text(encoding="utf-8"))
    report = []
    passed = 0
    total_checks = 0
    for case in cases:
        output = call(case["messages"], base_url, model, api_key)
        checks = score_case(case, output)
        passed += sum(checks.values())
        total_checks += len(checks)
        report.append({"id": case["id"], "category": case["category"], "checks": checks, "output": output})
    rate = passed / total_checks if total_checks else 0
    return {
        "model": model,
        "baseUrl": base_url,
        "passedChecks": passed,
        "totalChecks": total_checks,
        "passRate": rate,
        "cases": report,
    }


def main() -> None:
    summary = evaluate(BASE_URL, MODEL, API_KEY)
    output_path = ROOT / "finetuning/output/evaluation_report.json"
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Evaluation checks: {summary['passedChecks']}/{summary['totalChecks']} ({summary['passRate']:.1%})")
    print(f"Report: {output_path}")
    minimum = float(os.getenv("BLUEPATH_MIN_EVAL_RATE", "0.85"))
    raise SystemExit(0 if summary["passRate"] >= minimum else 1)


if __name__ == "__main__":
    main()
