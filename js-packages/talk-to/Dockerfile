FROM node:18.18-alpine as react-build

RUN mkdir -p /app/js-packages/talk-to

COPY ./js-packages/talk-to /app/js-packages/talk-to
WORKDIR /app/js-packages/talk-to
RUN yarn install
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:1.28.2-alpine
COPY ./js-packages/talk-to/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/chat/
COPY --from=react-build /app/js-packages/talk-to/build /usr/share/nginx/html/chat/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
	&& chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
