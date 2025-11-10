#!/bin/sh

pid=$(cat /app/twistd.pid)
if ! kill -0 $pid 2>/dev/null; then
    echo "Process not running, removing stale pid file"
    rm /app/twistd.pid
else
    echo "Process is still running, do not remove pid file"
fi
scrapyd & /app/wait_for_scrapyd.sh localhost:6800 && scrapyd-deploy
scrapydweb & uvicorn main:app --host 0.0.0.0 --port 5000
