FROM node:18.18-alpine as react-build

RUN mkdir -p /app/js-packages/admin-ui

COPY ./js-packages/admin-ui /app/js-packages/admin-ui
WORKDIR /app/js-packages/admin-ui
RUN yarn install
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:1.25.3
COPY ./js-packages/admin-ui/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/chat/
COPY --from=react-build /app/js-packages/admin-ui/build /usr/share/nginx/html/chat/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
	&& chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
