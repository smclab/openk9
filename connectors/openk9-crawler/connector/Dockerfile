# --- STAGE 1: Builder ---
FROM python:3.13-slim AS builder

ARG WITH_SCRAPYDWEB=0
ENV WITH_SCRAPYDWEB=$WITH_SCRAPYDWEB

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# Install ONLY the tools needed to compile and build
RUN apt-get update && apt-get install -y \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Use a virtual environment next stage copy
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY ./requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt
RUN if [ "$WITH_SCRAPYDWEB" = 1 ]; then \
    # Install scrapydweb and copy settings file
    # Also creates data folder
    echo "Installing Scrapydweb..."; \
    pip install --no-cache-dir scrapydweb; \
fi

RUN mkdir -p /var/lib/scrapydweb && \
    chown -R 1001:0 /var/lib/scrapydweb && \
    chmod -R 775 /var/lib/scrapydweb

# Install webkit browser
RUN PLAYWRIGHT_BROWSERS_PATH=/ms-playwright playwright install webkit

RUN mkdir -p /tld_cache
RUN tldextract --cache_dir /tld_cache --update || echo "Update failed, using local cached version"

# Setup entrypoint
COPY ./docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY wait_for_scrapyd.sh /app/wait_for_scrapyd.sh
# Convert line endings and set permissions
RUN dos2unix /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod +x /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod g+rw /app

# Create a dedicated scrapyd data and logs directory
RUN mkdir -p /var/lib/scrapyd/logs && \
    chown -R 1001:0 /var/lib/scrapyd && \
    chmod -R 775 /var/lib/scrapyd

# Build the egg file
COPY /app/scrapy.cfg .
RUN scrapyd-deploy --build-egg=project.egg

# --- STAGE 2: Runtime ---
FROM python:3.13-slim

ARG WITH_SCRAPYDWEB=0
ENV WITH_SCRAPYDWEB=$WITH_SCRAPYDWEB

ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
ENV TLDEXTRACT_CACHE=/tld_cache

# Copy the compiled site-packages/binaries from the builder
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Copy the browser binaries from builder
COPY --from=builder /ms-playwright /ms-playwright

# Copy the tldextract cache from builder
COPY --from=builder /tld_cache /tld_cache

COPY ./scrapydweb_settings_v11.py /app/scrapydweb_settings_v11.py

RUN apt-get update && apt-get upgrade -y && \
        # Install playwright to install dependencies
        python -m playwright install-deps webkit && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Setup app files
COPY ./app /app
WORKDIR /app

# Copy entrypoint from builder
COPY --from=builder /etc/docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY --from=builder /app/wait_for_scrapyd.sh /app/wait_for_scrapyd.sh

# Setup scrapyd conf
COPY scrapyd.conf /etc/scrapyd/scrapyd.conf

# Copy the empty, permissioned scrapyd and scrapydweb data directory from builder
COPY --from=builder --chown=1001:0 /var/lib/scrapyd /var/lib/scrapyd
COPY --from=builder --chown=1001:0 /var/lib/scrapydweb /var/lib/scrapydweb

# Copy the built scrapyd egg
COPY --from=builder --chown=1001:0 /project.egg /var/lib/scrapyd/eggs/generic_crawler/1.egg

# Security best practice: run as non-root
USER 1001

ENTRYPOINT ["/bin/bash"]
CMD ["/etc/docker-entrypoint.sh"]
