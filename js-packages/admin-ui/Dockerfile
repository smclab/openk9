FROM node:18.18-alpine as react-build

RUN mkdir -p /app/js-packages/admin-ui

COPY ./js-packages/admin-ui /app/js-packages/admin-ui
WORKDIR /app/js-packages/admin-ui
RUN yarn install
RUN CI=false GENERATE_SOURCEMAP=false NODE_OPTIONS="--max-old-space-size=4096" NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:stable-alpine3.23-slim

# Usiamo path custom per evitare che nginx processi automaticamente il template
COPY ./js-packages/admin-ui/nginx.conf.template /etc/nginx/nginx.conf.template
COPY ./js-packages/admin-ui/nginx.conf.default /etc/nginx/nginx.conf.default

RUN mkdir -p /usr/share/nginx/html/admin/
COPY --from=react-build /app/js-packages/admin-ui/build /usr/share/nginx/html/admin/
RUN sed -i 's|/run/nginx.pid|/tmp/nginx.pid|' /etc/nginx/nginx.conf \
    && sed -i 's/^user .*;//' /etc/nginx/nginx.conf \
    && rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run /etc/nginx/conf.d
EXPOSE 8080
USER 1001

# Se ENV_HOST è fornito, compila il template col proxy. Altrimenti usa il config standard.
# Usiamo envsubst '${ENV_HOST}' per evitare che sovrascriva le variabili interne di nginx come $host
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then cp /etc/nginx/nginx.conf.default /etc/nginx/conf.d/default.conf; else envsubst '${ENV_HOST}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
