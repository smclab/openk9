#!/bin/sh

exec java -jar /tika-server-1.27.jar org.apache.tika.server.TikaServerCli -h 0.0.0.0 $$0 $$@ -c /ocr/tika-config.xml &
scrapyd & /app/wait_for_scrapyd.sh localhost:6800 && scrapyd-deploy
uvicorn main:app --host 0.0.0.0 --port 80