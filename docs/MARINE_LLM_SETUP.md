# BluePath Marine AI Setup

BluePath routes quiz generation and AI counseling through the FastAPI server. The Android app contains no provider API key or model configuration screen.

- Server and Android environment setup: `docs/DEVELOPER_SETUP.md`
- Dataset generation, LoRA training, evaluation, serving, and deployment: `docs/FINE_TUNING_GUIDE.md`
- Learner-facing product capabilities: `README.md`

The backend adds RAG evidence, validates generated quiz structure, returns source metadata, and falls back to the verified offline quiz bank when the model is unavailable or produces an invalid response.
