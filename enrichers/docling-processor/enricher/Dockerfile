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
    XDG_CACHE_HOME=/docling-processor/.cache \
    EASYOCR_MODULE_PATH=/docling-processor/.EasyOCR
# Bake the PDF pipeline models (layout, TableFormer, EasyOCR) into the image so
# the pod needs no egress at runtime. `docling-tools models download` is not
# enough on docling 2.126: the layout model reads the HuggingFace cache, which
# the CLI does not fill. Building the real pipeline goes through every download.
RUN python -c "from docling.datamodel.base_models import InputFormat; \
from docling.document_converter import DocumentConverter; \
from app.utils.pipeline_options import get_format_options; \
DocumentConverter(format_options=get_format_options({}, InputFormat.PDF)).initialize_pipeline(InputFormat.PDF)"
RUN mkdir -p /docling-processor/.cache && \
    chown -R 1001:0 /docling-processor && chmod -R g=u /docling-processor

USER 1001

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]