package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseNerAnnotator extends BaseAnnotator {

	public BaseNerAnnotator(
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
	protected QueryBuilder query(
		String field, String token) {
		return QueryBuilders
			.fuzzyQuery(field, token)
			.fuzziness(annotator.getFuziness().toElasticType());
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		if (_containsStopword(tokens)) {
			return List.of();
		}

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		builder.must(
			QueryBuilders.matchQuery(
				"type.keyword", category));

		for (String token : tokens) {
			String[] words = token.split("\\s+");
			for (String word : words) {
				if (!stopWords.contains(word)) {
					builder.must(query("name", word));
				}
			}
		}

		SearchRequest searchRequest = new SearchRequest(
			tenantResolver.getTenantName() + "-entity");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(annotator.getSize());

		searchSourceBuilder.query(builder);

		searchRequest.source(searchSourceBuilder);

		List<CategorySemantics> list = new ArrayList<>();

		if (_log.isDebugEnabled()) {
			_log.debug(builder.toString());
		}

		try {
			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : search.getHits()) {
				Map<String, Object> senamtics = hit.getSourceAsMap();
				list.add(
					CategorySemantics.of(
						"$" + senamtics.get("type"),
						Map.of(
							"tokenType", "ENTITY",
							"entityType", senamtics.get("type"),
							"label", senamtics.get("type"),
							"entityName", senamtics.get("name"),
							"tenantId", senamtics.get("tenantId"),
							"value", senamtics.get("id"),
							"score", hit.getScore()
						)
					)
				);
			}

			if (_log.isDebugEnabled()) {
				_log.debug(list.toString());
			}

		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
		}


		return list;
	}

	private final String category;

	private static final Logger _log = Logger.getLogger(BaseNerAnnotator.class);

	private final RestHighLevelClient restHighLevelClient;

}