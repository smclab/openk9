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

package io.openk9.datasource.model.init;

import io.openk9.datasource.model.dto.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.SearchConfigDTO;

import java.util.List;

public class SearchConfig {

	public static final List<QueryParserConfigDTO> QUERY_PARSER_CONFIGS =
		QueryParserConfigs.INSTANCE;
	private static final String NAME = "Default Search Config";
	private static final String DESCRIPTION =
		"Default Search Config Configuration for OpenK9";
	private static final float MIN_SCORE = 0F;
	private static final boolean MIN_SCORE_SUGGESTION = false;
	private static final boolean MIN_SCORE_SEARCH = false;
	public static final SearchConfigDTO INSTANCE = SearchConfigDTO.builder()
		.name(NAME)
		.description(DESCRIPTION)
		.minScore(MIN_SCORE)
		.minScoreSuggestions(MIN_SCORE_SUGGESTION)
		.minScoreSearch(MIN_SCORE_SEARCH)
		.build();

	private SearchConfig() {}

}
