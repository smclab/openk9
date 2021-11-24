package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.api.query.parser.Tuple;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseNerAnnotator extends BaseAnnotator {

	public BaseNerAnnotator(String category) {
		this.category = category;
	}

	@Override
	protected QueryBuilder query(
		String field, String token) {
		return QueryBuilders
			.fuzzyQuery(field, token)
			.fuzziness(_annotatorConfig.nerAnnotatorFuzziness());
	}

	@Override
	public List<CategorySemantics> annotate_(
		Tuple<Integer> pos, long tenantId, List<Token> tokenList) {

		if (tokenList.stream().allMatch(Token::isStopword)) {
			return List.of();
		}

		RestHighLevelClient restHighLevelClient =
			restHighLevelClientProvider.get();

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		builder.must(
			QueryBuilders.matchQuery(
				"type.keyword", category));

		for (Token token : tokenList) {
			if (!token.isStopword()) {
				builder.must(query("name", token.getToken()));
			}
		}

		SearchRequest searchRequest;

		if (tenantId == -1) {
			searchRequest = new SearchRequest("*-entity");
		}
		else {
			searchRequest = new SearchRequest(tenantId + "-entity");
		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(_annotatorConfig.nerSize());

		searchSourceBuilder.query(builder);

		searchRequest.source(searchSourceBuilder);

		List<CategorySemantics> list = new ArrayList<>();

		if (_log.isDebugEnabled()) {
			_log.debug(builder.toString());
		}

		List<CategorySemantics> optionalCategorySemantics =
			getCategorySemantics(tokenList);

		Tuple<Integer> newPos = getPos(pos, tokenList);

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
							"entityName", senamtics.get("name"),
							"tenantId", senamtics.get("tenantId"),
							"value", senamtics.get("id"),
							"score", hit.getScore()
						),
						newPos
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

		list.addAll(optionalCategorySemantics);

		return list;
	}

	protected void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		this.restHighLevelClientProvider = restHighLevelClientProvider;
	}

	protected RestHighLevelClientProvider restHighLevelClientProvider;

	private final String category;

	private static final Logger _log = LoggerFactory.getLogger(
		BaseNerAnnotator.class);

}
