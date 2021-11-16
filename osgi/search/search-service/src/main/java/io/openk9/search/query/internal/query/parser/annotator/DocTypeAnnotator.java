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
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
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
public class DocTypeAnnotator extends BaseAnnotator {

	@Override
	public List<CategorySemantics> annotate_(long tenantId, String...tokens) {

		RestHighLevelClient restHighLevelClient =
			_restHighLevelClientProvider.get();

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		for (String token : tokens) {
			builder.must(
				QueryBuilders.matchQuery("documentTypes", token)
			);
		}

		SearchRequest searchRequest;

		if (tenantId == -1) {
			searchRequest = new SearchRequest("*-*-data");
		}
		else {
			searchRequest = new SearchRequest(tenantId + "-*-data");
		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(0);

		searchSourceBuilder.query(builder);

		searchSourceBuilder.aggregation(
			AggregationBuilders
				.terms("documentTypes")
				.field("documentTypes")
				.size(1)
		);

		searchRequest.source(searchSourceBuilder);

		List<CategorySemantics> list = new ArrayList<>();

		_log.info(builder.toString());

		try {
			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (Aggregation aggregation : search.getAggregations()) {
				Terms terms =(Terms)aggregation;
				for (Terms.Bucket bucket : terms.getBuckets()) {
					String keyAsString = bucket.getKeyAsString();
					list.add(
						CategorySemantics.of(
							"$DOCTYPE",
							Map.of(
								"tokenType", "DOCTYPE",
								"value", keyAsString
							)
						)
					);
				}
			}

			_log.info(list.toString());

		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
		}


		return list;
	}

	@Override
	@Reference
	protected void setAnnotatorConfig(
		AnnotatorConfig annotatorConfig) {
		super.setAnnotatorConfig(annotatorConfig);
	}

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		DocTypeAnnotator.class);

}
