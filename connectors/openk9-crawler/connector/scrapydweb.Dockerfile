FROM python:3.11-slim

# Install utils
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl wget unzip dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Set timezone (adjust as needed)
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Upgrade pip and install Scrapydweb
RUN pip3 install --upgrade pip
RUN pip3 install scrapydweb

# Copy custom settings
COPY ./scrapydweb_settings_v11.py /app/scrapydweb_settings_v11.py

# Clean up apt cache to reduce image size
RUN apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Run Scrapydweb
CMD ["scrapydweb"]