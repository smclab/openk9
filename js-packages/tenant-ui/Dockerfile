FROM node:18-alpine as react-build

COPY ./js-packages/tenant-ui /app
COPY ./yarn.lock /app

WORKDIR /app

RUN yarn install
RUN CI=false GENERATE_SOURCEMAP=false NODE_OPTIONS="--max-old-space-size=4096" NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:stable-alpine3.23-slim

# Usiamo path custom per evitare che nginx processi automaticamente il template
COPY ./js-packages/tenant-ui/nginx.conf.template /etc/nginx/nginx.conf.template
COPY ./js-packages/tenant-ui/nginx.conf.default /etc/nginx/nginx.conf.default

RUN mkdir -p /usr/share/nginx/html/admin/
COPY --from=react-build /app/build /usr/share/nginx/html/admin/
RUN sed -i 's|/run/nginx.pid|/tmp/nginx.pid|' /etc/nginx/nginx.conf \
    && sed -i 's/^user .*;//' /etc/nginx/nginx.conf \
    && rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run /etc/nginx/conf.d
EXPOSE 8080
USER 1001

# Se ENV_HOST è fornito, compila il template col proxy. Altrimenti usa il config standard.
# RESOLVER viene estratto da /etc/resolv.conf per funzionare su qualsiasi cluster.
# Usiamo envsubst '${ENV_HOST} ${RESOLVER}' per sostituire solo le nostre variabili
# senza toccare le variabili interne di nginx come $host, $remote_addr, ecc.
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then cp /etc/nginx/nginx.conf.default /etc/nginx/conf.d/default.conf; else export RESOLVER=$(grep nameserver /etc/resolv.conf | awk '{print $2}' | head -1); envsubst '${ENV_HOST} ${RESOLVER}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
