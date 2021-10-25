package io.openk9.search.query.internal.response.suggestions;

class DatasourceSuggestions extends Suggestions {
	DatasourceSuggestions(String value, long suggestionCategoryId) {
		super(TokenType.DATASOURCE, value, suggestionCategoryId);
	}
}
