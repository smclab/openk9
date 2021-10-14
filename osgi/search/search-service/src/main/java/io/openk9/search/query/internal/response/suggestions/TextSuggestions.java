package io.openk9.search.query.internal.response.suggestions;

class TextSuggestions extends Suggestions {

	TextSuggestions(String value, String keywordKey) {
		super(TokenType.TEXT, value);
		this.keywordKey = keywordKey;
	}

	public String getKeywordKey() {
		return keywordKey;
	}

	private final String keywordKey;

}
