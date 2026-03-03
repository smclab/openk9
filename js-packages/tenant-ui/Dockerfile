FROM node:20-alpine as deps
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

WORKDIR /app

# Copy only package files for better caching
COPY ./js-packages/tenant-ui/package.json ./js-packages/tenant-ui/yarn.lock* ./js-packages/tenant-ui/
WORKDIR /app/js-packages/tenant-ui
RUN yarn config set registry https://registry.npmjs.org/ && \
    yarn install --network-timeout 600000

# Stage 2 - Build
FROM node:20-alpine as builder
ARG NODE_OPTIONS
ENV NODE_OPTIONS=${NODE_OPTIONS}

WORKDIR /app/js-packages/tenant-ui

# Copy dependencies from deps stage
COPY --from=deps /app/js-packages/tenant-ui/node_modules ./node_modules

# Copy source code
COPY ./js-packages/tenant-ui .

# Build
# CI=false prevents warnings (like outdated babel plugins) from being treated as errors
# DISABLE_NEW_JSX_TRANSFORM=true fixes compatibility issues with older craco/babel configurations

RUN CI=false NODE_ENV=production yarn build

# Stage 3 - Production
FROM nginx:1.28.2-alpine
COPY ./js-packages/tenant-ui/nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /usr/share/nginx/html/admin/
COPY --from=builder /app/js-packages/tenant-ui/build /usr/share/nginx/html/admin/
RUN rm /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh \
    && mkdir -p /var/run \
    && chmod g+rw /var/cache/nginx /var/run
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
