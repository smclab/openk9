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

Allows you to perfom an understanding analysis on natural language query. In particular it returns a list of
search tokens based on different concepts and meanings founded in query.

### Request Body

`searchText`: (string) Textual content of the query

`tokens`: ([string]) List of search tokens confirmed by user, and postponed to possible subsequent calls,
in case new text is typed
