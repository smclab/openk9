package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseAutoCompleteAnnotator extends BaseAnnotator {

	public BaseAutoCompleteAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords,
		RestHighLevelClient restHighLevelClient,
		String includeField, String searchKeyword) {
		super(bucket, annotator, stopWords, null);
		this.includeField = includeField;
		this.searchKeyword = searchKeyword;
		this.restHighLevelClient = restHighLevelClient;
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		String token = String.join(" ", tokens);

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		MultiMatchQueryBuilder multiMatchQueryBuilder =
			new MultiMatchQueryBuilder(token);

		multiMatchQueryBuilder.type(
			MultiMatchQueryBuilder.Type.PHRASE_PREFIX);

		multiMatchQueryBuilder.field(searchKeyword);

		builder.must(multiMatchQueryBuilder);

		String[] indexNames =
			bucket
				.getDatasources()
				.stream()
				.map(Datasource::getDataIndex)
				.map(DataIndex::getName)
				.toArray(String[]::new);

		SearchRequest searchRequest = new SearchRequest(indexNames);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(annotator.getSize());

		searchSourceBuilder.query(builder);

		searchSourceBuilder.fetchSource(new String[] {includeField}, null);

		searchRequest.source(searchSourceBuilder);

		if (_log.isDebugEnabled()) {
			_log.debug(builder.toString());
		}

		try {

			List<CategorySemantics> categorySemantics = new ArrayList<>();

			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : search.getHits()) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();

				for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {

					String keyword = entry.getKey();
					Object value = entry.getValue();

					String label;

					if (annotator.getDocTypeField().getParentDocTypeField() == null) {
						label = annotator.getDocTypeField().getName();
					}
					else {
						label = annotator.getDocTypeField().getParentDocTypeField().getName();
					}

					if (value instanceof String) {
						categorySemantics.add(
							CategorySemantics.of(
								"$AUTOCOMPLETE",
								Map.of(
									"tokenType", "TEXT",
									"label", label,
									"keywordKey", keyword,
									"value", value,
									"score", 0.1f
								)
							)
						);
					}
					else if (value instanceof Map) {
						for (Map.Entry<?, ?> e2 : ((Map<?, ?>) value).entrySet()) {
							categorySemantics.add(
								CategorySemantics.of(
									"$AUTOCOMPLETE",
									Map.of(
										"tokenType", "TEXT",
										"label", label,
										"keywordKey", e2.getKey(),
										"value", e2.getValue(),
										"score", 0.1f
									)
								)
							);
						}
					}

				}
			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"for token " + token + " found " + categorySemantics + " category semantics");
			}

			return categorySemantics;


		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
		}

		return List.of();

	}

	private boolean _arrayContains(
		List<String> autocompleteEntityFields, String keyword) {
		return autocompleteEntityFields.contains(keyword);

	}

	private boolean _arrayContains(
		String[] autocompleteEntityFields, String keyword) {

		for (String autocompleteEntityField : autocompleteEntityFields) {
			if (keyword.equals(autocompleteEntityField)) {
				return true;
			}
		}

		return false;

	}

	protected final RestHighLevelClient restHighLevelClient;

	protected final String searchKeyword;

	protected final String includeField;

	private static final Logger _log = Logger.getLogger(
		BaseAutoCompleteAnnotator.class);

	@Override
	public int getLastTokenCount() {
		return 2;
	}

}