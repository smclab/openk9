# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/js-packages/rest-api/src
RUN mkdir -p /app/js-packages/search-frontend/src
RUN mkdir -p /app/js-packages/search-frontend/public

COPY ./package*.json /app
COPY ./js-packages/rest-api/package*.json ./js-packages/rest-api/rollup.config.js ./js-packages/rest-api/tsconfig.json /app/js-packages/rest-api/
COPY ./js-packages/rest-api/src /app/js-packages/rest-api/src
COPY ./js-packages/search-frontend/package*.json ./js-packages/search-frontend/tsconfig.json ./js-packages/search-frontend/*.js ./js-packages/search-frontend/nginx.conf /app/js-packages/search-frontend/
COPY ./js-packages/search-frontend/src /app/js-packages/search-frontend/src
COPY ./js-packages/search-frontend/public /app/js-packages/search-frontend/public
COPY ./yarn.lock /app

WORKDIR /app
RUN yarn install --frozen-lockfile

#COPY ./ /app
WORKDIR /app/js-packages/rest-api
RUN yarn build
WORKDIR /app/js-packages/search-frontend
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./js-packages/search-frontend/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-frontend/dist /usr/share/nginx/html/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
	&& chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
