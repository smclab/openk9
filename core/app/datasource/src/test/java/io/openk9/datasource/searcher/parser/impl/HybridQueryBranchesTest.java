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

package io.openk9.datasource.searcher.parser.impl;

import java.util.Base64;
import java.util.List;

import io.openk9.datasource.pipeline.service.EmbeddingService;

import com.jayway.jsonpath.JsonPath;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HybridQueryBranchesTest {

	private static final int K_NEIGHBORS = 3;
	private static final int PAGINATION_DEPTH = 100;


	private static final Query TEXT_QUERY = new MultiMatchQuery.Builder()
		.fields("chunkText")
		.query("test values")
		.build()
		.toQuery();

	private static final Query KNN_QUERY = KnnQueryParser.toKnnQuery(
		new EmbeddingService.EmbeddedText(List.of(0.1f, 0.2f, 0.3f)), K_NEIGHBORS);

	@Test
	void should_apply_the_datasource_filter_to_both_branches() {
		// a request with the hybrid token and a datasource filter
		var baseQuery = QueryBuilders.boolQuery()
			.filter(QueryBuilders.termsQuery("datasourceId", List.of(77L)));

		var hybridQuery = toHybridJson(baseQuery);

		// the textual branch filters as a TEXT search does
		List<Object> textTerms = JsonPath.read(
			hybridQuery, "$.hybrid.queries[0].bool.filter[*].terms.datasourceId");
		assertEquals(1, textTerms.size());

		// the knn branch searches the neighbors among the filtered documents
		List<Object> knnTerms = JsonPath.read(
			hybridQuery,
			"$.hybrid.queries[1].knn.vector.filter.bool.filter[*].terms.datasourceId");
		assertEquals(1, knnTerms.size());
	}

	@Test
	void should_restrict_the_knn_query_with_the_must_clauses() {
		// the DATE token adds a range as a must clause
		var baseQuery = QueryBuilders.boolQuery()
			.filter(QueryBuilders.termQuery("acl.public", true))
			.must(QueryBuilders.rangeQuery("pubDate").gte(1000L));

		var knnBranchJson = HybridQueryParser.toKnnBranch(baseQuery, KNN_QUERY);
		var knnBranch = knnBranchJson.encode();

		// the range restricts the neighbors, in filter context
		List<Object> ranges = JsonPath.read(
			knnBranch, "$.bool.must[*].knn.vector.filter.bool.filter[*].range");
		assertEquals(1, ranges.size());

		// and scores around the knn query as in a KNN search
		List<Object> scoringRanges = JsonPath.read(
			knnBranch, "$.bool.must[*].range");
		assertEquals(1, scoringRanges.size());

		// the filter clauses do not repeat outside the knn query
		assertNull(knnBranchJson.getJsonObject("bool").getJsonArray("filter"));
	}

	@Test
	void should_keep_the_should_clauses_as_boosts() {
		// the DATE_ORDER token adds a should clause, with no filter
		var baseQuery = QueryBuilders.boolQuery()
			.should(QueryBuilders.existsQuery("pubDate"));

		var knnBranch = HybridQueryParser.toKnnBranch(baseQuery, KNN_QUERY).encode();

		// the boost does not restrict the neighbors
		List<Object> knnFilters = JsonPath.read(
			knnBranch, "$.bool.must[*].knn.vector.filter");
		assertTrue(knnFilters.isEmpty());

		// it reorders them instead
		List<Object> boosts = JsonPath.read(knnBranch, "$.bool.should[*].exists");
		assertEquals(1, boosts.size());
	}

	@Test
	void should_restrict_with_the_should_clauses_with_a_minimum() {
		var baseQuery = QueryBuilders.boolQuery()
			.should(QueryBuilders.termQuery("category", "news"))
			.should(QueryBuilders.termQuery("category", "blog"))
			.minimumShouldMatch(1);

		var knnBranch = HybridQueryParser.toKnnBranch(baseQuery, KNN_QUERY).encode();

		List<Object> knnShould = JsonPath.read(
			knnBranch, "$.bool.must[*].knn.vector.filter.bool.should[*]");
		assertEquals(2, knnShould.size());

		List<String> knnMinimum = JsonPath.read(
			knnBranch,
			"$.bool.must[*].knn.vector.filter.bool.minimum_should_match");
		assertEquals(List.of("1"), knnMinimum);

		assertEquals("1", JsonPath.read(knnBranch, "$.bool.minimum_should_match"));
	}

	@Test
	void should_exclude_with_the_must_not_clauses() {
		var baseQuery = QueryBuilders.boolQuery()
			.mustNot(QueryBuilders.termQuery("category", "draft"));

		var knnBranch = HybridQueryParser.toKnnBranch(baseQuery, KNN_QUERY).encode();

		// no scoring clause: the knn query stands alone, with its filter
		List<Object> mustNot = JsonPath.read(
			knnBranch, "$.knn.vector.filter.bool.must_not[*].term");
		assertEquals(1, mustNot.size());
	}

	@Test
	void should_keep_the_knn_query_bare_without_a_base_query() {
		var hybridQuery = toHybridJson(QueryBuilders.boolQuery());

		assertEquals(
			K_NEIGHBORS,
			JsonPath.parse(hybridQuery)
				.read("$.hybrid.queries[1].knn.vector.k", Integer.class));

		assertFalse(hybridQuery.contains("filter"));
	}

	@Test
	void should_add_the_textual_query_as_a_must_clause() {
		var baseQuery = QueryBuilders.boolQuery()
			.should(QueryBuilders.existsQuery("pubDate"));

		var textBranch = HybridQueryParser.toTextBranch(baseQuery, TEXT_QUERY).encode();

		// the textual query is mandatory, the should clauses stay boosts
		List<Object> multiMatch = JsonPath.read(
			textBranch, "$.bool.must[*].multi_match");
		assertEquals(1, multiMatch.size());

		List<Object> boosts = JsonPath.read(textBranch, "$.bool.should[*].exists");
		assertEquals(1, boosts.size());
	}

	@Test
	void should_leave_the_base_query_untouched() {
		var baseQuery = QueryBuilders.boolQuery()
			.filter(QueryBuilders.termQuery("acl.public", true))
			.must(QueryBuilders.rangeQuery("pubDate").gte(1000L));

		var before = baseQuery.toString();

		toHybridJson(baseQuery);

		assertEquals(before, baseQuery.toString());
	}

	@Test
	void should_set_the_pagination_depth() {
		var hybridQuery = toHybridJson(QueryBuilders.boolQuery());

		assertEquals(
			PAGINATION_DEPTH,
			JsonPath.parse(hybridQuery)
				.read("$.hybrid.pagination_depth", Integer.class));
	}

	@Test
	void should_keep_the_same_depth_across_the_first_pages() {
		// the first page and the page right before the depth
		var firstPage = pagination(0, 10, new JsonObject());
		var lastPage = pagination(90, 10, new JsonObject());

		assertEquals(HybridQueryParser.DEFAULT_PAGINATION_DEPTH, firstPage);
		assertEquals(HybridQueryParser.DEFAULT_PAGINATION_DEPTH, lastPage);
	}

	@Test
	void should_grow_the_depth_beyond_it() {
		assertEquals(110, pagination(100, 10, new JsonObject()));
	}

	@Test
	void should_read_the_depth_from_the_configuration() {
		var jsonConfig = new JsonObject().put("paginationDepth", 50);

		assertEquals(50, pagination(0, 10, jsonConfig));
	}

	@Test
	void should_stop_the_depth_at_the_result_window() {
		var jsonConfig = new JsonObject().put("paginationDepth", 50_000);

		assertEquals(
			HybridQueryParser.MAX_PAGINATION_DEPTH,
			pagination(0, 10, jsonConfig));
	}

	@Test
	void should_cover_the_default_page_without_a_range() {
		var jsonConfig = new JsonObject().put("paginationDepth", 5);

		var depth = HybridQueryParser.getPaginationDepth(
			jsonConfig, new SearchSourceBuilder());

		// OpenSearch serves 10 hits when the request sets no size
		assertEquals(10, depth);
	}

	private static int pagination(int from, int size, JsonObject jsonConfig) {
		return HybridQueryParser.getPaginationDepth(
			jsonConfig, new SearchSourceBuilder().from(from).size(size));
	}

	private static String toHybridJson(BoolQueryBuilder baseQuery) {
		var wrapperQuery = HybridQueryParser.toHybridQuery(
			baseQuery, TEXT_QUERY, KNN_QUERY, PAGINATION_DEPTH);

		String encoded = JsonPath.read(wrapperQuery.toString(), "$.wrapper.query");

		return new String(Base64.getDecoder().decode(encoded));
	}

}
