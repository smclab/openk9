---
id: trigger
title: Trigger API
slug: /api/create-tenant-tables

---

Send trigger for datasources

```bash
POST /v1/index/trigger
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
