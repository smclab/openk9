package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatorAnnotator extends BaseAggregatorAnnotator {

	public AggregatorAnnotator(
		String keyword,
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, RestHighLevelClient restHighLevelClient) {
		super(bucket, annotator, stopWords, restHighLevelClient, null, keyword);
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey, String fieldName) {

		Map<String, Object> semantics = new HashMap<>(5);

		if (false) { // TODO
			semantics.put("keywordKey", aggregatorName);
		}

		String label;

		if (annotator.getDocTypeField().getParentDocTypeField() == null) {
			label = annotator.getDocTypeField().getName();
		}
		else {
			label = annotator.getDocTypeField().getParentDocTypeField().getName();
		}

		semantics.put("tokenType", "TEXT");
		semantics.put("label", label);
		semantics.put("keywordKey", aggregatorName);
		semantics.put("value", aggregatorKey);
		semantics.put("score", 50.0f);
		semantics.put("extra", annotator.getExtraParams());

		return CategorySemantics.of(
			"$AGGREGATE", Collections.unmodifiableMap(semantics));

	}

}