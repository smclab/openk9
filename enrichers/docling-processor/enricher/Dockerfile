ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim

FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG MODE=cpu
# Populated automatically by BuildKit from --platform (e.g. arm64, amd64).
ARG TARGETARCH

WORKDIR /docling-processor/

COPY ./requirements.txt ./
COPY ./requirements_cpu.txt ./
COPY ./requirements_arm64.txt ./

RUN echo "MODE=$MODE TARGETARCH=$TARGETARCH"
# MODE=gpu uses the CUDA requirements; otherwise pick CPU wheels by arch: the
# +cpu torch/torchvision wheels are x86_64-only, so arm64 builds use the
# default-PyPI aarch64 wheels (requirements_arm64) to run natively instead of
# under amd64 emulation.
RUN if [ "$MODE" = "gpu" ]; then \
        pip install --no-cache-dir -r requirements.txt; \
    elif [ "$TARGETARCH" = "arm64" ]; then \
        pip install --no-cache-dir -r requirements_arm64.txt; \
    else \
        pip install --no-cache-dir -r requirements_cpu.txt; \
    fi

COPY ./app ./app

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