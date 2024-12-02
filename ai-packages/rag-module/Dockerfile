FROM python:3.12-slim

COPY ./requirements.txt ./

RUN pip install -r requirements.txt && \
mkdir -p /rag-module \

WORKDIR /rag-module/

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/searcher/searcher.proto && \
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. ./app/external_services/grpc/tenant_manager/tenant_manager.proto

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]
