ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim
FROM python:${PYTHON_BASE_DOCKER_IMAGE} AS builder

ARG MODE=cpu

# The venv lives outside the WORKDIR, which is chowned to the runtime user
# below: chowning it would copy the whole venv into a new layer
ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    UV_PYTHON_DOWNLOADS=never \
    UV_NO_CACHE=1 \
    UV_PROJECT_ENVIRONMENT=/opt/venv \
    PATH="/opt/venv/bin:$PATH"

WORKDIR /agentic-rag-module/

COPY --from=ghcr.io/astral-sh/uv:0.13.0@sha256:cdc6093146eb3ff6a40107b38f008b789e050e77ad87865e381d9917da55a168 /uv /uvx /bin/

COPY ./pyproject.toml ./uv.lock ./

# Optimized single-layer RUN: installs system dependencies, python packages, and then purges build tools.
# This avoids Kaniko OutOfMemory (137) spikes caused by COPY --from=builder /usr/local
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        build-essential \
        libgl1 \
        libglib2.0-0 && \
    uv sync --locked --no-dev --no-install-project --extra "$MODE" && \
    apt-get purge -y --auto-remove build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
./app/external_services/grpc/searcher/searcher.proto \
./app/external_services/grpc/embedding/embedding.proto

# Run the unit tests in a dedicated stage: pytest and the tests never reach the
# runtime image. A failing test aborts the build because the runtime stage below
# depends on this stage's output.
FROM builder AS test

ARG MODE=cpu

COPY ./tests ./tests

RUN uv sync --locked --no-install-project --extra "$MODE" --group dev && \
    python -m pytest tests && \
    touch /tmp/tests.passed

FROM builder AS runtime

# Gate the build on the tests: this COPY forces the `test` stage to be built and
# to succeed before the runtime image can be assembled.
COPY --from=test /tmp/tests.passed /tmp/tests.passed

EXPOSE 5000

# arize-phoenix writes its working dir to $HOME/.phoenix, and libs that resolve
# the user (getpwuid) crash if uid 1001 has no /etc/passwd entry. Register the
# user, point HOME at the writable WORKDIR and set USER explicitly.
RUN useradd -m -u 1001 -g 0 agentic-rag-module
ENV HOME=/agentic-rag-module
ENV USER=agentic-rag-module
RUN mkdir -p /agentic-rag-module/.phoenix && chown -R 1001:0 /agentic-rag-module && chmod -R g=u /agentic-rag-module

USER 1001

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]
