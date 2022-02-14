package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;

import java.util.Map;

public class AggregatorAnnotator extends BaseAggregatorAnnotator {

	public AggregatorAnnotator(
		String keyword,
		AnnotatorConfig annotatorConfig,
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super(keyword);
		super.setAnnotatorConfig(annotatorConfig);
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey) {
		return CategorySemantics.of(
			"$AGGREGATE",
			Map.of(
				"tokenType", "TEXT",
				"value", aggregatorKey,
				"score", 1.0f
			)
		);
	}

}
