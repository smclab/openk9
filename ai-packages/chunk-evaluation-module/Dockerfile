FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY server.py .
COPY metrics/ ./metrics/
COPY rabbit_manager/ ./rabbit_manager/

CMD ["python", "server.py"]