package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatorAnnotator extends BaseAggregatorAnnotator {

	public AggregatorAnnotator(
		String keyword,
		Tenant tenant,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, RestHighLevelClient restHighLevelClient) {
		super(tenant, annotator, stopWords, restHighLevelClient, null, keyword);
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey) {

		Map<String, Object> semantics = new HashMap<>(5);

		if (false) { // TODO
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