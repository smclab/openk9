/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.searcher.suggestions;

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

	public long getSuggestionCategoryId() {
		return suggestionCategoryId;
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

}
