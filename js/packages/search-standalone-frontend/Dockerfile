# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/packages/http-api
RUN mkdir -p /app/packages/search-ui-components
RUN mkdir -p /app/packages/search-standalone-frontend
WORKDIR /app

COPY ./package*.json /app
COPY ./packages/http-api/package*.json /app/packages/http-api
COPY ./packages/search-ui-components/package*.json /app/packages/search-ui-components
COPY ./packages/search-standalone-frontend/package*.json /app/packages/search-standalone-frontend
COPY ./yarn.lock /app
RUN yarn install --frozen-lockfile

COPY ./ /app
WORKDIR /app/packages/search-standalone-frontend
RUN yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./packages/search-standalone-frontend/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/packages/search-standalone-frontend/build /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
