#!/bin/sh

scrapyd & /app/wait_for_scrapyd.sh localhost:6800 && scrapyd-deploy && gunicorn -w 1 -t 120 -b 0.0.0.0:80 main:app