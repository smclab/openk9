ARG PYTHON_BASE_DOCKER_IMAGE=3.12-slim
FROM python:${PYTHON_BASE_DOCKER_IMAGE}

ARG MODE=cpu

WORKDIR /rag-module/



COPY ./requirements.txt ./

# Optimized single-layer RUN: installs system dependencies, python packages, and then purges build tools.
# This avoids Kaniko OutOfMemory (137) spikes caused by COPY --from=builder /usr/local
RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        build-essential \
        libgl1 \
        libglib2.0-0 && \
    if [ "$MODE" = "cpu" ]; then \
    sed -i '1i\--extra-index-url https://download.pytorch.org/whl/cpu\n' requirements.txt; \
    fi && \
    pip install --no-cache-dir -r requirements.txt && \
    apt-get purge -y --auto-remove build-essential && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY ./app ./app

RUN python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. \
./app/external_services/grpc/searcher/searcher.proto \
./app/external_services/grpc/embedding/embedding.proto

EXPOSE 5000

# Under USER 1001 without a /etc/passwd entry, libs that resolve the user/home
# (torch _inductor via langchain-docling, arize-phoenix) call getpwuid() and
# crash with "uid not found". Register the user, set HOME, and point the torch/HF
# caches at writable dirs. chown 1001:0 + g=u keeps it OpenShift-friendly for
# arbitrary UIDs.
RUN useradd -m -u 1001 -g 0 rag-module
ENV USER=rag-module \
    HOME=/rag-module \
    HF_HOME=/rag-module/.cache/huggingface \
    TORCH_HOME=/rag-module/.cache/torch \
    TORCHINDUCTOR_CACHE_DIR=/tmp/torchinductor \
    XDG_CACHE_HOME=/rag-module/.cache
RUN mkdir -p /rag-module/.cache && \
    chown -R 1001:0 /rag-module && chmod -R g=u /rag-module

USER 1001

CMD ["uvicorn", "app.server:app", "--host", "0.0.0.0", "--port", "5000"]