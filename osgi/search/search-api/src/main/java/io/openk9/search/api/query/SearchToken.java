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

package io.openk9.search.api.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchToken {
	private String entityType;
	private String entityName;
	private String tokenType;
	private String keywordKey;
	private String[] values;
	private Map<String, Object> extra;
	private Boolean filter;

	public static SearchToken ofText(String value) {
		return ofText(new String[] {value}, null, false);
	}

	public static SearchToken ofText(String[] values) {
		return ofText(values, null, false);
	}

	public static SearchToken ofText(String[] values, String keywordKey) {
		return ofText(values, keywordKey, false);
	}

	public static SearchToken ofText(
		String[] values, String keywordKey, boolean filter) {

		return new SearchToken(
			null, null, "TEXT", keywordKey, values, Map.of(), filter);
	}

}
