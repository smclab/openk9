# --- STAGE 1: Builder ---
FROM python:3.13-slim AS builder

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# upgrade pip
RUN python -m pip install --upgrade pip==26.0

# Use a virtual environment next stage copy
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN apt-get update && rm -rf /var/lib/apt/lists/*

COPY ./requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt

# Install webkit browser
RUN PLAYWRIGHT_BROWSERS_PATH=/ms-playwright playwright install webkit

RUN mkdir -p /tld_cache
RUN tldextract --cache_dir /tld_cache --update || echo "Update failed, using local cached version"


# --- STAGE 2: Runtime ---
FROM python:3.13-slim

ARG WITH_SCRAPYDWEB=0
ENV WITH_SCRAPYDWEB=$WITH_SCRAPYDWEB

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
ENV TLDEXTRACT_CACHE=/tld_cache

# Upgrade pip
RUN python -m pip install --upgrade pip==26.0

# Copy the compiled site-packages/binaries from the builder
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Copy the browser binaries from builder
COPY --from=builder /ms-playwright /ms-playwright

# Copy the tldextract cache from builder
COPY --from=builder /tld_cache /tld_cache

COPY ./app /app

COPY ./scrapydweb_settings_v11.py /app/scrapydweb_settings_v11.py

RUN apt-get update && apt-get upgrade -y && apt-get install -y \
        curl dos2unix && \
        # Install playwright to install dependencies
        pip install playwright && \
        playwright install-deps webkit && \
        if [ "$WITH_SCRAPYDWEB" = 1 ]; then \
            # Install scrapydweb and copy settings file
            echo "Installing Scrapydweb..."; \
            pip install scrapydweb; \
        fi && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


# Setup entrypoint and app files
COPY ./docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY wait_for_scrapyd.sh /app/wait_for_scrapyd.sh
COPY scrapyd.conf /etc/scrapyd/scrapyd.conf

WORKDIR /app

# Convert line endings and set permissions
RUN dos2unix /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod +x /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod g+rw /app

# Security best practice: run as non-root
USER 1001

ENTRYPOINT ["/bin/bash"]
CMD ["/etc/docker-entrypoint.sh"]
