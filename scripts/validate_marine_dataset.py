#!/usr/bin/env python3
"""Validate BluePath training data before a fine-tuning run."""
from __future__ import annotations

import argparse
import json
from pathlib import Path


def validate(path: Path) -> tuple[int, list[str]]:
    errors: list[str] = []
    count = 0
    for line_no, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not raw.strip():
            continue
        count += 1
        try:
            row = json.loads(raw)
        except json.JSONDecodeError as exc:
            errors.append(f"line {line_no}: invalid JSON ({exc})")
            continue
        messages = row.get("messages")
        if not isinstance(messages, list) or len(messages) < 3:
            errors.append(f"line {line_no}: messages must contain system, user, and assistant turns")
            continue
        roles = [item.get("role") for item in messages]
        if roles[0] != "system" or roles[-1] != "assistant" or "user" not in roles:
            errors.append(f"line {line_no}: invalid role sequence {roles}")
        if any(not isinstance(item.get("content"), str) or not item["content"].strip() for item in messages):
            errors.append(f"line {line_no}: every message needs non-empty text")
        assistant = messages[-1]["content"].strip()
        if assistant.startswith("{"):
            try:
                payload = json.loads(assistant)
                for q_index, question in enumerate(payload.get("questions", []), 1):
                    if len(question.get("options", [])) != 4:
                        errors.append(f"line {line_no}, question {q_index}: exactly four options required")
                    if question.get("answerIndex") not in range(4):
                        errors.append(f"line {line_no}, question {q_index}: invalid answerIndex")
                    if not question.get("explanation"):
                        errors.append(f"line {line_no}, question {q_index}: explanation required")
            except json.JSONDecodeError:
                errors.append(f"line {line_no}: assistant output looks like JSON but cannot be parsed")
    return count, errors


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("paths", nargs="+", type=Path)
    args = parser.parse_args()
    failed = False
    for path in args.paths:
        count, errors = validate(path)
        print(f"{path}: {count} examples, {len(errors)} errors")
        for error in errors:
            print(f"  - {error}")
        failed |= bool(errors)
    raise SystemExit(1 if failed else 0)


if __name__ == "__main__":
    main()
