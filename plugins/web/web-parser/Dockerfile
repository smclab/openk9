FROM python:3.8

# Install utils
RUN  apt-get update \
  && apt-get install -y wget \
  && apt-get install -y unzip \
  && apt-get install -y dos2unix \
  && rm -rf /var/lib/apt/lists/*

COPY /plugins/web/web-parser/requirements.txt /requirements.txt

RUN pip3 install -r requirements.txt

ADD /plugins/web/web-parser/docker-entrypoint.sh /etc/docker-entrypoint.sh

COPY /plugins/web/web-parser/app /app

COPY ./plugins/web/web-parser/wait_for_scrapyd.sh /app/wait_for_scrapyd.sh

COPY ./plugins/web/web-parser/scrapyd.conf /etc/scrapyd/scrapyd.conf

RUN dos2unix /etc/docker-entrypoint.sh

RUN dos2unix /app/wait_for_scrapyd.sh

RUN chmod a+x /app/wait_for_scrapyd.sh

WORKDIR /app

ENTRYPOINT ["/bin/bash"]

CMD ["/etc/docker-entrypoint.sh"]