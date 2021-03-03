FROM node:14-alpine

RUN apk update && apk add --no-cache docker-cli

ENV PORT 3005

RUN mkdir -p /app/js-packages/admin-logs-service
RUN mkdir -p /app/js-packages/admin-logs-service/src
WORKDIR /app/js-packages/admin-logs-service

COPY ./js-packages/admin-logs-service/package*.json /app/js-packages/admin-logs-service
RUN yarn install

COPY ./js-packages/admin-logs-service/tsconfig.json /app/js-packages/admin-logs-service/tsconfig.json
COPY ./js-packages/admin-logs-service/src/* /app/js-packages/admin-logs-service/src
WORKDIR /app/js-packages/admin-logs-service
RUN yarn build
EXPOSE 3005

CMD "yarn" "run" "start"
