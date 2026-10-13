ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim
FROM python:${PYTHON_BASE_DOCKER_IMAGE} AS builder

# The venv lives outside the WORKDIR, which is chowned to the runtime user
# below: chowning it would copy the whole venv into a new layer
ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    UV_PYTHON_DOWNLOADS=never \
    UV_NO_CACHE=1 \
    UV_PROJECT_ENVIRONMENT=/opt/venv \
    PATH="/opt/venv/bin:$PATH"

WORKDIR /app

COPY --from=ghcr.io/astral-sh/uv:0.13.0@sha256:cdc6093146eb3ff6a40107b38f008b789e050e77ad87865e381d9917da55a168 /uv /uvx /bin/

# Copy the dependency manifests first for better layer caching
COPY pyproject.toml uv.lock ./

# Install dependencies with optimizations
# build-essential is required to compile native extensions (e.g. chonkie's Cython
# split extension) when no prebuilt wheel is available for the target platform.
RUN apt-get update && \
    apt-get install --no-install-recommends -y build-essential && \
    uv sync --locked --no-dev && \
    apt-get purge -y --auto-remove build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Copy application code
COPY app/ ./

# Run the unit tests in a dedicated stage: pytest and the tests never reach the
# runtime image. A failing test aborts the build because the runtime stage below
# depends on this stage's output.
FROM builder AS test

COPY tests/ ./tests/

RUN uv sync --locked --group dev && \
    python -m pytest tests && \
    touch /tmp/tests.passed

FROM builder AS runtime

# Gate the build on the tests: this COPY forces the `test` stage to be built and
# to succeed before the runtime image can be assembled.
COPY --from=test /tmp/tests.passed /tmp/tests.passed

# Run as non-root user for security
RUN useradd -m -u 1001 appuser && \
    chown -R appuser:appuser /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD python -c "import sys; sys.exit(0)"

CMD ["python", "server.py"]