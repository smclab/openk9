FROM postgres:17
COPY create-multiple-postgresql-databases.sh /docker-entrypoint-initdb.d/
# Keep it non-executable so the postgres entrypoint SOURCES it (env-driven bash),
# avoiding the macOS bind-mount exec quirk.
RUN chmod 644 /docker-entrypoint-initdb.d/create-multiple-postgresql-databases.sh
