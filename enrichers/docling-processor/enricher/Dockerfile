ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE} AS builder

ARG MODE=cpu

WORKDIR /docling-processor/

# `cv2` ends up being the non-headless opencv-python that rapidocr pulls in,
# and it needs these libraries, which python:*-slim does not carry.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libxcb1 \
        libgl1 \
        libglib2.0-0t64 && \
    rm -rf /var/lib/apt/lists/*

COPY ./requirements.txt ./
COPY ./requirements_cpu.txt ./

RUN echo $MODE
RUN if [ "$MODE" = "gpu" ]; then \
        pip install --no-cache-dir -r requirements.txt; \
    else \
        pip install --no-cache-dir -r requirements_cpu.txt; \
    fi

COPY ./app ./app

# Run the unit tests in a dedicated stage: pytest and the tests never reach the
# runtime image. A failing test aborts the build because the runtime stage below
# depends on this stage's output.
FROM builder AS test

COPY ./tests ./tests

RUN pip install --no-cache-dir pytest==9.1.0 && \
    python -m pytest tests && \
    touch /tmp/tests.passed

FROM builder AS runtime

# Gate the build on the tests: this COPY forces the `test` stage to be built and
# to succeed before the runtime image can be assembled.
COPY --from=test /tmp/tests.passed /tmp/tests.passed

EXPOSE 5000

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]