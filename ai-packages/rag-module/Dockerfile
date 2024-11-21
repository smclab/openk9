FROM python:3.12-slim

RUN mkdir -p /rag-module
WORKDIR /rag-module/
COPY ./requirements.txt ./
COPY ./app ./app

RUN pip install -r requirements.txt
RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/searcher/searcher.proto
RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/tenant_manager/tenant_manager.proto

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]