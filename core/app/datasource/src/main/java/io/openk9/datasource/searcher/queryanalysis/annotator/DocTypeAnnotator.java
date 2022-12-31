package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;
import java.util.Map;

public class DocTypeAnnotator extends BaseAggregatorAnnotator {

	public DocTypeAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, RestHighLevelClient restHighLevelClient) {
		super(bucket, annotator, stopWords, restHighLevelClient, null, "documentTypes.keyword");
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey, String fieldName) {

		return CategorySemantics.of(
			"$DOCTYPE",
			Map.of(
				"tokenType", "DOCTYPE",
				"value", aggregatorKey,
				"score", 50.0f
			)
		);

	}


}