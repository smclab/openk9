package io.openk9.search.query.internal.response.suggestions;

class DocTypeSuggestions extends Suggestions {
	DocTypeSuggestions(String value) {
		super(TokenType.DOCTYPE, value);
	}
}
