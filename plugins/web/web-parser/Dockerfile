FROM ubuntu:20.04

# Install OpenJDK-8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# Install dependencies
RUN apt-get -y install python3-pip

# Install utils
RUN  apt-get update \
  && apt-get install -y wget \
  && apt-get install -y unzip \
  && apt-get install -y dos2unix \
  && apt-get install -y nmap \
  && apt-get install -y curl \
  && apt-get install -y poppler-utils \
  && rm -rf /var/lib/apt/lists/*

COPY /plugins/web/web-parser/requirements.txt /requirements.txt

RUN pip3 install -r requirements.txt

RUN pip3 install gunicorn

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