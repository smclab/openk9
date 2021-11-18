package io.openk9.search.query.internal.query.parser.annotator;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseAggregatorAnnotator extends BaseAnnotator {

	public BaseAggregatorAnnotator(String...keywords) {
		this(List.of(keywords));
	}

	public BaseAggregatorAnnotator(List<String> keywords) {
		this.keywords = keywords;
	}

	@Override
	public List<CategorySemantics> annotate_(long tenantId, String...tokens) {

		if (Arrays.stream(tokens).allMatch(stopWords::contains)) {
			return List.of();
		}

		RestHighLevelClient restHighLevelClient =
			restHighLevelClientProvider.get();

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		for (String token : tokens) {
			for (String keyword : keywords) {
				builder.must(query(keyword, token));
			}
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

		for (String keyword : keywords) {
			searchSourceBuilder.aggregation(
				AggregationBuilders
					.terms(keyword)
					.field(keyword)
					.size(1)
			);
		}

		searchRequest.source(searchSourceBuilder);

		List<CategorySemantics> list = new ArrayList<>();

		if (_log.isDebugEnabled()) {
			_log.debug(builder.toString());
		}

		try {
			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (Aggregation aggregation : search.getAggregations()) {
				Terms terms =(Terms)aggregation;
				for (Terms.Bucket bucket : terms.getBuckets()) {
					String keyAsString = bucket.getKeyAsString();
					list.add(_createCategorySemantics(
						terms.getName(), keyAsString));
				}
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

	protected abstract CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey);

	protected void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		this.restHighLevelClientProvider = restHighLevelClientProvider;
	}

	protected RestHighLevelClientProvider restHighLevelClientProvider;

	protected final List<String> keywords;

	private static final Logger _log = LoggerFactory.getLogger(
		BaseAggregatorAnnotator.class);

}