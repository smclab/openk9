# DEPRECATED

# OpenK9 JS REST API Interface

This package contains tpescript client for the OpenK9 REST API.

```typescript
import { OpenK9Client } from "@opekn9/rest-api";

const client = OpenK9Client("https://demo.openk9.io");

const results = await client.doSearch({
  searchQuery: [{ tokenType: "TEXT", values: ["hello world"], filter: false }],
  range: [0, 10],
});

```
