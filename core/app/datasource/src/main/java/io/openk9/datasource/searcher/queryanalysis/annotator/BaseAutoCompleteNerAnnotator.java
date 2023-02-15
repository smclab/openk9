package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Bucket;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseAutoCompleteNerAnnotator extends BaseAnnotator {

	public BaseAutoCompleteNerAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, String category,
		RestHighLevelClient restHighLevelClient,
		TenantResolver tenantResolver) {
		super(bucket, annotator, stopWords, tenantResolver);
		this.category = category;
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

		multiMatchQueryBuilder.field("name.searchasyou");

		builder.must(multiMatchQueryBuilder);

		builder.must(QueryBuilders.termQuery("type", category));

		SearchRequest searchRequest = new SearchRequest(
			tenantResolver.getTenantName() + "-entity");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(annotator.getSize());

		searchSourceBuilder.query(builder);

		searchSourceBuilder.fetchSource(new String[] {"name", "type", "id", "tenantId"}, null);

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

				if (!sourceAsMap.isEmpty()) {

					Map<String, Object> entitySemantics = new HashMap<>();

					entitySemantics.put("tokenType", "ENTITY");

					entitySemantics.put("score", hit.getScore());

					for (Map.Entry<String, Object> entitySourceField : sourceAsMap.entrySet()) {
						String key = entitySourceField.getKey();
						Object value = entitySourceField.getValue();

						switch (key) {
							case "id" -> entitySemantics.put("value", value);
							case "name" ->
								entitySemantics.put("entityName", value);
							case "type" -> {
								entitySemantics.put("entityType", value);
								entitySemantics.put("label", value);
							}
							case "tenantId" ->
								entitySemantics.put("tenantId", value);
						}

					}

					categorySemantics.add(
						CategorySemantics.of(
							"$" + entitySemantics.get("entityType"),
							entitySemantics)
					);

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

	private final String category;

	private static final Logger _log = Logger.getLogger(BaseNerAnnotator.class);

	private final RestHighLevelClient restHighLevelClient;

}