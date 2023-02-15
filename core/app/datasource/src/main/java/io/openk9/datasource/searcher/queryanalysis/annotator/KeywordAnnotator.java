package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import io.openk9.datasource.searcher.util.Utils;

import java.util.List;
import java.util.Map;

class KeywordAnnotator extends BaseAnnotator {

	public KeywordAnnotator(
		Bucket bucket, Annotator annotator, List<String> stopWords) {
		super(bucket, annotator, stopWords, null);
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];
			if (Utils.inQuote(token)) {
				return List.of(
					CategorySemantics.of(
						"$QUOTE_TOKEN",
						Map.of(
							"tokenType", "TEXT",
							"label", "Keyword",
							"value", token,
							"score", 100.0f
						)
					)
				);
			}

		}

		return List.of();

	}

}
