# Stage 1 - build environment
FROM node:18.18-alpine as react-build

RUN mkdir -p /app/docs-website
WORKDIR /app

COPY . /app
RUN yarn install
RUN yarn build

# Stage 2 - the production environment
FROM nginx:1.30.7-alpine3.24@sha256:caf4e1e032a20f9c3c5e28fc7f25ffd768ad8677b4975c54193075997b3a97e3
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/build /usr/share/nginx/html/
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
