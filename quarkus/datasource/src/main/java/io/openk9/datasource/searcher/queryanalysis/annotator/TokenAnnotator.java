package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;

import java.util.List;
import java.util.Map;
import java.util.Set;

class TokenAnnotator extends BaseAnnotator {

	public TokenAnnotator(
		Tenant tenant, Annotator annotator, List<String> stopWords) {
		super(tenant, annotator, stopWords, null);
	}

	@Override
	public List<CategorySemantics> annotate(
		Set<String> context, String... tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];

			if (stopWords.contains(token)) {
				return List.of();
			}

			if (!context.contains(token)) {
				return List.of(
					CategorySemantics.of(
						"$TOKEN",
						Map.of(
							"tokenType", "TOKEN",
							"value", token,
							"score", 1.0f
						)
					)
				);
			}

		}

		return List.of();
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];
			return List.of(
				CategorySemantics.of(
					"$TOKEN",
					Map.of(
						"tokenType", "TOKEN",
						"value", token,
						"score", 1.0f
					)
				)
			);
		}

		return List.of();

	}

	@Override
	public int weight() {
		return 10;
	}

}
