# Stage 1 - build environment
FROM node:18.18-alpine as react-build

RUN mkdir -p /app/docs-website
WORKDIR /app

COPY . /app
RUN yarn install
RUN yarn build

# Stage 2 - the production environment
FROM nginx:1.28.2-alpine
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/build /usr/share/nginx/html/
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
