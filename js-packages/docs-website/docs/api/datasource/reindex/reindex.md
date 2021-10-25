---
id: reindex
title: Reindex API
slug: /api/reindex

---

Send reindex for datasources

```bash
POST /v1/index/reindex/
{
	"datasourceIds" : [
		1,2,3,4
	]
}
```

### Description

This endpoint allows you to send a content from an external source to Openk9.

### Request Body

`datasourceIds`: (list) List of datasourceIds to reindex
