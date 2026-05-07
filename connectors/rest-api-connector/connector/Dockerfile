# --- STAGE 1: Builder ---
FROM python:3.13-slim AS builder

# Use a virtual environment next stage copy
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN apt-get update && rm -rf /var/lib/apt/lists/*

COPY ./requirements.txt /requirements.txt
RUN pip install --no-cache-dir --upgrade pip==26.1.1 && \
    pip install --no-cache-dir -r /requirements.txt


# --- STAGE 2: Runtime ---
FROM python:3.13-slim

# Uninstall global pip, uses upgraded venv pip
RUN python -m pip uninstall -y pip

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Copy the compiled site-packages/binaries from the builder
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Setup entrypoint and app files
ADD ./docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY ./app /app
WORKDIR /app

# Final preparations
RUN dos2unix /etc/docker-entrypoint.sh && \
    chmod a+x /etc/docker-entrypoint.sh

# Security best practice: run as non-root
USER 1001

ENTRYPOINT ["/bin/bash"]
CMD ["/etc/docker-entrypoint.sh"]

