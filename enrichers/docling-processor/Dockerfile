ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim
ARG MODE=cpu

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

WORKDIR /docling-processor/

COPY ./requirements.txt ./
COPY ./requirements_cpu.txt ./

RUN echo $MODE
RUN if [ "$MODE" = "gpu" ]; then \
        pip install --no-cache-dir -r requirements.txt; \
    else \
        pip install --no-cache-dir -r requirements_cpu.txt; \
    fi

COPY ./app ./app

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]