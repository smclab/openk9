#!/bin/sh

pid=$(cat /app/twistd.pid)
if ! kill -0 $pid 2>/dev/null; then
    echo "Process not running, removing stale pid file"
    rm /app/twistd.pid
else
    echo "Process is still running, do not remove pid file"
fi

with_scrapydweb=${WITH_SCRAPYDWEB}
if [ $with_scrapydweb -ne 1 ] && [ $with_scrapydweb -ne 0 ]; then
  with_scrapydweb=0;
fi

scrapyd & /app/wait_for_scrapyd.sh localhost:6800 && scrapyd-deploy
if [ $with_scrapydweb -eq 1 ]; then
  scrapydweb & uvicorn main:app --host 0.0.0.0 --port 5000
else
  uvicorn main:app --host 0.0.0.0 --port 5000
fi
