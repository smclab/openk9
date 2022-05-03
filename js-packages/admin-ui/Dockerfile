FROM node:14-alpine

RUN mkdir -p /app/js-packages/rest-api
RUN mkdir -p /app/js-packages/search-frontend
RUN mkdir -p /app/js-packages/admin-ui

COPY ./package.json /app
COPY ./yarn.lock /app
COPY ./js-packages/rest-api/package.json /app/js-packages/rest-api
COPY ./js-packages/search-frontend/package.json /app/js-packages/search-frontend
COPY ./js-packages/admin-ui/package.json /app/js-packages/admin-ui

WORKDIR /app
RUN yarn install --frozen-lockfile

COPY ./js-packages/rest-api /app/js-packages/rest-api
WORKDIR /app/js-packages/rest-api
RUN yarn build

COPY ./js-packages/search-frontend /app/js-packages/search-frontend
WORKDIR /app/js-packages/search-frontend
RUN NODE_ENV=production yarn build

COPY ./js-packages/admin-ui /app/js-packages/admin-ui
WORKDIR /app/js-packages/admin-ui
# RUN yarn build

EXPOSE 3000
# CMD "yarn" "run" "start"
CMD "yarn" "run" "dev"
