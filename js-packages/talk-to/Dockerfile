FROM node:20-alpine as deps
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

WORKDIR /app

# Copy only package files for better caching
COPY ./js-packages/talk-to/package.json ./js-packages/talk-to/yarn.lock* ./js-packages/talk-to/
WORKDIR /app/js-packages/talk-to
RUN yarn config set registry https://registry.npmjs.org/ && \
    yarn install --network-timeout 600000

# Stage 2 - Build
FROM node:20-alpine as builder
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

WORKDIR /app/js-packages/talk-to

# Copy dependencies from deps stage
COPY --from=deps /app/js-packages/talk-to/node_modules ./node_modules

# Copy source code
COPY ./js-packages/talk-to .

# Build
# CI=false prevents warnings (like outdated babel plugins) from being treated as errors
# DISABLE_NEW_JSX_TRANSFORM=true fixes compatibility issues with older craco/babel configurations

RUN CI=false NODE_ENV=production yarn build

# Stage 3 - Production
FROM nginx:stable-alpine3.23-slim
RUN mkdir -p /usr/share/nginx/html/chat/
COPY --from=builder /app/js-packages/talk-to/build /usr/share/nginx/html/chat/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && rm -f /etc/nginx/conf.d/default.conf \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run /etc/nginx/conf.d
COPY ./js-packages/talk-to/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
USER 1001
CMD ["nginx", "-g", "daemon off;"]
