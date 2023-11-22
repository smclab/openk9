---
id: query-autocomplete
title: Query Autocomplete API
slug: /api/query-autocomplete

---

Get Query Autocompletion

```bash
POST /v1/query-autocomplete
{
	"searchQuery" : [
		{
			"tokenType": "AUTOCOMPLETE",
			"values": ["sear"]
		}
	]
}
```

### Description

Allows you to search all possible autocompletion in field set as autocomplete. This functionality can be used by search
frontend to get autocompletion to show to user as suggestions in query writing.

### Request Body

`searchQuery`: ([searchToken]) Array of search tokens of AUTOCOMPLETE type. A token of this type has tokenType set
as AUTOCOMPLETE and a string value use to get all possible autocompletion
