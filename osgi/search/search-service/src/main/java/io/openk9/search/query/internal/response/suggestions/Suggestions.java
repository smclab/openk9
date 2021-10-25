package io.openk9.search.query.internal.response.suggestions;

import java.util.Objects;

public abstract class Suggestions {

	Suggestions(TokenType tokenType, String value, long suggestionCategoryId) {
		this.tokenType = tokenType;
		this.value = value;
		this.suggestionCategoryId = suggestionCategoryId;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Suggestions)) {
			return false;
		}
		Suggestions that = (Suggestions) o;
		return tokenType == that.tokenType && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokenType, value);
	}

	private final TokenType tokenType;
	private final String value;
	private final long suggestionCategoryId;

	public static Suggestions entity(
		String value, long suggestionCategoryId, String entityType, String entityValue) {

		return new EntitySuggestions(
			value, suggestionCategoryId, entityType, entityValue);
	}

	public static Suggestions entity(
		String value, long suggestionCategoryId, String entityType,
		String entityValue, String keywordKey) {

		return new EntityContextSuggestions(
			value, suggestionCategoryId, entityType, entityValue, keywordKey);
	}

	public static Suggestions text(
		String value, long suggestionCategoryId, String keywordKey) {
		return new TextSuggestions(value, suggestionCategoryId, keywordKey);
	}

	public static Suggestions docType(String value, long suggestionCategoryId) {
		return new DocTypeSuggestions(value, suggestionCategoryId);
	}

	public static Suggestions datasource(String value, long suggestionCategoryId) {
		return new DatasourceSuggestions(value, suggestionCategoryId);
	}

}
