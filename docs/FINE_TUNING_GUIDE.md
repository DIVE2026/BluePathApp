# BluePath Marine Model Fine-Tuning Guide

BluePath uses a two-layer AI design:

1. **RAG supplies current, inspectable marine evidence.**
2. **Fine-tuning teaches stable behavior, format, tone, quiz structure, source discipline, privacy, and safety patterns.**

Fine-tuning is not a replacement for current laws, license requirements, schedules, or institutional notices. Those facts belong in the RAG knowledge base so they can be reviewed and updated without retraining the model.

## Included pipeline

```text
scripts/build_marine_finetune_dataset.py   Build deterministic, group-isolated train/validation/evaluation splits
scripts/validate_marine_dataset.py         Validate chat structure, quiz JSON, secrets, duplicates, and split isolation
finetuning/train_lora.py                   Run dataset preflight and train a PEFT LoRA adapter with early stopping
finetuning/evaluate_model.py               Run retry-safe format, grounding, privacy, safety, and integrity checks
finetuning/compare_models.py               Compare overall and per-category results against a baseline
finetuning/test_pipeline.py                Regression-test splitting, validators, and evaluation reference answers
finetuning/data/                            Generated datasets, evaluation cases, hashes, and manifest
finetuning/.env.example                    Developer-entered training, serving, and release-gate values
```

## What version 2 of the dataset teaches

The generated dataset contains two prompt variants for each reviewed quiz and knowledge source. Variants from the same source are assigned a shared group ID and always stay in the same partition, preventing near-duplicate source leakage between training, validation, and evaluation.

The examples cover:

- Bronze, Silver, Gold, and Platinum four-option quiz generation
- Balanced correct-answer positions
- Explanations and validated source-number fields
- Grounded marine education and career guidance
- Evidence limitations and official-verification language
- Tier promotion rules and assessment integrity
- Refusal to fabricate institutions, sources, and URLs
- Prompt-injection resistance
- API-secret and minor learner-data protection
- Caution around emergency, legal, license, and schedule claims
- Diamond certification and project requirements

The generated `finetuning/data/manifest.json` records split sizes, category counts, group counts, answer-index balance, SHA-256 hashes, and leakage-check results.

## Step 1 — Build, validate, and test the dataset

```bash
python scripts/build_marine_finetune_dataset.py
python scripts/validate_marine_dataset.py \
  finetuning/data/train.jsonl \
  finetuning/data/validation.jsonl
python -m unittest finetuning.test_pipeline
```

The validator rejects malformed role sequences, empty turns, invalid quiz objects, duplicate options, invalid answer indexes, missing source numbers, assistant-side secret leakage, duplicate rows, and duplicates across supplied files.

Review generated files before every production run. Domain experts should check factual accuracy, terminology, difficulty, answer uniqueness, distractor quality, explanation usefulness, source fidelity, uncertainty handling, and personal-data minimization.

To reproduce a different deterministic split, set the same seed everywhere:

```bash
export BLUEPATH_DATASET_SEED=20260711
export BLUEPATH_SEED=20260711
python scripts/build_marine_finetune_dataset.py
```

## Step 2 — Add approved institutional examples

Add organization-approved conversations in the same chat JSONL shape:

```json
{"messages":[
  {"role":"system","content":"BluePath Marine AI system instruction"},
  {"role":"user","content":"Evidence and learner request"},
  {"role":"assistant","content":"Grounded answer or validated quiz JSON"}
]}
```

Keep training, validation, and evaluation items separate. Related variants from the same source must remain in one partition. Do not place held-out evaluation cases in the training file.

Recommended review dimensions:

- Marine-domain correctness
- Age and tier appropriateness
- Four-choice quiz validity
- Correct answer and distractor quality
- Explanation usefulness
- Source fidelity and URL integrity
- Prompt-injection resistance
- Safety and uncertainty handling
- Personal-data minimization
- Assessment integrity

## Step 3 — Prepare the training environment

A CUDA GPU is recommended. Create an isolated environment and install the training packages:

```bash
python -m venv .venv-finetune
source .venv-finetune/bin/activate
pip install -r finetuning/requirements.txt
cp finetuning/.env.example finetuning/.env
set -a; source finetuning/.env; set +a
```

Set `BLUEPATH_BASE_MODEL` to a chat model whose license permits the intended use and deployment.

## Step 4 — Train the LoRA adapter

```bash
python finetuning/train_lora.py
```

Before loading the model, the training script validates both datasets and fails on malformed examples or train/validation duplicates. The default run uses supervised fine-tuning with a LoRA adapter. On compatible CUDA systems, it uses NF4 4-bit loading and LoRA across all linear layers for a QLoRA-style run.

Training improvements include:

- Fixed training and data seeds
- Dataset SHA-256 hashes in the run manifest
- Cosine learning-rate scheduling
- Gradient clipping
- Non-reentrant gradient checkpointing
- Best-checkpoint selection by validation loss
- Configurable early stopping
- Optional automatic checkpoint resume
- Trainable-parameter counts and package versions
- Full trainer log history in `bluepath_log_history.json`

Important environment variables:

