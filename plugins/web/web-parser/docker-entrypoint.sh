#!/bin/sh

scrapyd & /app/wait_for_scrapyd.sh localhost:6800 && scrapyd-deploy
uvicorn main:app --host 0.0.0.0 --port 5000