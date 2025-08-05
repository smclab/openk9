---
id: standalone-apps
title: Standalone Search and Generative Apps
sidebar_label: Standalone Search and Generative Apps
slug: /standalone-apps
---

Openk9 offers three different Standalone Apps to search or chat with your data:

- **Standalone Search App**: it's possible to perform multi-source, semantic, token-based searches, with a fast and efficient User Experience. The Standalone Search App is built with React.
- **Standalone Generative App**: it's possible to chat and explore data with a fast and efficient User Experience. The Standalone Search App is built with React.
- **Standalone Chatbot App**: it's possible to chat and explore data with a simple chatbot experience. The Standalone Search App is built with React.

You can look at the app's code in our [GitHub repository](https://github.com/smclab/openk9/tree/main/js-packages).

A working example for Search App can be found on [here](http://demo.openk9.cloud)

For working example for Generativa App you can go [here](http://demo.openk9.cloud/search)

## Docker

Prebuild docker images are available on [docker hub](https://hub.docker.com/r/smclab):

- [Generative Search App](https://hub.docker.com/layers/smclab/search-frontend/2.0.0/images/sha256-ef6ebbb2b236362efa1e60cc50958801e4c9b89dde30ea79ff0f72ef27fd01e5) 
- [Standalone Generative App](https://hub.docker.com/layers/smclab/openk9-talk-to/2.0.0/images/sha256-5668a2aa96b03f50630dd73981d86773a72664ee5c25bc8151c77ef0e1eef537) 

The images expose `8080` port to serve the app static files.

## From Sources

### Prerequisites

This software is needed for the build process

- [git](https://git-scm.com/)
- [nodejs](https://nodejs.org/it/)
- [yarn](https://yarnpkg.com/)
- [docker](https://www.docker.com/)

### Build Search App

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

- `openk9/js-packages/talk-to/dist` will contain the static resources that can be served with a static web server

This image exposes `8080` port to serve the app static files.

To run locally the full deployment refer to [Getting Started using Docker](using-docker)


### Build Generative App

The frontend can be build as standalone web application to be deployed

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
yarn # install dependencies
cd js-packages/talk-to
yarn build # build static resources
```

- `openk9/js-packages/talk-to/dist` will contain the static resources that can be served with a static web server

### Build & run docker image locally

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
docker build -t search-frontend:latest -f js-packages/search-frontend/Dockerfile .
docker run openk9/search-frontend
```

This image exposes `8080` port to serve the app static files.

To run locally the full deployment refer to [Getting Started using Docker](using-docker)