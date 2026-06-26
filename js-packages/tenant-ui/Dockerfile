FROM node:22-alpine as deps
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

WORKDIR /app

# Copy only package files for better caching
COPY ./js-packages/tenant-ui/package.json ./js-packages/tenant-ui/yarn.lock* ./js-packages/tenant-ui/
WORKDIR /app/js-packages/tenant-ui
RUN yarn config set registry https://registry.npmjs.org/ && \
    yarn install --network-timeout 600000

# Stage 2 - Build
FROM node:22-alpine as builder
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

WORKDIR /app/js-packages/tenant-ui

# Copy dependencies from deps stage
COPY --from=deps /app/js-packages/tenant-ui/node_modules ./node_modules

# Copy source code
COPY ./js-packages/tenant-ui .

# Dev mode exposes development-only options (e.g. NO_GATEWAY_AUTH) in the UI.
# Off by default; k9.sh enables it for dev image tags.
ARG DEV_MODE=false
ENV REACT_APP_DEV_MODE=$DEV_MODE

# Build
# CI=false prevents warnings (like outdated babel plugins) from being treated as errors
# DISABLE_NEW_JSX_TRANSFORM=true fixes compatibility issues with older craco/babel configurations

RUN CI=false GENERATE_SOURCEMAP=false NODE_OPTIONS="--max-old-space-size=4096" yarn build

# Stage 3 - Production
FROM nginx:stable-alpine3.23-slim
RUN mkdir -p /usr/share/nginx/html/admin/
COPY --from=builder /app/js-packages/tenant-ui/build /usr/share/nginx/html/admin/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && sed -i 's|/run/nginx.pid|/tmp/nginx.pid|' /etc/nginx/nginx.conf \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run /etc/nginx/conf.d
COPY ./js-packages/tenant-ui/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
USER 1001
# The slim nginx base declares ENTRYPOINT ["/docker-entrypoint.sh"] but does
# not ship that script, so the inherited entrypoint fails at container start.
# Reset it and run nginx directly via CMD (the static nginx.conf needs none of
# the /docker-entrypoint.d template processing).
ENTRYPOINT []
CMD ["nginx", "-g", "daemon off;"]
