ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

# ---- base: dependencies, app code, generated gRPC stubs ----
FROM python:${PYTHON_BASE_DOCKER_IMAGE} AS base

ARG MODE=cpu

# The venv lives outside the WORKDIR, which is chowned to the runtime user
# below: chowning it would copy the whole venv into a new layer
ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    UV_PYTHON_DOWNLOADS=never \
    UV_NO_CACHE=1 \
    UV_PROJECT_ENVIRONMENT=/opt/venv \
    PATH="/opt/venv/bin:$PATH"

WORKDIR /embedding_module/

COPY --from=ghcr.io/astral-sh/uv:0.13.0@sha256:cdc6093146eb3ff6a40107b38f008b789e050e77ad87865e381d9917da55a168 /uv /uvx /bin/

COPY ./pyproject.toml ./uv.lock ./


# Single-layer RUN: install build tools, build python packages, then purge the
# build tools so they don't bloat the final image (and to avoid Kaniko OOM-137 spikes).
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        build-essential && \
    uv sync --locked --no-dev --no-install-project --extra "$MODE" && \
    apt-get purge -y --auto-remove build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY ./app ./app

RUN python -m grpc_tools.protoc \
        -I. \
        --python_out=. \
        --grpc_python_out=. \
        ./app/external_services/grpc/embedding/embedding.proto

# ---- test: quality gate; the runtime stage depends on this passing ----
FROM base AS test

ARG MODE=cpu

COPY ./tests ./tests

RUN uv sync --locked --no-install-project --extra "$MODE" --group dev && \
    python -m pytest && \
    touch /tests-passed

# ---- runtime ----
FROM base AS runtime

RUN mkdir -p /var/log/openk9 && chown -R 1001:0 /var/log/openk9 && chmod -R g=u /var/log/openk9

EXPOSE 5000

# Register the non-root user so libs resolving the user/home (getpwuid) don't
# crash under an unregistered uid, and give it a writable HOME/cache. chown
# 1001:0 + g=u keeps it OpenShift-friendly for arbitrary UIDs.
RUN useradd -m -u 1001 -g 0 embedding-module
ENV USER=embedding-module \
    HOME=/embedding_module \
    HF_HOME=/embedding_module/.cache/huggingface \
    TORCH_HOME=/embedding_module/.cache/torch \
    XDG_CACHE_HOME=/embedding_module/.cache
RUN mkdir -p /embedding_module/.cache && \
    chown -R 1001:0 /embedding_module && chmod -R g=u /embedding_module

# Quality gate: the runtime image cannot be built unless the test stage passed.
COPY --from=test /tests-passed /tests-passed

USER 1001

CMD ["python", "-m", "app.server"]
