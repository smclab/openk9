FROM python:3.11.6

# Install utils
RUN  apt-get update \
    && apt-get install -y wget \
    && apt-get install -y unzip \
    && apt-get install -y dos2unix \
    && rm -rf /var/lib/apt/lists/*

COPY ./connector/requirements.txt /requirements.txt

RUN pip3 install -r requirements.txt

ADD ./connector/docker-entrypoint.sh /etc/docker-entrypoint.sh

COPY ./connector/app /app

COPY ./connector/wait_for_scrapyd.sh /app/wait_for_scrapyd.sh

COPY ./connector/scrapyd.conf /etc/scrapyd/scrapyd.conf

RUN dos2unix /app/wait_for_scrapyd.sh
RUN dos2unix /etc/docker-entrypoint.sh

RUN chmod a+x /app/wait_for_scrapyd.sh
RUN chmod a+x /etc/docker-entrypoint.sh

RUN chmod g+rw /app

WORKDIR /app

RUN mkdir logs

ENTRYPOINT ["/bin/bash"]

CMD ["/etc/docker-entrypoint.sh"]

