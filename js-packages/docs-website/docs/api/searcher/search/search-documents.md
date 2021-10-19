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

Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard
dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.

### Request Body


