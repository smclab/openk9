FROM python:3.6-alpine

COPY ./email-parser/requirements.txt /requirements.txt

RUN pip install -r requirements.txt

RUN pip install gunicorn

COPY ./email-parser/app /app

WORKDIR /app