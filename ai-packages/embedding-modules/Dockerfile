ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG MODE=cpu

RUN mkdir -p /var/log/openk9 && chown -R 1001:0 /var/log/openk9 && chmod -R g=u /var/log/openk9

WORKDIR /embedding_module/

COPY ./requirements.txt ./


# Single-layer RUN: install build tools, build python packages, then purge the
# build tools so they don't bloat the final image (and to avoid Kaniko OOM-137 spikes).
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        build-essential && \
    if [ "$MODE" = "cpu" ]; then \
        sed -i '1i\--extra-index-url https://download.pytorch.org/whl/cpu\n' requirements.txt; \
    fi && \
    pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt && \
    apt-get purge -y --auto-remove build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY ./app ./app

RUN python -m grpc_tools.protoc \
        -I. \
        --python_out=. \
        --grpc_python_out=. \
        ./app/external_services/grpc/embedding/embedding.proto

EXPOSE 5000

USER 1001

CMD ["python", "-m", "app.server"]
