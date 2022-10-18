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
