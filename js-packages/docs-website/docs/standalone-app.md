---
id: standalone-app
title: Standalone Search App
sidebar_label: Standalone Search App
slug: /standalone-app
---

With the **Standalone Search App** it's possible to perform multi-source, semantic, token-based searches, with a fast and efficient User Experience. The Standalone Search App is built with React.

The Standalone Search App has its own [docker image](https://hub.docker.com/r/smclab/openk9-search-frontend) ready to be deployed with a Web Server to serve the app static files.

You can look at the frontend code in our [GitHub repository](https://github.com/smclab/openk9/tree/main/js-packages/search-standalone-frontend).

## Usage

### With package manager

- install the dependency
  - with npm  `npm install @openk9/search-frontend`
  - or with yarn `yarn add @openk9/search-frontend`
- configure the ui
```javascript
import { OpenK9 } from "@openk9/search-frontend";

Openk9.search = docuement.querySelector('#search');
Openk9.tabs = document.querySelector('#tabs');
Openk9.results = docuement.querySelector('#results');
Openk9.details = docuement.querySelector('#details');
```

### Without package manager

- Download the script from https://unpkg.com/@openk9/search-frontend@latest
- Or build the script from sources
  - `openk9/js-packages/search-frontend/dist` will contain a file named `embeddable.js` that must be included on your site

```html
<html>
  <body>
    <div id="search"></div>
    <div id="tabs"></div>
    <div id="results"></div>
    <div id="details"></div>
    <!-- this is the file from dist directory-->
    <script src="embeddable.js"></script>
    <!-- tell the script where to render parts of the interface -->
    <script>
      Openk9.search = docuement.querySelector('#search');
      Openk9.tabs = document.querySelector('#tabs');
      Openk9.results = docuement.querySelector('#results');
      Openk9.details = docuement.querySelector('#details');
    </script>
  </body>
</html>
```

### Docker

Prebuild docker image is available on [docker hub](https://hub.docker.com/r/smclab/openk9-search-frontend) 

## From Sources

### Prerequisites

This software is neeeded for the build process

- [git](https://git-scm.com/)
- [nodejs](https://nodejs.org/it/)
- [yarn](https://yarnpkg.com/)
- [docker](https://www.docker.com/)

### Development

This setup enables development with live-reload on changes

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
yarn # install dependencies
cd js-packages/search-frontend
yarn dev # start development
```

### Build

The frontend can be build as standalone web application to be deployed

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
yarn # install dependencies
cd js-packages/search-frontend
yarn build # build static resources
```

- `openk9/js-packages/search-frontend/dist` will contain the static resources that can be served with a static web server (NOTE: the backend must be located ad the same domain)

### Build & run docker image locally

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
docker build -t search-frontend:latest -f js-packages/search-frontend/Dockerfile .
docker run openk9/search-frontend
```

This image assumes that the backlend il located on the same domain.

To run locally the full deployment refer to [Getting Started using Docker](using-docker)