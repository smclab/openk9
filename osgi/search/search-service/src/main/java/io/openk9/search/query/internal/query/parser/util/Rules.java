package io.openk9.search.query.internal.query.parser.util;

import io.openk9.search.query.internal.query.parser.Rule;
import io.openk9.search.query.internal.query.parser.Semantic;
import io.openk9.search.query.internal.query.parser.SemanticTypes;

import java.util.ArrayList;
import java.util.List;
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
				Semantic.of(SemanticTypes::merge)));


		List<Rule> rulesEntity = List.of(
			Rule.of("$Entity", "$PER", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Entity", "$ORG", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Entity", "$DATE", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Entity", "$LOC", Semantic.of(sems ->  SemanticTypes.of(sems.get(0)))),
			Rule.of("$Intent", "$Entity", Semantic.of(sems ->  SemanticTypes.of(sems.get(0))))
		);

		List<Rule> aggregation = new ArrayList<>();
		aggregation.addAll(rulesOptional);
		aggregation.addAll(rulesEntity);
		aggregation.addAll(rulesBase);
		return aggregation;
	}

}
