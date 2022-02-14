package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;

import java.util.Collections;
import java.util.HashMap;
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

		Map<String, Object> semantics = new HashMap<>(5);

		if (_annotatorConfig.aggregatorKeywordKeyEnable()) {
			semantics.put("keywordKey", aggregatorName);
		}

		semantics.put("tokenType", "TEXT");
		semantics.put("keywordName", aggregatorName);
		semantics.put("value", aggregatorKey);
		semantics.put("score", 50.0f);

		return CategorySemantics.of(
			"$AGGREGATE", Collections.unmodifiableMap(semantics));

	}

}
