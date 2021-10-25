package io.openk9.search.query.internal.response.suggestions;

class DocTypeSuggestions extends Suggestions {
	DocTypeSuggestions(String value, long suggestionCategoryId) {
		super(TokenType.DOCTYPE, value, suggestionCategoryId);
	}
}
