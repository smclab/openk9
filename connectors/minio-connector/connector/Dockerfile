FROM python:3.13-slim

COPY requirements.txt /requirements.txt

RUN apt-get update \
    && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/*

RUN pip install -r requirements.txt

COPY app /app

RUN chmod -R a+rwX /app

WORKDIR /app

CMD ["uvicorn", "main:app", "--reload", "--host", "0.0.0.0", "--port", "5000"]