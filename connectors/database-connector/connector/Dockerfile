# --- STAGE 1: Builder ---
FROM python:3.13-slim AS builder

# Upgrade pip (removes vulnerabilities)
RUN python -m pip install --upgrade pip==26.0

# Install ONLY the tools needed to compile and build
RUN apt-get update && apt-get install -y \
    gcc \
    libpq-dev \
    freetds-dev \
    && rm -rf /var/lib/apt/lists/*

# Use a virtual environment next stage copy
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY ./requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt


# --- STAGE 2: Runtime ---
FROM python:3.13-slim

# Upgrade pip (removes vulnerabilities)
RUN python -m pip install --upgrade pip==26.0

# Install ONLY runtime utilities
# libpq5 is the runtime library for postgres
RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    wget \
    unzip \
    dos2unix \
    libpq5 \
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
