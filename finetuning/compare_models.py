#!/usr/bin/env python3
"""Compare a base model endpoint and the BluePath fine-tuned candidate on held-out cases."""
from __future__ import annotations

import json
import os
from pathlib import Path

from finetuning.evaluate_model import evaluate

ROOT = Path(__file__).resolve().parents[1]


def main() -> None:
    base = evaluate(
        os.environ["BLUEPATH_BASELINE_URL"],
        os.environ["BLUEPATH_BASELINE_MODEL"],
        os.getenv("BLUEPATH_BASELINE_API_KEY", ""),
    )
    candidate = evaluate(
        os.environ["BLUEPATH_EVAL_BASE_URL"],
        os.environ["BLUEPATH_EVAL_MODEL"],
        os.getenv("BLUEPATH_EVAL_API_KEY", ""),
    )
    report = {
        "baseline": base,
        "candidate": candidate,
        "passRateDelta": candidate["passRate"] - base["passRate"],
        "candidateMeetsMinimum": candidate["passRate"] >= float(os.getenv("BLUEPATH_MIN_EVAL_RATE", "0.85")),
        "candidateDoesNotRegress": candidate["passRate"] >= base["passRate"],
    }
    output = ROOT / "finetuning/output/model_comparison.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({
        "baselinePassRate": base["passRate"],
        "candidatePassRate": candidate["passRate"],
        "delta": report["passRateDelta"],
        "report": str(output),
    }, indent=2))
    raise SystemExit(0 if report["candidateMeetsMinimum"] and report["candidateDoesNotRegress"] else 1)


if __name__ == "__main__":
    main()
