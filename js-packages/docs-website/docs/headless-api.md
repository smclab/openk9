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
import { SearchQuery, doSearch } from "@openk9/rest-api";

async function testSearch() {
  const searchQuery: SearchQuery = [
    {
      tokenType: "DOCTYPE",
      keywordKey: "type",
      values: ["document"],
    },
    {
      tokenType: "ENTITY",
      entityType: "person",
      values: ["vj4HsXcBabEqb3Lp0KJ8"],
    },
    {
      tokenType: "TEXT",
      values: ["Test Free text"],
    },
  ];

  const results = await doSearch({
    searchQuery,
    range: [0, 10],
  });

  console.log(results);
}
testSearch();
```
