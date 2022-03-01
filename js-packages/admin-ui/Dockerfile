FROM node:14-alpine

ENV PORT 3000

RUN mkdir -p /app/patches
RUN mkdir -p /app/js-packages/rest-api
RUN mkdir -p /app/js-packages/search-frontend
RUN mkdir -p /app/js-packages/search-ui-components
RUN mkdir -p /app/js-packages/admin-ui
WORKDIR /app

COPY ./package*.json /app
# COPY ./patches/*.patch /app/patches/
COPY ./js-packages/rest-api/package*.json /app/js-packages/rest-api
COPY ./js-packages/search-frontend/package*.json /app/js-packages/search-frontend
COPY ./js-packages/search-ui-components/package*.json /app/js-packages/search-ui-components
COPY ./js-packages/admin-ui/package*.json /app/js-packages/admin-ui
COPY ./yarn.lock /app
RUN yarn install --frozen-lockfile

COPY . /app
WORKDIR /app/js-packages/rest-api
RUN yarn build
WORKDIR /app/js-packages/search-frontend
RUN yarn build
WORKDIR /app/js-packages/admin-ui
RUN yarn build

EXPOSE 3000
CMD "yarn" "run" "start"
