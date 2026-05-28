FROM node:18.18-alpine as react-build

RUN mkdir -p /app/js-packages/talk-to

COPY ./js-packages/talk-to /app/js-packages/talk-to
WORKDIR /app/js-packages/talk-to
RUN yarn install --network-timeout 600000 || \
    (echo "retry yarn install after network failure" && yarn install --network-timeout 600000)
RUN CI=false GENERATE_SOURCEMAP=false NODE_OPTIONS="--max-old-space-size=4096" NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:stable-alpine3.23-slim

# Usiamo path custom per evitare che nginx processi automaticamente il template
COPY ./js-packages/talk-to/nginx.conf.template /etc/nginx/nginx.conf.template
COPY ./js-packages/talk-to/nginx.conf.default /etc/nginx/nginx.conf.default

RUN mkdir -p /usr/share/nginx/html/chat/
COPY --from=react-build /app/js-packages/talk-to/build /usr/share/nginx/html/chat/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && sed -i 's|/run/nginx.pid|/tmp/nginx.pid|' /etc/nginx/nginx.conf \
    && sed -i 's/^user .*;//' /etc/nginx/nginx.conf \
    && mkdir -p /var/run \
                /var/cache/nginx/client_temp \
                /var/cache/nginx/proxy_temp \
                /var/cache/nginx/fastcgi_temp \
                /var/cache/nginx/uwsgi_temp \
                /var/cache/nginx/scgi_temp \
    && chown -R 1001:0 /var/cache/nginx /var/run /etc/nginx/conf.d /usr/share/nginx/html \
    && chmod -R g+rwX /var/cache/nginx /var/run /etc/nginx/conf.d /usr/share/nginx/html
EXPOSE 8080
USER 1001

# Se ENV_HOST è fornito, compila il template col proxy. Altrimenti usa il config standard.
# Usiamo envsubst '${ENV_HOST}' per evitare che sovrascriva le variabili interne di nginx come $host
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then cp /etc/nginx/nginx.conf.default /etc/nginx/conf.d/default.conf; else envsubst '${ENV_HOST}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
