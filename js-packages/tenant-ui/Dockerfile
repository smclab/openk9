FROM node:16.18-alpine as react-build

COPY ./js-packages/tenant-ui /app
COPY ./yarn.lock /app

WORKDIR /app

RUN yarn install --frozen-lockfile
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/build /usr/share/nginx/html/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
	&& chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
