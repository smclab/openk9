FROM python:3.6-alpine

COPY ./plugins/email/email-parser/requirements.txt /requirements.txt

RUN pip install -r requirements.txt

RUN pip install gunicorn

COPY ./plugins/email/email-parser/app /app

WORKDIR /app

CMD ["gunicorn", "-w", "2", "-t", "60", "-b", "0.0.0.0:80", "main:app"]