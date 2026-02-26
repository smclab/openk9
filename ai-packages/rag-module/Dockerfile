ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim
FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG MODE=cpu

WORKDIR /rag-module/

COPY ./requirements.txt ./

# Optimized single-layer RUN: installs system dependencies, python packages, and then purges build tools.
# This avoids Kaniko OutOfMemory (137) spikes caused by COPY --from=builder /usr/local
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        build-essential \
        libgl1 \
        libglib2.0-0 && \
    if [ "$MODE" = "cpu" ]; then \
    sed -i '1i\--extra-index-url https://download.pytorch.org/whl/cpu\n' requirements.txt; \
    fi && \
    pip install --no-cache-dir -r requirements.txt && \
    apt-get purge -y --auto-remove build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
./app/external_services/grpc/searcher/searcher.proto \
./app/external_services/grpc/tenant_manager/tenant_manager.proto \
./app/external_services/grpc/embedding/embedding.proto

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]