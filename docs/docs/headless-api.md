---
id: headless-api
title: Headless API
sidebar_label: Headless API
slug: /headless-api
---

With the **Headless API** it's possible to perform searches inside enriched documents and entities with a JSON and HTTP interface, allowing you to build any kind of application, ranging from Mobile Apps to Speech Interfaces and even Data Visualisation and Analytics.

:::info
More info about the Headless API is coming soon.
:::

## TypeScript Interface library

If you are using our Headless API in a browser or Node.JS environment you may want to use our TypeScript wrapper, available as [NPM package](https://www.npmjs.com/package/@openk9/rest-api).

```bash
npm install --save @openk9/rest-api
```

### Example using the TypeScript library

```ts
import { OpenK9Client } from "@openk9/rest-api";

const client = OpenK9Client({ tenant: "https://demo.openk9.io" });

client
  .doSearch({
    searchQuery: [
      {
        tokenType: "DOCTYPE",
        keywordKey: "type",
        values: ["document"],
        filter: true,
      },
      { tokenType: "TEXT", values: ["free text search"], filter: false },
    ],
    range: [0, 10],
  })
  .then(console.log);
```

### Docs

Coming soon...

In the meantime you can explore the api by harnessing typescript intellisense and JSDOC using an ide that supports it (ex: [vscode](https://code.visualstudio.com/))