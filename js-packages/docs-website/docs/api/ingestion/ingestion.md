---
id: ingestion
title: Ingestion API
slug: /api/ingestion

---

Send message to Openk9

```bash
POST /v1/ingestion/
{
	"datasourceId": 1,
	"contentId": 1,
	"rawContent": "test test",
	"datasourcePayload": {
		"document": {}
	},
	"parsingDate": 0,
	"resources": {
     "binaries" : [
        {}
      ]
  }
}
```

### Description

This endpoint allows you to send a content from an external source to Openk9.

### Request Body

`datasourceId`: (integer) Id of datasource

`contentId`: (integer) Unique id of content

`rawContent`: (string) Raw text of content

`parsingDate`: (string) Date the parsing was performed

`datasourcePayload`: (string) Payload with structured data of content

`resources`: (string) Contains resources associated with the content, i.e binaries of files
