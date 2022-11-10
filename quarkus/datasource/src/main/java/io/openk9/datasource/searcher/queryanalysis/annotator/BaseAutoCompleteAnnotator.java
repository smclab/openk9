package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import io.openk9.datasource.searcher.util.Utils;
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
		String...keywords) {
		this(bucket, annotator, stopWords, restHighLevelClient, List.of(keywords));
	}

	public BaseAutoCompleteAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords,
		RestHighLevelClient restHighLevelClient,
		List<String> keywords) {
		super(bucket, annotator, stopWords, null);
		this.keywords = keywords;
		this.restHighLevelClient = restHighLevelClient;
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		List<String> normalizedKeywords = keywords;

		if (normalizedKeywords == null) {
			return List.of();
		}

		String token;

		if (tokens.length == 1) {
			token = tokens[0];
			if (Utils.inQuote(token)) {
				return List.of(
					CategorySemantics.of(
						"$QUOTE_TOKEN",
						Map.of(
							"tokenType", "TEXT",
							"value", token,
							"score", 100.0f
						)
					)
				);
			}

		}
		else {
			token = String.join(" ", tokens);
		}

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		MultiMatchQueryBuilder multiMatchQueryBuilder =
			new MultiMatchQueryBuilder(token);

		multiMatchQueryBuilder.type(
			MultiMatchQueryBuilder.Type.PHRASE_PREFIX);

		for (String normalizedKeyword : normalizedKeywords) {
			multiMatchQueryBuilder.field(normalizedKeyword);
		}

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

		String[] includes =
			normalizedKeywords.stream()
				.distinct()
				.toArray(String[]::new);

		searchSourceBuilder.fetchSource(includes, null);

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

					if (value instanceof String) {
						categorySemantics.add(
							CategorySemantics.of(
								"$AUTOCOMPLETE",
								Map.of(
									"tokenType", "TEXT",
									"keywordName", keyword,
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
										"keywordName", e2.getKey(),
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

	protected final List<String> keywords;

	private static final Logger _log = Logger.getLogger(
		BaseAutoCompleteAnnotator.class);

}