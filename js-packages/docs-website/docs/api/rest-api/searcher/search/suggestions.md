---
id: suggestions
title: Suggestions API
slug: /api/suggestions

---

Get suggestions

```bash
POST /v1/suggestions
{
	"searchQuery" : [
		{
			"entityType" : "",
			"tokenType": "DOCTYPE",
			"keywordKey": "",
			"values": ["web"]
		}
	],
  "suggestKeyword": "web"
  "suggestionCategoryId": 1
  "range" : [0, 10]
}
```

### Description

Allows you to execute a search query to get back a list of suggestions about different information based on suggestion
categories defined in database. Example of concepts, for which to receive suggestions, are:

- datasources
- documentTypes
- entities
- categories
- topics
- etc.

Suggestions endpoint works similar as search, in the sense that it performs a search based on searchQuery passed in
request body, and on it filter information based on suggestion categories configured.

You can provide search queries using the following request body.

### Request Body

`searchQuery`: ([searchToken]) Array of search tokens.

`suggestKeyword`: (string) Text to rank sugggestions

`suggestionCategoryId`: (integer) Id of suggestion category

`range`: ([integer]) range to paginate results



