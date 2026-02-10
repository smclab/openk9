FROM node:18.18-alpine as react-build

RUN mkdir -p /app/js-packages/admin-ui

COPY ./js-packages/admin-ui /app/js-packages/admin-ui
WORKDIR /app/js-packages/admin-ui
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}
RUN yarn install
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:1.28.2-alpine
COPY ./js-packages/admin-ui/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/admin/
COPY --from=react-build /app/js-packages/admin-ui/build /usr/share/nginx/html/admin/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
	&& chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
