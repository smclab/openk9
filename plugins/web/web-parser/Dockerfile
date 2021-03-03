FROM python:3.6

COPY ./web-parser/requirements.txt /requirements.txt

RUN apt-get update && apt-get install dos2unix

RUN pip install -r requirements.txt

RUN pip install gunicorn

COPY ./web-parser/app /app

COPY ./web-parser/app/scrapyd.conf /etc/scrapyd/scrapyd.conf

RUN dos2unix /app/wait_for_scrapyd.sh

RUN chmod a+x /app/wait_for_scrapyd.sh

WORKDIR /app