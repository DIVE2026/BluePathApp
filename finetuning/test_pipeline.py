"""Regression tests for the BluePath fine-tuning data and evaluation pipeline."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

from finetuning.evaluate_model import score_case
from scripts import build_marine_finetune_dataset as builder
from scripts.validate_marine_dataset import validate_row

ROOT = Path(__file__).resolve().parents[1]


class FineTuningPipelineTest(unittest.TestCase):
    def test_group_split_has_no_source_leakage(self) -> None:
        questions = json.loads(builder.QUIZ_PATH.read_text(encoding="utf-8"))
        knowledge = json.loads(builder.KNOWLEDGE_PATH.read_text(encoding="utf-8"))
        rows = (
            builder.quiz_rows(questions)
            + builder.grounded_agent_rows(knowledge)
            + builder.policy_rows()
            + builder.safety_rows()
        )
        train, validation, evaluation = builder.deterministic_group_split(rows)
        splits = {"train": train, "validation": validation, "evaluation": evaluation}
        builder.assert_no_leakage(splits)

        group_sets = {
            name: {row["metadata"]["groupId"] for row in values}
            for name, values in splits.items()
        }
        self.assertTrue(group_sets["train"].isdisjoint(group_sets["validation"]))
        self.assertTrue(group_sets["train"].isdisjoint(group_sets["evaluation"]))
        self.assertTrue(group_sets["validation"].isdisjoint(group_sets["evaluation"]))

    def test_reference_answers_pass_all_automated_checks(self) -> None:
        cases = json.loads((ROOT / "finetuning/data/eval_cases.json").read_text(encoding="utf-8"))
        for case in cases:
            with self.subTest(case=case["id"]):
                checks = score_case(case, case["referenceAnswer"])
                self.assertTrue(all(checks.values()), checks)

    def test_validator_rejects_duplicate_quiz_options(self) -> None:
        row = {
            "messages": [
                {"role": "system", "content": "system"},
                {"role": "user", "content": "근거 [1]로 문제를 만들어줘"},
                {
                    "role": "assistant",
                    "content": json.dumps(
                        {
                            "questions": [
                                {
                                    "topic": "해양안전",
                                    "question": "질문",
                                    "options": ["같음", "같음", "셋", "넷"],
                                    "answerIndex": 0,
                                    "explanation": "설명",
                                    "sourceNumbers": [1],
                                }
                            ]
                        },
                        ensure_ascii=False,
                    ),
                },
            ]
        }
        errors, _ = validate_row(row, 1)
        self.assertTrue(any("options must be unique" in error for error in errors), errors)


if __name__ == "__main__":
    unittest.main()
