# Stage 1 - build environment
FROM node:18-alpine as react-build

RUN mkdir -p /app/js-packages/search-frontend

COPY ./package.json /app
COPY ./yarn.lock /app
COPY ./js-packages/search-frontend/package.json /app/js-packages/search-frontend/

WORKDIR /app
RUN yarn install --frozen-lockfile

COPY ./js-packages/search-frontend /app/js-packages/search-frontend
WORKDIR /app/js-packages/search-frontend
RUN NODE_ENV=production yarn build

# Stage 2 - the production environment
FROM nginx:stable-alpine3.23-slim

# Usiamo path custom per evitare che nginx processi automaticamente il template
COPY ./js-packages/search-frontend/nginx.conf.template /etc/nginx/nginx.conf.template
COPY ./js-packages/search-frontend/nginx.conf.default /etc/nginx/nginx.conf.default

RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-frontend/dist /usr/share/nginx/html/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run /etc/nginx/conf.d
EXPOSE 8080
USER 1001

# Se ENV_HOST è fornito, compila il template col proxy. Altrimenti usa il config standard.
# Usiamo envsubst '${ENV_HOST}' per evitare che sovrascriva le variabili interne di nginx come $host
CMD ["sh", "-c", "if [ -z \"$ENV_HOST\" ]; then cp /etc/nginx/nginx.conf.default /etc/nginx/conf.d/default.conf; else envsubst '${ENV_HOST}' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
