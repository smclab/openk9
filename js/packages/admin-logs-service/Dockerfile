FROM node:14

ENV PORT 3005

RUN mkdir -p /app/packages/admin-logs-service
WORKDIR /app

COPY ./package*.json /app
COPY ./packages/admin-logs-service/package*.json /app/packages/admin-logs-service
RUN yarn install

COPY . /app
WORKDIR /app/packages/admin-logs-service
RUN yarn build
EXPOSE 3005

CMD "yarn" "run" "start"
