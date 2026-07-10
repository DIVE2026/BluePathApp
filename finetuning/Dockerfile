FROM python:3.11-slim
WORKDIR /opt/bluepath
COPY finetuning/requirements.txt /opt/bluepath/requirements.txt
RUN pip install --no-cache-dir -r requirements.txt
COPY finetuning /opt/bluepath/finetuning
ENV PYTHONUNBUFFERED=1
CMD ["uvicorn", "finetuning.serve_model:app", "--host", "0.0.0.0", "--port", "8001"]
