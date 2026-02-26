# Build stage
ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim
FROM python:${PYTHON_BASE_DOCKER_IMAGE} AS builder

ARG MODE=cpu

WORKDIR /rag-module/

COPY ./requirements.txt ./

RUN apt-get -y update && \
    apt-get install --no-install-recommends -y \
        build-essential \
        libgl1 \
        libglib2.0-0 && \
    apt-get autoremove -y && \
    apt-get clean -y && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* && \
    if [ "$MODE" = "cpu" ]; then \
    sed -i '1i\--extra-index-url https://download.pytorch.org/whl/cpu\n' requirements.txt; \
    fi && \
    pip install --no-cache-dir -r requirements.txt

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
./app/external_services/grpc/searcher/searcher.proto \
./app/external_services/grpc/tenant_manager/tenant_manager.proto \
./app/external_services/grpc/embedding/embedding.proto

# Runtime stage
FROM python:${PYTHON_BASE_DOCKER_IMAGE}

WORKDIR /rag-module/

RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        libgl1 \
        libglib2.0-0 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=builder /usr/local /usr/local
COPY --from=builder /rag-module/app ./app
COPY --from=builder /rag-module/app/external_services/grpc/searcher/*.py ./app/external_services/grpc/searcher/
COPY --from=builder /rag-module/app/external_services/grpc/tenant_manager/*.py ./app/external_services/grpc/tenant_manager/
COPY --from=builder /rag-module/app/external_services/grpc/embedding/*.py ./app/external_services/grpc/embedding/

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]