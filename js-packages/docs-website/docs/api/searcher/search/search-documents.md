---
id: search-documents
title: Search Document API
slug: /api/search-documents

---

Search Document on Indexes

```bash
POST /search
{
	"searchQuery" : [
		{
			"entityType" : "type",
			"tokenType": "TEXT",
			"keywordKey": "",
			"values": ["name surname"]
		}
	],
	"range" : [0, 10]
}
```

### Description

Allows you to execute a search query and get back search results that match the query.
You can provide search queries using the following request body.

### Request Body

`searchQuery`: Array of search tokens.

`range`: Array of search tokens

### Search Token

