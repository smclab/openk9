# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/js-packages/search-frontend

COPY ./package.json /app
COPY ./yarn.lock /app
COPY ./js-packages/search-frontend/package.json /app/js-packages/search-frontend/

WORKDIR /app
RUN yarn install --frozen-lockfile

COPY ./js-packages/search-frontend /app/js-packages/search-frontend
WORKDIR /app/js-packages/search-frontend
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:1.25.3
COPY ./js-packages/search-frontend/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-frontend/dist /usr/share/nginx/html/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
	&& chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
