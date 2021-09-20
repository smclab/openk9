FROM apache/tika:1.27-full

ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies
RUN apt-get update \
  && apt-get -y install python3-pip

# Install utils
RUN  apt-get update \
  && apt-get install -y wget \
  && apt-get install -y unzip \
  && apt-get install -y dos2unix \
  && apt-get install -y nmap \
  && apt-get install -y curl \
  && apt-get install -y poppler-utils \
  && apt-get install -y zip \
  && rm -rf /var/lib/apt/lists/*

COPY /plugins/web/web-parser/requirements.txt /requirements.txt

RUN pip3 install -r requirements.txt

RUN pip3 install gunicorn

ADD /plugins/web/web-parser/docker-entrypoint.sh /etc/docker-entrypoint.sh

COPY /plugins/web/web-parser/app /app

COPY ./plugins/web/web-parser/wait_for_scrapyd.sh /app/wait_for_scrapyd.sh

COPY ./plugins/web/web-parser/scrapyd.conf /etc/scrapyd/scrapyd.conf

COPY ./plugins/web/web-parser/tika-configs/ocr /ocr

RUN dos2unix /etc/docker-entrypoint.sh

RUN dos2unix /app/wait_for_scrapyd.sh

RUN chmod a+x /app/wait_for_scrapyd.sh

RUN unzip tika-server-1.27.jar org/apache/tika/parser/ocr/TesseractOCRConfig.properties  -d .
RUN cp /ocr/org/apache/tika/parser/ocr/TesseractOCRConfig.properties org/apache/tika/parser/ocr/TesseractOCRConfig.properties
RUN zip tika-server-1.27.jar org/apache/tika/parser/ocr/TesseractOCRConfig.properties

WORKDIR /app

ENTRYPOINT ["/bin/bash"]

CMD ["/etc/docker-entrypoint.sh"]