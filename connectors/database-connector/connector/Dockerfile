FROM python:3.12

COPY ./connector/requirements.txt /requirements.txt

# Install utils
RUN  apt-get update \
    && apt-get install -y wget \
    && apt-get install -y unzip \
    && apt-get install -y dos2unix \
    && rm -rf /var/lib/apt/lists/*

RUN pip3 install -r requirements.txt

ADD ./connector/docker-entrypoint.sh /etc/docker-entrypoint.sh

COPY ./connector/app /app

WORKDIR /app

RUN dos2unix /etc/docker-entrypoint.sh

RUN chmod a+x /etc/docker-entrypoint.sh

ENTRYPOINT ["/bin/bash"]

CMD ["/etc/docker-entrypoint.sh"]
