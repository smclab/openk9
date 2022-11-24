package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;

import java.util.List;
import java.util.Map;

class StopWordsAnnotator extends BaseAnnotator {

	public StopWordsAnnotator(
		Bucket bucket, Annotator annotator, List<String> stopwords) {
		super(bucket, annotator, stopwords, null);
	}

	@Override
	public List<CategorySemantics> annotate(String... tokens) {

		if (tokens.length == 1) {

			String token = tokens[0];

			if (stopWords.contains(token)) {
				return _RESULT;
			}
		}

		return List.of();
	}

	@Override
	public int weight() {
		return 1;
	}

	private static final List<CategorySemantics> _RESULT = List.of(
		CategorySemantics.of(
			"$StopWord",
			Map.of()
		)
	);

}