FROM python:3.6

COPY ./liferay-parser/requirements.txt /requirements.txt

RUN pip install -r requirements.txt

RUN pip install gunicorn

COPY ./liferay-parser/app /app

WORKDIR /app