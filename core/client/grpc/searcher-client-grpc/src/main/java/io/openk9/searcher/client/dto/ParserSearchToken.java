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

package io.openk9.searcher.client.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParserSearchToken {
	@Schema(description = "Used for specify entityType to search in case of tokenType ENTITY")
	private String entityType;
	@Schema(description = "Used for specify entityName to search in case of tokenType ENTITY")
	private String entityName;
	@Schema(description = "Used to identify the ParserSearchToken that contains the text entered in the search input")
	private boolean isSearch;
	@Schema(description = "Token Type to specify type of ParserSearchToken")
	private String tokenType;
	@Schema(description = "Used to specify specific keyword field to perform search")
	private String keywordKey;
	@Schema(description = "List of strings used to perform search. In case of multiple strings, search logic (MUST/SHOULD/...) depends on Openk9 search config")
	private List<String> values;
	@Schema(description = "Used to specify extra configurations to overwrite default configurations")
	private Map<String, String> extra;
	private boolean filter;

	public static ParserSearchToken ofText(String value) {
		return ofText(List.of(value), null, false);
	}

	public static ParserSearchToken ofText(List<String> values) {
		return ofText(values, null, false);
	}

	public static ParserSearchToken ofText(List<String> values, String keywordKey) {
		return ofText(values, keywordKey, false);
	}

	public static ParserSearchToken ofText(
		List<String> values, String keywordKey, boolean filter) {

		return new ParserSearchToken(
			null, null, false, TEXT, keywordKey, values, Map.of(), filter);
	}

	public static final String TEXT = "TEXT";

}
