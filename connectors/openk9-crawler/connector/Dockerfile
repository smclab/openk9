FROM python:3.11.6-slim

# Install system deps (Playwright needs these)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl wget unzip dos2unix \
    libglib2.0-0 libnss3 libatk1.0-0 libatk-bridge2.0-0 \
    libdrm2 libxcomposite1 libxdamage1 libxrandr2 \
    libgbm1 libasound2 libpangocairo-1.0-0 libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

# Copy and install Python requirements
COPY requirements.txt /requirements.txt
RUN pip3 install --no-cache-dir -r requirements.txt

# --- PLAYWRIGHT FIX START ---
# Set persistent path for browsers
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN mkdir -p /ms-playwright && chmod -R 777 /ms-playwright
# Install browser with dependencies
RUN playwright install --with-deps webkit
# --- PLAYWRIGHT FIX END ---

# Copy scripts and configs
COPY docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY install_playwright.sh /etc/install_playwright.sh
COPY wait_for_scrapyd.sh /app/wait_for_scrapyd.sh
COPY scrapyd.conf /etc/scrapyd/scrapyd.conf

# Convert line endings and set permissions
RUN dos2unix /etc/install_playwright.sh /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod +x /etc/install_playwright.sh /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod g+rw /app

# Clean up apt cache to reduce image size
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy app
COPY app /app
WORKDIR /app

# Entrypoint
ENTRYPOINT ["/etc/docker-entrypoint.sh"]
