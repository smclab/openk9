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

`datasourceId`: (int) Id of datasource

`contentId`: (int) Unique id of content

`rawContent`: (string) Raw text of content

`parsingDate`: (date) Date the parsing was performed

`datasourcePayload`: (dict) Payload with structured data of content

`resources`: (dict) Contains resources associated with the content, i.e binaries of files
