---
id: search-documents
title: Search Document API
slug: /api/search-documents

---

Search Document on Indexes

```bash
POST /search
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

Allows you to execute a search query and get back search results that match the query.
You can provide search queries using the following request body.

### Request Body

`searchQuery`: ([searchToken]) Array of search tokens.

`range`: ([integer]) Range to paginate results

### Search Token

Every search token represents a specific part of the search query. In particular every token is then
translated in a specific query in Elasticsearch.

Different types of Search Token are supported:

- **TEXT**: Match values in all fields of the data or eventually, if keywordKey is specified, in a specific key of the data
- **DOCTYPE**: Filter data with DocumentType that matches values
- **DATASOURCE**: Filter data with Datasource that matches values
- **ENTITY**: Filter data which contain entities that match entity values

Every search token has:

- **entityType**: type of entity (present only if tokenType is ENTITY)
- **tokenType**: type of searchToken
- **keywordKey**: keyword key of data where search for matches (present only if tokenType is TEXT)
- **values**: array of text values to match


See more on searcher components on [Architecture Documentation](/docs/searcher)

