---
id: embeddable-app
title: Embeddable Search App
sidebar_label: Embeddable Search App
slug: /embeddable-app
---

With the **Embeddable Search App** it's possible to perform multi-source, semantic, token-based searches, with a fast and efficient User Experience embedded to into your site. The Embeddable Search App is built with React.

See also [Standalone Search App](/standalone-app)

You can look at the frontend code in our [GitHub repository](https://github.com/smclab/openk9/tree/main/js-packages/search-frontend).

A working example can be found here [demo.openk9.io](http://demo.openk9.io)

This package takes care oc complex interactions on your behalf.

## Usage

### With package manager

- install the dependency
  - with npm  `npm install @openk9/search-frontend`
  - or with yarn `yarn add @openk9/search-frontend`
- configure the ui
```javascript
import { OpenK9 } from "@openk9/search-frontend";

Openk9.enabled = true;
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
      Openk9.enabled = true;
      Openk9.search = docuement.querySelector('#search');
      Openk9.tabs = document.querySelector('#tabs');
      Openk9.results = docuement.querySelector('#results');
      Openk9.details = docuement.querySelector('#details');
    </script>
  </body>
</html>
```

## From Sources

### Prerequisites

This software is neeeded for the build process

- [git](https://git-scm.com/)
- [nodejs](https://nodejs.org/it/)
- [yarn](https://yarnpkg.com/)

### Development

This setup enables development with live-reload on changes

```bash
git clone https://git.smc.it/openk9/openk9.git # clone repository
cd openk9
yarn # install dependencies
cd js-packages/search-frontend
yarn dev # start development
```

## Docs

The embeddable app expects HTML DOM elements where parts of the ui will be rendered.

A global variable, single instance is availabe in global scope `window.OpenK9` once the script is included.

### Configuration

This global variable has the following fields. That can be reassigned during runtime.

| Attribute           | Type    | Default | Description |
|---------------------|---------|---------|-------------|
| enabled             | boolean | `false` | If false the search interface is not rendered |
| search              | element | `null`  | Target element where the searchbar will be rendered |
| tabs                | element | `null`  | Target element where the result tabs will be rendered |
| filters             | element | `null`  | Target element where the search flters will be rendered |
| results             | element | `null`  | Target element where the search results will be rendered |
| details             | element | `null`  | Target element where the detailed view about the highlighted will be rendered |
| login               | element | `null` | Target element where the login button will be rendered |
| searchAutoselect    | boolean | `true` | Whether or not to automatically select most meaningful semantic entity asociated to the search text |
| searchReplaceText   | boolean | `true` | Whether or not to automaticallly replaced typed text with that of the manually chosen semantic entity |

### Look & Feel

The look and feel can be customized including a CSS stylesheet.

Include this [CSS file](https://github.com/smclab/openk9/blob/main/js-packages/search-frontend/src/app.css) in your page for the default styling.

Copy, modify and his [CSS file](https://github.com/smclab/openk9/blob/main/js-packages/search-frontend/src/app.css) in your page for custom styling.

### UI map

coming soon

### Internals

The user can search simply by typing text into the searchbar.

Semantic analysis is performed and results shown.

The user can additionally associate a semantic meaning to portions of the search text by positioning the cursor over a word and selecting an entity from a dropdown.

A semantic entity and some other information forms a search token.

The frontend sends to the backend the full text typed by the user and a list of search tokens. This list of search tokens if formed by the entities associated to portions of text, checkbox filters and the selected tab.