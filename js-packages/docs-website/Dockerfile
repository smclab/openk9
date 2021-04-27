# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/patches
RUN mkdir -p /app/js-packages/docs-website
WORKDIR /app

# COPY ./patches/*.patch /app/patches/
COPY ./package*.json /app
COPY ./js-packages/docs-website/package*.json /app/js-packages/docs-website
COPY ./yarn.lock /app
RUN yarn install --frozen-lockfile

COPY ./ /app
WORKDIR /app/js-packages/docs-website
RUN yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./js-packages/docs-website/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/docs-website/build /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
