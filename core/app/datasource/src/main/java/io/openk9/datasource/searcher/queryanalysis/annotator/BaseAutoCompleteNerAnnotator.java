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

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

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
		String tenantId) {
		super(bucket, annotator, stopWords, tenantId);
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
			tenantId + "-entity");

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

					entitySemantics.put("tokenType", "AUTOCOMPLETE");

					entitySemantics.put("score", hit.getScore());

					for (Map.Entry<String, Object> entitySourceField : sourceAsMap.entrySet()) {
						String key = entitySourceField.getKey();
						Object value = entitySourceField.getValue();

						switch (key) {
							case "name" -> {
								entitySemantics.put("entityName", value);
								entitySemantics.put("value", value);
							}
							case "type" -> {
								entitySemantics.put("entityType", value);
								entitySemantics.put("label", value);
							}
							case "tenantId" ->
								entitySemantics.put("tenantId", value);
						}

					}

					String name = entitySemantics.get("value").toString();

					if (!name.startsWith(token) || !name.equals(token)) {
						categorySemantics.add(
							CategorySemantics.of(
								"$" + entitySemantics.get("entityType"),
								entitySemantics)
						);
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

	private final String category;

	private static final Logger _log = Logger.getLogger(BaseNerAnnotator.class);

	private final RestHighLevelClient restHighLevelClient;

}