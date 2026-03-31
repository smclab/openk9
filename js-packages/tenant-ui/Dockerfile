FROM node:18-alpine as react-build

COPY ./js-packages/tenant-ui /app
COPY ./yarn.lock /app

WORKDIR /app

RUN yarn install
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:1.28.2-alpine

# Usiamo path custom per evitare che nginx processi automaticamente il template
COPY ./js-packages/tenant-ui/nginx.conf.template /etc/nginx/nginx.conf.template
COPY ./js-packages/tenant-ui/nginx.conf.default /etc/nginx/nginx.conf.default

RUN mkdir -p /usr/share/nginx/html/tenant/
COPY --from=react-build /app/build /usr/share/nginx/html/tenant/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080

# Se ENV_HOST è fornito, compila il template col proxy. Altrimenti usa il config standard.
# Usiamo envsubst '${ENV_HOST}' per evitare che sovrascriva le variabili interne di nginx come $host
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then cp /etc/nginx/nginx.conf.default /etc/nginx/conf.d/default.conf; else envsubst '${ENV_HOST}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
