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

package io.openk9.datasource.searcher.suggestions;

import io.openk9.searcher.grpc.Suggestions;
import io.openk9.searcher.grpc.TokenType;

public class SuggestionsUtil {

	public static Suggestions entity(
		String value, long suggestionCategoryId, String entityType, String entityValue) {

		return Suggestions
			.newBuilder()
			.setTokenType(TokenType.ENTITY)
			.setSuggestionCategoryId(suggestionCategoryId)
			.setValue(value)
			.setEntityType(entityType)
			.setEntityValue(entityValue)
			.build();
	}

	public static Suggestions entity(
		String value, long suggestionCategoryId, String entityType,
		String entityValue, String keywordKey) {

		return Suggestions
			.newBuilder()
			.setTokenType(TokenType.ENTITY)
			.setSuggestionCategoryId(suggestionCategoryId)
			.setValue(value)
			.setEntityType(entityType)
			.setEntityValue(entityValue)
			.setKeywordKey(keywordKey)
			.build();
	}

	public static Suggestions text(
		String value, long suggestionCategoryId, String keywordKey, long count) {
		return Suggestions
			.newBuilder()
			.setTokenType(TokenType.TEXT)
			.setSuggestionCategoryId(suggestionCategoryId)
			.setValue(value)
			.setKeywordKey(keywordKey)
			.setCount(count)
			.build();
	}

	public static Suggestions docType(
		String value, long suggestionCategoryId, long count) {
		return Suggestions
			.newBuilder()
			.setTokenType(TokenType.DOCTYPE)
			.setSuggestionCategoryId(suggestionCategoryId)
			.setValue(value)
			.setCount(count)
			.build();
	}

}
