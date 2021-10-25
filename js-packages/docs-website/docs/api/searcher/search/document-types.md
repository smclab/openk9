---
id: document-types
title: Document Types API
slug: /api/document-types

---

Get document types

```bash
POST /v1/document-types
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

Allows you to execute a search query to get back the list of document types .

### Request Body
