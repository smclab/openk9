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

import io.openk9.datasource.model.dto.BucketDTO;
import io.openk9.datasource.model.dto.LanguageDTO;
import io.openk9.datasource.model.dto.QueryAnalysisDTO;
import io.openk9.datasource.model.dto.SearchConfigDTO;

import java.util.List;

public class Bucket {
	public static final QueryAnalysisDTO QUERY_ANALYSIS = QueryAnalysis.INSTANCE;
	public static final SearchConfigDTO SEARCH_CONFIG = SearchConfig.INSTANCE;
	public static final List<LanguageDTO> LANGUAGES = List.of(
		Languages.ITALIAN,
		Languages.ENGLISH,
		Languages.FRENCH,
		Languages.GERMAN,
		Languages.SPANISH
	);
	private static final String NAME = "Default Bucket";
	private static final String DESCRIPTION = "";
	private static final boolean REFRESH_ON_SUGGESTION_CATEGORY = false;
	private static final boolean REFRESH_ON_TAB = false;
	private static final boolean REFRESH_ON_DATE = false;
	private static final boolean REFRESH_ON_QUERY = false;

	public static final BucketDTO INSTANCE = BucketDTO.builder()
		.name(NAME)
		.description(DESCRIPTION)
		.refreshOnDate(REFRESH_ON_DATE)
		.refreshOnQuery(REFRESH_ON_QUERY)
		.refreshOnTab(REFRESH_ON_TAB)
		.refreshOnSuggestionCategory(REFRESH_ON_SUGGESTION_CATEGORY)
		.build();

}
