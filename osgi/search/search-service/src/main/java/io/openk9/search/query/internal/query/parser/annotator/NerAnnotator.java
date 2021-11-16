package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true, service = Annotator.class
)
public class NerAnnotator implements Annotator {

	@Override
	public List<CategorySemantics> annotate(long tenantId, String...tokens) {

		RestHighLevelClient restHighLevelClient =
			_restHighLevelClientProvider.get();

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		for (String token : tokens) {
			builder.must(
				QueryBuilders.matchQuery("name", token)
			);
		}

		SearchRequest searchRequest;

		if (tenantId == -1) {
			searchRequest = new SearchRequest("*-entity");
		}
		else {
			searchRequest = new SearchRequest(tenantId + "-entity");
		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(100);

		searchSourceBuilder.query(builder);

		searchRequest.source(searchSourceBuilder);

		List<CategorySemantics> list = new ArrayList<>();

		try {
			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : search.getHits()) {
				Map<String, Object> senamtics = hit.getSourceAsMap();
				list.add(
					CategorySemantics.of(
						(String)senamtics.get("type"),
						Map.of(
							"tokenType", "ENTITY",
							"entityType", senamtics.get("type"),
							"entityName", senamtics.get("name"),
							"tenantId", senamtics.get("tenantId"),
							"value", senamtics.get("id")
						)
					)
				);
			}

			_log.info(list.toString());

		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
		}


		return list;
	}

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		NerAnnotator.class);

}
