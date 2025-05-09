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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openk9.datasource.mapper.FuzzinessMapper;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;

import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

public class BaseNerAnnotator extends BaseAnnotator {

	public BaseNerAnnotator(
		TenantWithBucket tenantWithBucket,
		Annotator annotator,
		List<String> stopWords,
		String category,
		RestHighLevelClient restHighLevelClient) {
		super(tenantWithBucket, annotator, stopWords);
		this.category = category;
		this.restHighLevelClient = restHighLevelClient;
	}

	@Override
	protected QueryBuilder query(
		String field, String token) {
		return QueryBuilders
			.fuzzyQuery(field, token)
			.fuzziness(FuzzinessMapper.map(annotator.getFuziness()));
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

		var tenant = tenantWithBucket.getTenant();
		var tenantId = tenant.schemaName();

		SearchRequest searchRequest = new SearchRequest(tenantId + "-entity");

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