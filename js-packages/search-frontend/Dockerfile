# Stage 1 - build environment
FROM node:14-alpine as react-build

RUN mkdir -p /app/js-packages/search-frontend
WORKDIR /app

COPY ./js-packages/search-frontend/package*.json /app/js-packages/search-frontend
COPY ./yarn.lock /app
RUN yarn install --frozen-lockfile

COPY ./ /app
WORKDIR /app/js-packages/search-frontend
RUN yarn install
RUN yarn build

# Stage 2 - the production environment
FROM nginx:alpine
COPY ./js-packages/search-frontend/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-frontend/dist /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
