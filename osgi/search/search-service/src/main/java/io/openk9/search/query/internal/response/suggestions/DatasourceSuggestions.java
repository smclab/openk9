package io.openk9.search.query.internal.response.suggestions;

class DatasourceSuggestions extends Suggestions {
	DatasourceSuggestions(String value) {
		super(TokenType.DATASOURCE, value);
	}
}
