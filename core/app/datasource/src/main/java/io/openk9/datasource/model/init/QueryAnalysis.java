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

import io.openk9.datasource.model.dto.AnnotatorDTO;
import io.openk9.datasource.model.dto.QueryAnalysisDTO;
import io.openk9.datasource.model.dto.RuleDTO;

import java.util.List;

public class QueryAnalysis {

	public static final List<AnnotatorDTO> ANNOTATORS = Annotators.INSTANCE;
	public static final List<RuleDTO> RULES = Rules.INSTANCE;
	private static final String NAME = "Default Query Analysis";
	private static final String DESCRIPTION = "Default Query Analysis Configuration";
	private static final String STOP_WORDS = "";
	public static final QueryAnalysisDTO INSTANCE = QueryAnalysisDTO.builder()
		.name(NAME)
		.description(DESCRIPTION)
		.stopWords(STOP_WORDS)
		.build();

	private QueryAnalysis() {}

}
