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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
	@Schema(description = "List of ParserSearchToken to compose search query.")
	private List<ParserSearchToken> searchQuery;
	@Schema(description = "List of integer for pagination where first element is start element and second element is page size")
	private List<Integer> range;
	@Schema(description = "After key used for pagination in suggestions endpoint")
	private String afterKey;
	@Schema(description = "Keyword used to filter options in suggestions endpoint")
	private String suggestKeyword;
	@Schema(format = "uuid",
			description = "Unique string that identifies the Suggestion Category to return suggestions")
	private Long suggestionCategoryId;
	@Schema(description = "How to order suggestions")
	private String order = "asc";
	@Schema(description = "List of objects to define sort logic")
	private List<Map<String, Map<String, String>>> sort;
	@Schema(description = "After key used for pagination when sort is present")
	private String sortAfterKey;
	@Schema(description = "Language used to apply search in specific language.")
	private String language;
}