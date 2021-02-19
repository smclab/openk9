FROM node:14-alpine

ENV PORT 3000

RUN mkdir -p /app/packages/http-api
RUN mkdir -p /app/packages/search-ui-components
RUN mkdir -p /app/packages/admin-ui
WORKDIR /app

COPY ./package*.json /app
COPY ./packages/http-api/package*.json /app/packages/http-api
COPY ./packages/search-ui-components/package*.json /app/packages/search-ui-components
COPY ./packages/admin-ui/package*.json /app/packages/admin-ui
COPY ./yarn.lock /app
RUN yarn install --frozen-lockfile

COPY . /app
WORKDIR /app/packages/admin-ui
RUN yarn build
EXPOSE 3000

CMD "yarn" "run" "start"
