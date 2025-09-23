# Stage 1 - build environment
FROM node:20-alpine AS react-build
ARG BUILD_ENV=keycloak

RUN mkdir -p /app/js-packages/search-frontend

# Copy the package.json from the root
COPY ./package.json /app/package.json
COPY ./yarn.lock /app
# COPY ./.yarn/ /app/.yarn/
# COPY ./.yarnrc.yml /app/

# Copy the package.json.conf file from the search-frontend package (package.json with MUI library)
COPY ./js-packages/search-frontend/package.json.conf /app/js-packages/search-frontend/package.json

# Set the project root
WORKDIR /app
#Yarn v4 disabled in Docker
# # Enable Corepack (required to manage modern Yarn versions)
# RUN corepack enable

# # Set the Yarn version specified in package.json
# RUN corepack prepare yarn@4.10.0 --activate

# Install all workspace dependencies - For Yarn v4: RUN yarn install --immutable --immutable-cache
RUN yarn install --frozen-lockfile


COPY ./js-packages/search-frontend /app/js-packages/search-frontend
WORKDIR /app/js-packages/search-frontend

# Conditional building - BUILD_ENV
RUN if [ "$BUILD_ENV" = "keycloak" ]; then \
      echo "Building for Keycloak environment..." && \
      NODE_ENV=production yarn build:keycloak; \
    elif [ "$BUILD_ENV" = "nokeycloak" ]; then \
      echo "Building for No-Keycloak environment..." && \
      NODE_ENV=production yarn build:nokeycloak; \
    elif [ "$BUILD_ENV" = "chatbot" ]; then \
      echo "Building for Chatbot environment..." && \
      NODE_ENV=production yarn build:chatbot; \
    else \
      echo "Unknown BUILD_ENV: $BUILD_ENV, defaulting to keycloak..." && \
      NODE_ENV=production yarn build:keycloak; \
    fi

# Stage 2 - the production environment
FROM nginx:1.25.3
COPY ./js-packages/search-frontend/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-frontend/dist /usr/share/nginx/html/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]