ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG PYTORCH_CPU=false

WORKDIR /rag-module/

COPY ./requirements.txt ./

RUN if [ "$PYTORCH_CPU" = false ]; then \
        pip install --no-cache-dir -r requirements.txt; \
    else \
        ${PYTORCH_CPU} && \
        pip install --no-cache-dir -r requirements.txt; \
    fi && \
    apt-get -y update && \
    apt-get install --no-install-recommends -y \
        libgl1 \
        libglib2.0-0 && \
    apt-get autoremove -y && \
    apt-get clean -y && \
    rm -rf /var/lib/apt/lists/*    

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
./app/external_services/grpc/searcher/searcher.proto ./app/external_services/grpc/tenant_manager/tenant_manager.proto ./app/external_services/grpc/embedding/embedding.proto

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]
