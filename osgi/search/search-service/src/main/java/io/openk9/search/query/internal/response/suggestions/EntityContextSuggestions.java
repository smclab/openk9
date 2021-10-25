package io.openk9.search.query.internal.response.suggestions;

import java.util.Objects;

class EntityContextSuggestions extends EntitySuggestions {

	EntityContextSuggestions(
		String value, long suggestionCategoryId, String entityType,
		String entityValue, String keywordKey) {
		super(value, suggestionCategoryId, entityType, entityValue);
		this.keywordKey = keywordKey;
	}

	public String getKeywordKey() {
		return keywordKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		EntityContextSuggestions that = (EntityContextSuggestions) o;
		return keywordKey.equals(that.keywordKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), keywordKey);
	}

	private final String keywordKey;

}
