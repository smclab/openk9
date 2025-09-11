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
    fi

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
./app/external_services/grpc/searcher/searcher.proto ./app/external_services/grpc/tenant_manager/tenant_manager.proto

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]
