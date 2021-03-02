# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/patches
RUN mkdir -p /app/js-packages/http-api
RUN mkdir -p /app/js-packages/search-ui-components
RUN mkdir -p /app/js-packages/search-standalone-frontend
WORKDIR /app

COPY ./patches/*.patch /app/patches
COPY ./package*.json /app
COPY ./js-packages/http-api/package*.json /app/js-packages/http-api
COPY ./js-packages/search-ui-components/package*.json /app/js-packages/search-ui-components
COPY ./js-packages/search-standalone-frontend/package*.json /app/js-packages/search-standalone-frontend
COPY ./yarn.lock /app
RUN yarn install --frozen-lockfile

COPY ./ /app
WORKDIR /app/js-packages/search-standalone-frontend
RUN yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./js-packages/search-standalone-frontend/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-standalone-frontend/build /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
