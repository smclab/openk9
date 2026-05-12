# --- STAGE 1: Builder ---
FROM python:3.13-slim AS builder

# Install ONLY the tools needed to compile and build
RUN apt-get update && apt-get install -y \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Use a virtual environment next stage copy
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY ./requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt

# Setup entrypoint
COPY ./docker-entrypoint.sh /etc/docker-entrypoint.sh
RUN dos2unix /etc/docker-entrypoint.sh && \
    chmod a+x /etc/docker-entrypoint.sh


# --- STAGE 2: Runtime ---
FROM python:3.13-slim

# Copy the compiled site-packages/binaries from the builder
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Install ONLY runtime utilities
RUN apt-get update && apt-get upgrade -y \
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