```dotenv
BLUEPATH_BASE_MODEL=Qwen/Qwen2.5-3B-Instruct
BLUEPATH_EPOCHS=3
BLUEPATH_LEARNING_RATE=0.0002
BLUEPATH_LR_SCHEDULER=cosine
BLUEPATH_MAX_GRAD_NORM=1.0
BLUEPATH_LORA_R=16
BLUEPATH_LORA_ALPHA=32
BLUEPATH_LORA_TARGETS=all-linear
BLUEPATH_MAX_SEQ_LENGTH=2048
BLUEPATH_EARLY_STOPPING_PATIENCE=2
BLUEPATH_AUTO_RESUME=false
BLUEPATH_ASSISTANT_ONLY_LOSS=false
BLUEPATH_EOS_TOKEN=<|im_end|>
BLUEPATH_SEED=20260711
BLUEPATH_FINETUNE_OUTPUT=./finetuning/output/marine-lora
```

Set `BLUEPATH_RESUME_CHECKPOINT=auto` or `BLUEPATH_AUTO_RESUME=true` to continue from the numerically latest `checkpoint-*` directory. More epochs are not automatically better; retain the best validation checkpoint and stop when validation loss no longer improves.

`BLUEPATH_ASSISTANT_ONLY_LOSS` remains disabled by default for broad tokenizer compatibility. Enable it only after confirming that the selected model chat template exposes a valid assistant-generation mask.

## Step 5 — Serve the trained adapter

```bash
export BLUEPATH_BASE_MODEL="Qwen/Qwen2.5-3B-Instruct"
export BLUEPATH_ADAPTER_PATH="./finetuning/output/marine-lora"
export BLUEPATH_SERVED_MODEL="bluepath-marine"
export BLUEPATH_MODEL_API_KEY="replace-with-a-long-random-value"
export BLUEPATH_SERVE_4BIT="true"
uvicorn finetuning.serve_model:app --host 0.0.0.0 --port 8001
```

The server exposes:

```text
POST /v1/chat/completions
GET  /health
```

Only the FastAPI backend should know `BLUEPATH_MODEL_API_KEY`. Do not place it in Android resources, `BuildConfig`, source code, logs, or My Page.

## Step 6 — Evaluate before deployment

```bash
export BLUEPATH_EVAL_BASE_URL="http://localhost:8001/v1"
export BLUEPATH_EVAL_MODEL="bluepath-marine"
export BLUEPATH_EVAL_API_KEY="$BLUEPATH_MODEL_API_KEY"
python finetuning/evaluate_model.py
```

The evaluation client uses deterministic generation, configurable timeouts, and retry handling. It records request errors and latency, then reports overall and category-level pass rates.

Automated checks cover:

- Non-empty answers
- Parseable quiz JSON and expected question count
- Exactly four unique options
- Valid answer indexes, explanations, and source-number fields
- Source numbers limited to evidence markers supplied in the prompt
- Citation markers for grounded answers
- Fabricated URLs against supplied evidence
- Accidental API-key or bearer-token leakage
- Official-verification language for changing rules, licenses, and schedules
- Prompt-injection resistance
- Personal-data protection
- Professional and emergency caution
- Assessment integrity

Release gates are controlled by:

```dotenv
BLUEPATH_MIN_EVAL_RATE=0.85
BLUEPATH_MIN_CATEGORY_RATE=0.75
BLUEPATH_EVAL_TIMEOUT_SECONDS=120
BLUEPATH_EVAL_RETRIES=2
```

The detailed report is saved to `finetuning/output/evaluation_report.json`. Automated checks are a release gate, not a substitute for marine-domain expert review.

## Step 7 — Compare against the base model

Expose the baseline and candidate through OpenAI-compatible endpoints and run:

```bash
export BLUEPATH_BASELINE_URL="http://localhost:8002/v1"
export BLUEPATH_BASELINE_MODEL="base-model-name"
export BLUEPATH_EVAL_BASE_URL="http://localhost:8001/v1"
export BLUEPATH_EVAL_MODEL="bluepath-marine"
python finetuning/compare_models.py
```

The comparison records overall pass-rate delta and per-category deltas. By default it fails when the candidate is below the minimum score, has a negative overall delta, has request failures, or regresses in any category.

```dotenv
BLUEPATH_MIN_PASS_RATE_DELTA=0.0
BLUEPATH_CATEGORY_REGRESSION_TOLERANCE=0.0
BLUEPATH_MAX_REGRESSED_CATEGORIES=0
```

The detailed result is written to `finetuning/output/model_comparison.json`.

## Step 8 — Connect the model to BluePath

Enter model-server values in `backend/.env`:

```dotenv
LLM_BASE_URL=http://host.docker.internal:8001/v1
LLM_API_KEY=replace-with-the-model-serving-key
LLM_MODEL=bluepath-marine
```

Restart the API. The same model is then used by the RAG AI Agent and promotion-quiz generator. The backend still validates every generated quiz and falls back to the verified local bank when output is incomplete or invalid.

## Step 9 — Production release checklist

- Freeze reviewed training and evaluation datasets with version tags and SHA-256 hashes.
- Record the base model, adapter configuration, random seed, dependencies, and best checkpoint.
- Run dataset validation, regression tests, automated evaluation, baseline comparison, and marine-domain expert review.
- Red-team source fabrication, prompt injection, unsafe maritime advice, privacy leakage, and assessment leakage.
- Deploy behind authentication, rate limits, minimized request logging, and rollback support.
- Monitor failed quiz validation, unsupported answers, category-level regressions, latency, and user feedback.
- Update changing facts through RAG; retrain only when behavior or domain coverage needs improvement.
