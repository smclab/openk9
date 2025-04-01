ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

WORKDIR /rag-module/

COPY ./requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/searcher/searcher.proto && \
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/tenant_manager/tenant_manager.proto

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]
