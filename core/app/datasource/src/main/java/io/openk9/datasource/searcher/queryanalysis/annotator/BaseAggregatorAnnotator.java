/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.mapper.FuzzinessMapper;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.parser.impl.AclQueryParser;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import io.openk9.datasource.searcher.util.Tuple;
import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

abstract class BaseAggregatorAnnotator extends BaseAnnotator {

	public BaseAggregatorAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, RestHighLevelClient restHighLevelClient,
		String tenantId, JWT jwt,
		String...keywords) {
		this(
			bucket, annotator, stopWords, restHighLevelClient, tenantId, jwt,
			List.of(keywords));
	}

	public BaseAggregatorAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, RestHighLevelClient restHighLevelClient,
		String tenantId, JWT jwt,
		List<String> keywords) {
		super(bucket, annotator, stopWords, tenantId);
		this.keywords = keywords;
		this.restHighLevelClient = restHighLevelClient;
		this.jwt = jwt;
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
		}
		else {
			token = String.join(" ", tokens);
		}

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (String keyword : normalizedKeywords) {
			boolQueryBuilder.should(query(keyword, token));
		}

		builder.must(boolQueryBuilder);

		Iterator<AclMapping> iterator =
			bucket.getDatasources()
				.stream()
				.flatMap(d -> d.getPluginDriver().getAclMappings().stream())
				.distinct()
				.iterator();

		BoolQueryBuilder innerQuery =
			QueryBuilders
				.boolQuery()
				.minimumShouldMatch(1)
				.should(QueryBuilders.matchQuery("acl.public", true));

		while (iterator.hasNext()) {

			AclMapping aclMapping = iterator.next();

			DocTypeField docTypeField = aclMapping.getDocTypeField();

			UserField userField = aclMapping.getUserField();

			AclQueryParser.apply(docTypeField, userField.getTerms(jwt), innerQuery);

		}

		builder.filter(innerQuery);

		String[] indexNames =
			bucket
				.getDatasources()
				.stream()
				.map(Datasource::getDataIndex)
				.map(DataIndex::getName)
				.toArray(String[]::new);

		SearchRequest searchRequest = new SearchRequest(indexNames);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(0);

		searchSourceBuilder.query(builder);

		for (String keyword : normalizedKeywords) {
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

			Aggregations aggregations = search.getAggregations();

			if (aggregations != null) {

				for (Aggregation aggregation : aggregations) {

					Terms terms = (Terms) aggregation;
					for (Terms.Bucket bucket : terms.getBuckets()) {
						String keyAsString = bucket.getKeyAsString();

						if (token.equalsIgnoreCase(keyAsString)) {
							return List.of(_createCategorySemantics(
								terms.getName(), keyAsString, annotator.getFieldName()));
						}

						scoreKeys.add(
							Tuple.of(
								(Supplier<Double>) () -> _levenshteinDistance(
									token, keyAsString),
								keyAsString,
								terms.getName()));

					}

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

		return List.of(_createCategorySemantics(name, key, annotator.getFieldName()));

	}

	@Override
	protected QueryBuilder query(
		String field, String token) {
		return QueryBuilders
			.fuzzyQuery(field, token)
			.fuzziness(FuzzinessMapper.map(annotator.getFuziness()));
	}

	protected abstract CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey, String fieldName);
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

	private final RestHighLevelClient restHighLevelClient;
	private final List<String> keywords;

	protected final JWT jwt;

	private static final Logger _log = Logger.getLogger(
		BaseAggregatorAnnotator.class);

}