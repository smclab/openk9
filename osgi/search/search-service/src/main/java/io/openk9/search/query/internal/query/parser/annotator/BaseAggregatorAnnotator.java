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
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public abstract class BaseAggregatorAnnotator extends BaseAnnotator {

	public BaseAggregatorAnnotator(String...keywords) {
		this(List.of(keywords));
	}

	public BaseAggregatorAnnotator(List<String> keywords) {
		this.keywords = keywords;
	}

	@Override
	protected QueryBuilder query(
		String field, String token) {
		return QueryBuilders.fuzzyQuery(field, token);
	}

	@Override
	public List<CategorySemantics> annotate_(long tenantId, String...tokens) {

		if (Arrays.stream(tokens).allMatch(stopWords::contains)) {
			return List.of();
		}

		RestHighLevelClient restHighLevelClient =
			restHighLevelClientProvider.get();

		String token;

		if (tokens.length == 1) {
			token = tokens[0];
		}
		else {
			token = String.join(" ", tokens);
		}

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (String keyword : keywords) {
			boolQueryBuilder.should(query(keyword, token));
		}

		builder.must(boolQueryBuilder);

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
					.size(10)
			);
		}

		searchRequest.source(searchSourceBuilder);

		if (_log.isDebugEnabled()) {
			_log.debug(builder.toString());
		}

		List<Tuple> scoreKeys = new ArrayList<>();

		try {
			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (Aggregation aggregation : search.getAggregations()) {

				Terms terms =(Terms)aggregation;
				for (Terms.Bucket bucket : terms.getBuckets()) {
					String keyAsString = bucket.getKeyAsString();

					if (token.equalsIgnoreCase(keyAsString)) {
						return List.of(_createCategorySemantics(
							terms.getName(), keyAsString));
					}

					scoreKeys.add(
						Tuple.of(
							(Supplier<Double>)() -> _levenshteinDistance(token, keyAsString),
							keyAsString,
							terms.getName()));

				}

			}

		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
		}

		if (scoreKeys.isEmpty()) {
			return List.of();
		}

		scoreKeys.sort(
			Collections.reverseOrder(
				Comparator.comparingDouble(
					t -> ((Supplier<Double>)t.get(0)).get())));

		String key = (String)scoreKeys.get(0).get(1);
		String name = (String)scoreKeys.get(0).get(2);

		return List.of(_createCategorySemantics(name, key));

	}

	protected abstract CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey);

	protected void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		this.restHighLevelClientProvider = restHighLevelClientProvider;
	}

	protected RestHighLevelClientProvider restHighLevelClientProvider;

	protected final List<String> keywords;

	private static double _levenshteinDistance(String x, String y) {

		int xLength = x.length();
		int yLength = y.length();

		int[][] dp = new int[xLength + 1][yLength + 1];

		for (int i = 0; i <= xLength; i++) {
			for (int j = 0; j <= yLength; j++) {
				if (i == 0) {
					dp[i][j] = j;
				}
				else if (j == 0) {
					dp[i][j] = i;
				}
				else {
					dp[i][j] = _min(dp[i - 1][j - 1]
									+ _costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
						dp[i - 1][j] + 1,
						dp[i][j - 1] + 1);
				}
			}
		}

		return 1 - ((double)dp[xLength][yLength] / Math.max(xLength, yLength));
	}

	private static int _min(int... numbers) {
		return Arrays.stream(numbers)
			.min().orElse(Integer.MAX_VALUE);
	}

	private static int _costOfSubstitution(char a, char b) {
		return a == b ? 0 : 1;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		BaseAggregatorAnnotator.class);

}