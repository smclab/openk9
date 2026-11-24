FROM node:18.18-alpine as react-build

RUN mkdir -p /app/js-packages/admin-ui

COPY ./js-packages/admin-ui /app/js-packages/admin-ui
WORKDIR /app/js-packages/admin-ui
RUN yarn install --frozen-lockfile --network-timeout 600000 || \
    (echo "retry yarn install after network failure" && yarn install --frozen-lockfile --network-timeout 600000)
RUN CI=false GENERATE_SOURCEMAP=false NODE_OPTIONS="--max-old-space-size=4096" NODE_ENV=production yarn build

# Stage 2 - the production environment
# Name the nginx version: stable-alpine3.x tags freeze when nginx moves Alpine.
FROM nginx:1.30.7-alpine3.24-slim@sha256:8c950211a641ec5b23f265450517001f6bcca533666120ebe36cb4cff3d00a55
RUN apk upgrade --no-cache

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
                /var/cache/nginx/client_temp \
                /var/cache/nginx/proxy_temp \
                /var/cache/nginx/fastcgi_temp \
                /var/cache/nginx/uwsgi_temp \
                /var/cache/nginx/scgi_temp \
    && chown -R 1001:0 /var/cache/nginx /etc/nginx/conf.d /usr/share/nginx/html \
    && chmod -R g+rwX /var/cache/nginx /etc/nginx/conf.d /usr/share/nginx/html
EXPOSE 8080
USER 1001

# Se ENV_HOST è fornito, compila il template col proxy. Altrimenti usa il config standard.
# Entrambi i rami passano da envsubst: la CSP contiene ${AUTH_ORIGIN}, l'origin
# dell'identity provider. La lista esplicita evita che envsubst tocchi $host.
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then envsubst '${AUTH_ORIGIN}' < /etc/nginx/nginx.conf.default > /etc/nginx/conf.d/default.conf; else envsubst '${ENV_HOST} ${AUTH_ORIGIN}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
