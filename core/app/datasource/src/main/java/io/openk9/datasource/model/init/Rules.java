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

import io.openk9.datasource.model.dto.RuleDTO;

import java.util.Set;

public class Rules {

	private static final RuleDTO $ROOT_$Query = RuleDTO.builder()
		.name("$ROOT_$Query")
		.lhs("$ROOT")
		.rhs("$Query")
		.build();
	private static final RuleDTO $Query_$Collection = RuleDTO.builder()
		.name("$Query_$Collection")
		.lhs("$Query")
		.rhs("$Collection")
		.build();
	private static final RuleDTO $Part_$Intent = RuleDTO.builder()
		.name("$Part_$Intent")
		.lhs("$Part")
		.rhs("$Intent")
		.build();
	private static final RuleDTO $Part_$NotIntent = RuleDTO.builder()
		.name("$Part_$NotIntent")
		.lhs("$Part")
		.rhs("$NotIntent")
		.build();
	private static final RuleDTO $Intent_$Content = RuleDTO.builder()
		.name("$Intent_$Content")
		.lhs("$Intent")
		.rhs("$Content")
		.build();
	private static final RuleDTO $NotIntent_$TOKEN = RuleDTO.builder()
		.name("$NotIntent_$TOKEN")
		.lhs("$NotIntent")
		.rhs("$TOKEN")
		.build();
	private static final RuleDTO $Content_$QUOTE_TOKEN = RuleDTO.builder()
		.name("$Content_$QUOTE_TOKEN")
		.lhs("$Content")
		.rhs("$QUOTE_TOKEN")
		.build();
	private static final RuleDTO $NotIntent_$StopWord = RuleDTO.builder()
		.name("$NotIntent_$StopWord")
		.lhs("$NotIntent")
		.rhs("$StopWord")
		.build();
	private static final RuleDTO $Content_$AUTOCOMPLETE = RuleDTO.builder()
		.name("$Content_$AUTOCOMPLETE")
		.lhs("$Content")
		.rhs("$AUTOCOMPLETE")
		.build();
	private static final RuleDTO $Content_$AGGREGATE = RuleDTO.builder()
		.name("$Content_$AGGREGATE")
		.lhs("$Content")
		.rhs("$AGGREGATE")
		.build();
	private static final RuleDTO $Content_$KEYWORD_AUTOCOMPLETE = RuleDTO.builder()
		.name("$Content_$KEYWORD_AUTOCOMPLETE")
		.lhs("$Content")
		.rhs("$KEYWORD_AUTOCOMPLETE")
		.build();
	private static final RuleDTO $Collection_$Part_$Collection = RuleDTO.builder()
		.name("$Collection_$Part ?$Collection")
		.lhs("$Collection")
		.rhs("$Part ?$Collection")
		.build();
	private static final RuleDTO $Content_$$AUTOCORRECT = RuleDTO.builder()
		.name("$Content_$$AUTOCORRECT")
		.lhs("$Content")
		.rhs("$$AUTOCORRECT")
		.build();
	private static final RuleDTO $Content_$ENTITY = RuleDTO.builder()
		.name("$Content_$ENTITY")
		.lhs("$Content")
		.rhs("$ENTITY")
		.build();
	private static final RuleDTO $Content_$DOCTYPE = RuleDTO.builder()
		.name("$Content_$DOCTYPE")
		.lhs("$Content")
		.rhs("$DOCTYPE")
		.build();

	public static final Set<RuleDTO> INSTANCE = Set.of(
		$ROOT_$Query,
		$Query_$Collection,
		$Part_$Intent,
		$Part_$NotIntent,
		$Intent_$Content,
		$NotIntent_$TOKEN,
		$Content_$QUOTE_TOKEN,
		$NotIntent_$StopWord,
		$Content_$AUTOCOMPLETE,
		$Content_$AGGREGATE,
		$Content_$KEYWORD_AUTOCOMPLETE,
		$Collection_$Part_$Collection,
		$Content_$$AUTOCORRECT,
		$Content_$ENTITY,
		$Content_$DOCTYPE
	);

	private Rules() {}

}
