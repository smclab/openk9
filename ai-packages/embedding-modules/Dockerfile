ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG MODE=cpu

RUN mkdir -p /var/log/openk9

WORKDIR /embedding_module/

COPY ./requirements.txt ./
COPY ./app ./app

RUN if [ "$MODE" = "cpu" ]; then \
        sed -i '1i\--extra-index-url https://download.pytorch.org/whl/cpu\n' requirements.txt; \
    fi && \
    pip install --no-cache-dir -r requirements.txt && \
    python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
    ./app/external_services/grpc/embedding/embedding.proto

EXPOSE 5000

CMD ["python", "-m", "app.server"]
