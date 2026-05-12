#!/bin/sh

PID_FILE="/var/lib/scrapyd/twistd.pid"

# Checks for stale pid file
if [ -f "$PID_FILE" ]; then
    pid=$(cat "$PID_FILE")
    if ! kill -0 "$pid" 2>/dev/null; then
        echo "Process not running, removing stale pid file"
        rm "$PID_FILE"
    fi
fi

with_scrapydweb=${WITH_SCRAPYDWEB:-0}  # Default to 0

scrapyd --pidfile "$PID_FILE" & /app/wait_for_scrapyd.sh localhost:6800
if [ "$with_scrapydweb" = "1" ]; then
    echo "Starting ScrapydWeb..."
    scrapydweb & 
    exec uvicorn main:app --host 0.0.0.0 --port 5000
else
    exec uvicorn main:app --host 0.0.0.0 --port 5000
fi
