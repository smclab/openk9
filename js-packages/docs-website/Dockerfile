# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/js-packages/docs-website
WORKDIR /app

COPY ./package.json /app
COPY ./yarn.lock /app
COPY ./js-packages/docs-website/package.json /app/js-packages/docs-website
RUN yarn install --frozen-lockfile

COPY ./js-packages/docs-website /app/js-packages/docs-website
WORKDIR /app/js-packages/docs-website
RUN yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./js-packages/docs-website/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/docs-website/build /usr/share/nginx/html/
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
