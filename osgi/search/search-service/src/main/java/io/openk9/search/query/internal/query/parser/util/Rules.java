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

package io.openk9.search.query.internal.query.parser.util;

import io.openk9.search.query.internal.query.parser.Rule;
import io.openk9.search.query.internal.query.parser.Semantic;
import io.openk9.search.query.internal.query.parser.SemanticTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Rules {

	public static List<Rule> getRules() {
		List<String> stopWords =
			List.of(
				"e", "le", "su", "i", "con", "di", "il", "del", "in", "sulle");

		List<Rule> rulesOptional =
			Stream
				.concat(
					Stream.of(Rule.of("$Optionals", "$Optional ?$Optionals")),
					stopWords.stream().map(word -> Rule.of("$Optional", word)))
				.collect(Collectors.toList());


		List<Rule> rulesBase = List.of(
			Rule.of("$ROOT", "?$Optionals $Query ?$Optionals", Semantic.identity()),
			//Rule.of("$ROOT", "$Query", Semantic.identity()),
			Rule.of("$Query", "$IntentCollection", Semantic.identity()),
			Rule.of("$IntentCollection", "$Intent ?$Optionals ?$IntentCollection",
				Semantic.identity()));


		List<Rule> rulesEntity = List.of(
			Rule.of("$Entity", "$PER", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Entity", "$ORG", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Entity", "$DATE", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Entity", "$LOC", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Intent", "$Entity", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Intent", "$TOKEN", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Intent", "$STOPWORD", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$PER", "daniele caldarini", Semantic.of(Map.of("token", "daniele caldarini"))),
			Rule.of("$PER", "cristian bianco", Semantic.of(Map.of("token", "cristian bianco")))
		);

		List<Rule> aggregation = new ArrayList<>();
		aggregation.addAll(rulesOptional);
		aggregation.addAll(rulesEntity);
		aggregation.addAll(rulesBase);
		return aggregation;
	}

}
