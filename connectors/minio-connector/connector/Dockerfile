FROM python:3.11

COPY requirements.txt /requirements.txt

RUN pip install -r requirements.txt

COPY app /app

RUN chmod -R a+rwX /app

WORKDIR /app

USER 1001

CMD ["uvicorn", "main:app", "--reload", "--host", "0.0.0.0", "--port", "5000"]