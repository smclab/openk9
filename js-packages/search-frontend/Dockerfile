# Stage 1 - build environment
FROM node:22-alpine AS react-build
ARG BUILD_ENV=oauth2
ARG CHATBOT_ENABLED
ARG GENERATIVE_ENABLED
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

RUN mkdir -p /app/js-packages/search-frontend

# Copy the package.json from the root
COPY ./package.json /app/package.json
COPY ./yarn.lock* /app
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
RUN yarn config set registry https://registry.npmjs.org/ && \
  yarn install --frozen-lockfile --network-timeout 600000


COPY ./js-packages/search-frontend /app/js-packages/search-frontend
WORKDIR /app/js-packages/search-frontend

# Conditional building - BUILD_ENV
# CI=false prevents warnings (like outdated babel plugins) from being treated as errors
# DISABLE_NEW_JSX_TRANSFORM=true fixes compatibility issues with older craco/babel configurations

RUN if [ -n "$CHATBOT_ENABLED" ] || [ -n "$GENERATIVE_ENABLED" ]; then \
  echo "Building with explicit flags: chatbot=${CHATBOT_ENABLED:-false}, generative=${GENERATIVE_ENABLED:-false}..." && \
  CI=false NODE_ENV=production \
  CHATBOT_ENABLED="${CHATBOT_ENABLED:-false}" GENERATIVE_ENABLED="${GENERATIVE_ENABLED:-false}" \
  yarn build:flags; \
  elif [ "$BUILD_ENV" = "oauth2" ]; then \
  echo "Building for OAuth2 environment..." && \
  CI=false NODE_ENV=production yarn build:oauth2; \
  elif [ "$BUILD_ENV" = "chatbot" ]; then \
  echo "Building for Chatbot environment..." && \
  CI=false NODE_ENV=production yarn build:chatbot; \
  else \
  echo "Unknown BUILD_ENV: $BUILD_ENV, defaulting to oauth2..." && \
  CI=false NODE_ENV=production yarn build:oauth2; \
  fi

# Stage 2 - the production environment
FROM nginx:stable-alpine3.23-slim
RUN mkdir -p /usr/share/nginx/html/
COPY --from=react-build /app/js-packages/search-frontend/dist /usr/share/nginx/html/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && sed -i 's|/run/nginx.pid|/tmp/nginx.pid|' /etc/nginx/nginx.conf \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run /etc/nginx/conf.d
COPY ./js-packages/search-frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
USER 1001
# The slim nginx base declares ENTRYPOINT ["/docker-entrypoint.sh"] but does
# not ship that script, so the inherited entrypoint fails at container start.
# Reset it and run nginx directly via CMD (the static nginx.conf needs none of
# the /docker-entrypoint.d template processing).
ENTRYPOINT []
CMD ["nginx", "-g", "daemon off;"]