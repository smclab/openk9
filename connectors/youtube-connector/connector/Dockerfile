# --- STAGE 1: Estrattore Node.js ---
FROM node:22-slim AS node-bin

# --- STAGE 2: Python Builder ---
FROM python:3.13-slim AS builder

# Install ONLY the tools needed to compile and build
RUN apt-get update && apt-get install -y \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Creates venv
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY ./requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt

# Setup entrypoint
COPY ./docker-entrypoint.sh /etc/docker-entrypoint.sh
RUN dos2unix /etc/docker-entrypoint.sh && \
    chmod a+x /etc/docker-entrypoint.sh


# --- STAGE 3: Runtime ---
FROM python:3.13-slim

# Copy nodejs binaries
COPY --from=node-bin /usr/local/bin/node /usr/local/bin/node

# Copy venv
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN apt-get update && apt-get upgrade -y && apt-get install -y --no-install-recommends \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Setup app files
COPY ./app /app
WORKDIR /app

# Copy entrypoint from builder
COPY --from=builder /etc/docker-entrypoint.sh /etc/docker-entrypoint.sh

# Security best practice: run as non-root
USER 1001

ENTRYPOINT ["/bin/bash"]
CMD ["/etc/docker-entrypoint.sh"]

