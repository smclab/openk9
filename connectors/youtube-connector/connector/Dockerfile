# --- STAGE 1: Estrattore Node.js ---
FROM node:22-slim AS node-bin

# --- STAGE 2: Python Builder ---
FROM python:3.13-slim AS python-builder

# Creates venv
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip==26.1.1 && \
    pip install --no-cache-dir -r requirements.txt


# --- STAGE 3: Runtime ---
FROM python:3.13-slim

# Uninstall global pip
RUN python -m pip uninstall -y pip

# Copy venv
COPY --from=python-builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"


# Copy nodejs binaries
COPY --from=node-bin /usr/local/bin/node /usr/local/bin/node

RUN apt-get update && apt-get upgrade && apt-get install -y --no-install-recommends \
    ffmpeg dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Setup entrypoint and app files
COPY ./docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY ./app /app
WORKDIR /app

# Final preparations
RUN dos2unix /etc/docker-entrypoint.sh && \
    chmod a+x /etc/docker-entrypoint.sh

# Security best practice: run as non-root
USER 1001

ENTRYPOINT ["/bin/bash"]
CMD ["/etc/docker-entrypoint.sh"]

