---
id: create-data-index-from-doc-types
title: create-data-index-from-doc-types API
slug: /api/create-data-index-from-doc-types

---

Send reindex for datasources

```bash
POST /v1/index/reindex
{
	"datasourceIds" : [
		1,2,3,4
	]
}
```

### Description

This endpoint allows you to send a content from an external source to Openk9.

### Request Body

`datasourceIds`: ([integer]) List of datasourceIds to reindex
