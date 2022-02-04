---
id: delete-data-documents-api
title: Delete Data Documents APIs
slug: /api/delete-data-documents-api

---

Send Entities to Entity Manager for handling and disambiguation

```bash
POST /v1/get-or-add-entities/

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
