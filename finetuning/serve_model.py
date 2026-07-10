#!/usr/bin/env python3
"""Serve a base model plus BluePath LoRA adapter with an OpenAI-compatible endpoint."""
from __future__ import annotations

import os
import time
import uuid

import torch
from fastapi import FastAPI, Header, HTTPException
from peft import PeftModel
from pydantic import BaseModel, Field
from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig

BASE_MODEL = os.getenv("BLUEPATH_BASE_MODEL", "Qwen/Qwen2.5-3B-Instruct")
ADAPTER_PATH = os.getenv("BLUEPATH_ADAPTER_PATH", "./output/marine-lora")
MODEL_NAME = os.getenv("BLUEPATH_SERVED_MODEL", "bluepath-marine")
SERVING_KEY = os.getenv("BLUEPATH_MODEL_API_KEY", "")
SERVE_4BIT = os.getenv("BLUEPATH_SERVE_4BIT", "true").lower() == "true"

app = FastAPI(title="BluePath Marine Model Server")
tokenizer = AutoTokenizer.from_pretrained(ADAPTER_PATH, trust_remote_code=True)
quantization_config = None
if SERVE_4BIT and torch.cuda.is_available():
    compute_dtype = torch.bfloat16 if torch.cuda.is_bf16_supported() else torch.float16
    quantization_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_compute_dtype=compute_dtype,
        bnb_4bit_use_double_quant=True,
    )

base = AutoModelForCausalLM.from_pretrained(
    BASE_MODEL,
    dtype="auto",
    device_map="auto",
    quantization_config=quantization_config,
    trust_remote_code=True,
)
model = PeftModel.from_pretrained(base, ADAPTER_PATH)
model.eval()


class ChatRequest(BaseModel):
    model: str = MODEL_NAME
    messages: list[dict[str, str]]
    temperature: float = Field(default=0.2, ge=0, le=2)
    max_tokens: int = Field(default=1200, ge=1, le=9000)


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "model": MODEL_NAME, "quantized4Bit": bool(quantization_config)}


@app.post("/v1/chat/completions")
def chat(request: ChatRequest, authorization: str = Header(default="")) -> dict:
    if SERVING_KEY and authorization != f"Bearer {SERVING_KEY}":
        raise HTTPException(status_code=401, detail="Invalid model API key")
    text = tokenizer.apply_chat_template(request.messages, tokenize=False, add_generation_prompt=True)
    inputs = tokenizer(text, return_tensors="pt").to(model.device)
    with torch.inference_mode():
        generated = model.generate(
            **inputs,
            max_new_tokens=request.max_tokens,
            do_sample=request.temperature > 0,
            temperature=max(request.temperature, 1e-5),
            pad_token_id=tokenizer.eos_token_id,
        )
    answer = tokenizer.decode(generated[0][inputs.input_ids.shape[1]:], skip_special_tokens=True)
    return {
        "id": f"chatcmpl-{uuid.uuid4().hex}",
        "object": "chat.completion",
        "created": int(time.time()),
        "model": MODEL_NAME,
        "choices": [{"index": 0, "message": {"role": "assistant", "content": answer}, "finish_reason": "stop"}],
    }
