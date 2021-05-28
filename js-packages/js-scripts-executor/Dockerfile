FROM node:14-alpine

ENV PORT 3005

RUN mkdir -p /app/js-packages/js-scripts-executor
RUN mkdir -p /app/js-packages/js-scripts-executor/src
WORKDIR /app/js-packages/js-scripts-executor

COPY ./js-packages/js-scripts-executor/package*.json /app/js-packages/js-scripts-executor
RUN yarn install

COPY ./js-packages/js-scripts-executor/tsconfig.json /app/js-packages/js-scripts-executor/tsconfig.json
COPY ./js-packages/js-scripts-executor/src/* /app/js-packages/js-scripts-executor/src
WORKDIR /app/js-packages/js-scripts-executor
RUN yarn build
EXPOSE 3000

CMD "yarn" "run" "start"
