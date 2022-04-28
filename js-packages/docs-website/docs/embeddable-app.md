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

This package takes care of complex interactions on your behalf.

## Usage

### With package manager

- install the dependency
  - with npm  `npm install @openk9/search-frontend`
  - or with yarn `yarn add @openk9/search-frontend`
- configure the ui
```javascript
import { OpenK9 } from "@openk9/search-frontend";

new OpenK9({
  enabled: true,
  search: "#search",
  tabs: "#tabs",
  results: "#results",
  details: "#details",
})
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
      new OpenK9({
        enabled: true,
        search: "#search",
        tabs: "#tabs",
        results: "#results",
        details: "#details",
      })
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

### Configuration

The configuration can be passed to the constructor, for example:

```javascript
const openk9 = new OpenK9({ enabled: true })
```

And can be modified at any time by calling the `updateConfiguration()` method that will overwrite existing fields with the provided ones, example:

```javascript
const openk9 = new OpenK9({ tenant: "https://demo.openk9.io" });
openk9.updateConfiguration({ enabled: true });
console.log(openk9.getConfiguration()); // { tenant: "https://demo.openk9.io", enabled: true }
```

You can configure these fields, all the fields are optional.

| Attribute           | Type    | Default | Description | Example |
|---------------------|---------|---------|-------------|---------|
| tenant              | `string`  | `""`    | The tenant url. An empty string means that the backend is located at the same domain as the frontend application | `"https://demo.openk9.io"` |
| enabled             | `boolean` | `false` | If false the search interface is not rendered | `true` |
| search              | `Element` or `string` | `null`  | Target element or query selector where the searchbar will be rendered | `#search-div` or `document.getElementById("search-div")` |
| tabs                | `Element` or `string` | `null`  | Target element where the result tabs will be rendered | `#tabs-div` or `document.getElementById("tabs-div")` |
| filters             | `Element` or `string` | `null`  | Target element where the search flters will be rendered | `#filters-div` or `document.getElementById("filters-div")` |
| results             | `Element` or `string` | `null`  | Target element where the search results will be rendered | `#results-div` or `document.getElementById("results-div")` |
| details             | `Element` or `string` | `null`  | Target element where the detailed view about the highlighted will be rendered | `#details-div` or `document.getElementById("details-div")` |
| login               | `Element` or `string` | `null` | Target element where the login button will be rendered | `#login-div` or `document.getElementById("login-div")` |
| searchAutoselect    | `boolean` | `true` | Whether or not to automatically select most meaningful semantic entity asociated to the search text | `true` |
| searchReplaceText   | `boolean` | `true` | Whether or not to automaticallly replaced typed text with that of the manually chosen semantic entity | `true` |
| defaultTokens        | `Array<SearchToken>` | `[]` | Search tokens that will be used in the search queries, they are **not displayed** anywhere | `[{ tokenType: "DATASOURCE", values: ["human-resources"], filter: false }]` |
| filterTokens        | `Array<SearchToken>` | `[]` | Search tokens that will be used in the search queries, displayed in the **filters** section | `[{ tokenType: "TEXT", values: ["hello"], filter: true }]` |

### Authentication

To make authenticated requests to the backend you need to provide a valid token to the `authenticate()` method.

If you rely on the default username/password authentication provided by openk9, you can obtain such a token by:

```javascript
const openk9 = new OpenK9();
const token = await openk9.client.getLoginInfoByUsernamePassword({ username: "admin", password: "admin" })
await openk9.authenticate(token);
```

Or you can retreive a valid token by other means:

```javascript
const openk9 = new OpenK9();
const token = await fetch("/get-openk9-token").then(res => res.json());
await openk9.authenticate(token);
```

You can also stop making authenticated requests by calling the `deauthenticate()` method.

```javascript
openk9.deauthenticate();
```

### Look & Feel

The look and feel can be customized including a CSS stylesheet.

Include this [CSS file](https://github.com/smclab/openk9/blob/main/js-packages/search-frontend/src/app.css) in your page for the default styling.

Copy, modify and his [CSS file](https://github.com/smclab/openk9/blob/main/js-packages/search-frontend/src/app.css) in your page for custom styling.

Beware, you MUST include a css file, because styling is not included in the javascript package.

### UI map

![ui map](/img/ui-map.png)

### Event listeners

```javascript
const openk9 = new OpenK9();

openk9.addEventListener("configurationChange", (configuration) => {
  console.log("the configuration changed to", configuration)
})

openk9.addEventListener("queryStateChange", (queryState) => {
  console.log("the user interacted, now searching results for", queryState)
})
```

### Internals

The user can search simply by typing text into the searchbar.

Semantic analysis is performed and results shown.

The user can additionally associate a semantic meaning to portions of the search text by positioning the cursor over a word and selecting an entity from a dropdown.

A semantic entity and some other information forms a search token.

The frontend sends to the backend the full text typed by the user and a list of search tokens. This list of search tokens if formed by the entities associated to portions of text, checkbox filters and the selected tab.