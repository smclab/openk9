FROM python:3.11.6-slim

# Install utils (combine apt steps)
RUN apt-get update && \
    apt-get install -y curl wget unzip dos2unix

COPY requirements.txt /requirements.txt
RUN pip3 install --no-cache-dir -r requirements.txt  # Use --no-cache-dir for smaller image

# Copy scripts and app
ADD docker-entrypoint.sh /etc/docker-entrypoint.sh
COPY install_playwright.sh /etc/install_playwright.sh
COPY wait_for_scrapyd.sh /app/wait_for_scrapyd.sh
COPY scrapyd.conf /etc/scrapyd/scrapyd.conf

RUN dos2unix /etc/install_playwright.sh /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod +x /etc/install_playwright.sh /app/wait_for_scrapyd.sh /etc/docker-entrypoint.sh && \
    chmod g+rw /app  # If needed; consider chown if non-root

# Install Playwright browsers in image (NO mount - persists in layer)
# Run as root; --with-deps pulls system libs
RUN /etc/install_playwright.sh webkit  # Explicitly call with arg; removes need for default in script

# Removes apt temporary files and reduces image size
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

COPY app /app

WORKDIR /app

# Optional: Pin cache path (env propagates to runtime)
ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright

ENTRYPOINT ["/etc/docker-entrypoint.sh"]