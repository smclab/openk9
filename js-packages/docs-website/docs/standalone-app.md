---
id: standalone-app
title: Standalone Search App
sidebar_label: Standalone Search App
slug: /standalone-app
---

With the **Standalone Search App** it's possible to perform multi-source, semantic, token-based searches, with a fast and efficient User Experience. The Standalone Search App is built with React.

The Standalone Search App has its own [docker image](https://hub.docker.com/r/smclab/openk9-search-frontend) ready to be deployed with a Web Server to serve the app static files.

You can look at the frontend code in our [GitHub repository](https://github.com/smclab/openk9/tree/main/js-packages/search-frontend).

A working example can be found here [demo.openk9.io](http://demo.openk9.io)

## Docker

Prebuild docker image is available on [docker hub](https://hub.docker.com/r/smclab/openk9-search-frontend)

This image exposes `8080` port to serve the app static files.

This image expects that te api is available on the same domain under the route `/api`

## From Sources

### Prerequisites

This software is needed for the build process

- [git](https://git-scm.com/)
- [nodejs](https://nodejs.org/it/)
- [yarn](https://yarnpkg.com/)
- [docker](https://www.docker.com/)

### Build

The frontend can be build as standalone web application to be deployed

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
yarn # install dependencies
cd js-packages/search-frontend
yarn build # build static resources
```

- `openk9/js-packages/search-frontend/dist` will contain the static resources that can be served with a static web server (NOTE: the backend must be located ad the same domain under the route `/api`)

### Build & run docker image locally

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
docker build -t search-frontend:latest -f js-packages/search-frontend/Dockerfile .
docker run openk9/search-frontend
```

This image exposes `8080` port to serve the app static files.

This image assumes that the backend il located on the same domain under the route `/api`.

To run locally the full deployment refer to [Getting Started using Docker](using-docker)