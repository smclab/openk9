FROM node:18-alpine as react-build

WORKDIR /app

# Install from the workspace root so the root package.json
# resolutions apply; copy only manifests for better caching
COPY ./package.json ./yarn.lock ./
COPY ./js-packages/tenant-ui/package.json ./js-packages/tenant-ui/package.json
RUN yarn install --frozen-lockfile --network-timeout 600000 || \
    (echo "retry yarn install after network failure" && yarn install --frozen-lockfile --network-timeout 600000)

COPY ./js-packages/tenant-ui ./js-packages/tenant-ui
WORKDIR /app/js-packages/tenant-ui
RUN CI=false GENERATE_SOURCEMAP=false NODE_OPTIONS="--max-old-space-size=4096" NODE_ENV=production yarn build

# Stage 2 - the production environment
# Name the nginx version: stable-alpine3.x tags freeze when nginx moves Alpine.
FROM nginx:1.30.7-alpine3.24-slim@sha256:8c950211a641ec5b23f265450517001f6bcca533666120ebe36cb4cff3d00a55
RUN apk upgrade --no-cache

# Usiamo path custom per evitare che nginx processi automaticamente il template
COPY ./js-packages/tenant-ui/nginx.conf.template /etc/nginx/nginx.conf.template
COPY ./js-packages/tenant-ui/nginx.conf.default /etc/nginx/nginx.conf.default

RUN mkdir -p /usr/share/nginx/html/admin/
COPY --from=react-build /app/js-packages/tenant-ui/build /usr/share/nginx/html/admin/
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
# RESOLVER viene estratto da /etc/resolv.conf per funzionare su qualsiasi cluster.
# Usiamo envsubst '${ENV_HOST} ${RESOLVER}' per sostituire solo le nostre variabili
# senza toccare le variabili interne di nginx come $host, $remote_addr, ecc.
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then cp /etc/nginx/nginx.conf.default /etc/nginx/conf.d/default.conf; else export RESOLVER=$(grep nameserver /etc/resolv.conf | awk '{print $2}' | head -1); envsubst '${ENV_HOST} ${RESOLVER}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
