---
id: query-analysis
title: Query Analysis API
slug: /api/query-analysis

---

Get Query Analysis

```bash
POST /v1/query-analysis
{
	"searchText": "email of daniele caldarini",
	"tokens": [
	  {
	    score: 5.256,
	    keywordKey: "web.title",
	    tokenType: "TEXT",
	    value: "openk9"
	  }
	]
}
```

### Description

Allows you to perfom an understanding analysis on natural language query. In particular it returns a list of
search tokens based on different concepts and meanings founded in query.

### Request Body

`searchText`: (string) Textual content of the query

`tokens`: ([string]) List of search tokens confirmed by user, and postponed to possible subsequent calls,
in case new text is typed
