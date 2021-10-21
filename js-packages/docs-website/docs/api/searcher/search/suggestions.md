---
id: suggestions
title: Search Document API
slug: /api/suggestions

---

Get suggestions

```bash
POST /v1/suggestions
{
	"searchQuery" : [
		{
			"entityType" : "",
			"tokenType": "DOCTYPE",
			"keywordKey": "",
			"values": ["web"]
		}
	],
	"range" : [0, 100]
}
```

### Description

Allows you to execute a search query to get back a list of suggestions about different informations: like:
- datasources
- documentTypes
- entities
- search keywords
- categories
- topics

You can provide search queries using the following request body.

### Request Body


