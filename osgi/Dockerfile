FROM smclab/apache-karaf:4.3.0

COPY ./build/libs/ opt/apache-karaf/deploy

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait

RUN chmod +x /wait
