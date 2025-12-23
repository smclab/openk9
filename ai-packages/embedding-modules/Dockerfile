ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG MODE=cpu

RUN mkdir -p /embedding_module  && \
mkdir -p /var/log/openk9
WORKDIR /embedding_module/

COPY ./requirements.txt ./
COPY ./requirements_cpu.txt ./
COPY ./app ./app

RUN echo "$MODE" && \
    if [ "$MODE" = "gpu" ]; then \
        pip install --no-cache-dir -r requirements.txt; \
    else \
        pip install --no-cache-dir -r requirements_cpu.txt; \
    fi && \
    python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
    ./app/external_services/grpc/embedding/embedding.proto

EXPOSE 5000

CMD ["python", "-m", "app.server"]
