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

WORKDIR /docling-processor/

COPY --from=ghcr.io/astral-sh/uv:0.13.0@sha256:cdc6093146eb3ff6a40107b38f008b789e050e77ad87865e381d9917da55a168 /uv /uvx /bin/

# `cv2` ends up being the non-headless opencv-python that rapidocr pulls in,
# and it needs these libraries, which python:*-slim does not carry.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        libxcb1 \
        libgl1 \
        libglib2.0-0t64 && \
    rm -rf /var/lib/apt/lists/*

COPY ./pyproject.toml ./uv.lock ./

# MODE=cpu installs the +cpu torch/torchvision wheels, available for both amd64
# and arm64; MODE=gpu installs the CUDA ones.
RUN uv sync --locked --no-default-groups --group "$MODE"

COPY ./app ./app

# Run the unit tests in a dedicated stage: pytest and the tests never reach the
# runtime image. A failing test aborts the build because the runtime stage below
# depends on this stage's output.
FROM builder AS test

ARG MODE=cpu

COPY ./tests ./tests

RUN uv sync --locked --no-default-groups --group "$MODE" --group dev && \
    python -m pytest tests && \
    touch /tmp/tests.passed

FROM builder AS runtime

# Gate the build on the tests: this COPY forces the `test` stage to be built and
# to succeed before the runtime image can be assembled.
COPY --from=test /tmp/tests.passed /tmp/tests.passed

EXPOSE 5000

# Run non-root. docling loads torch-based layout/OCR models at runtime, which
# resolve the user/home (getpwuid) and write a cache dir. Register the user, set
# HOME and point the torch/HF caches at writable dirs. chown 1001:0 + g=u keeps
# it OpenShift-friendly for arbitrary UIDs.
RUN useradd -m -u 1001 -g 0 docling
ENV USER=docling \
    HOME=/docling-processor \
    HF_HOME=/docling-processor/.cache/huggingface \
    TORCH_HOME=/docling-processor/.cache/torch \
    TORCHINDUCTOR_CACHE_DIR=/tmp/torchinductor \
    XDG_CACHE_HOME=/docling-processor/.cache
RUN mkdir -p /docling-processor/.cache && \
    chown -R 1001:0 /docling-processor && chmod -R g=u /docling-processor

USER 1001

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]