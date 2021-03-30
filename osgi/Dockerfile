FROM smclab/apache-karaf:4.3.0

RUN mkdir -p /mnt/karaf/deploy

ENV KARAF_MOUNT_DIR=/mnt/karaf

COPY ./build/libs/ opt/apache-karaf/deploy

COPY scripts/* /usr/local/bin/

RUN chmod a+x /usr/local/bin/*

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait

RUN chmod +x /wait

ENTRYPOINT ["/bin/bash"]

CMD ["/usr/local/bin/docker-entrypoint.sh"]