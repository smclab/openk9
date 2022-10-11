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

package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.query.internal.query.parser.util.Utils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseAutoCompleteNerAnnotator extends BaseAnnotator {

	private final Map<Long, List<String>> tenantKeywordsMap;

	public BaseAutoCompleteNerAnnotator(String...keywords) {
		this(List.of(keywords));
	}

	public BaseAutoCompleteNerAnnotator(List<String> keywords) {
		this.tenantKeywordsMap = _createTenantKeywordsMap(keywords);
	}

	@Override
	public List<CategorySemantics> annotate_(long tenantId, String...tokens) {

		List<String> normalizedKeywords =
			tenantKeywordsMap.getOrDefault(
				tenantId, tenantKeywordsMap.get(-1L));

		String[] autocompleteEntityTypes =
			_annotatorConfig.autocompleteEntityTypes();

		if (normalizedKeywords == null) {
			return List.of();
		}

		RestHighLevelClient restHighLevelClient =
			restHighLevelClientProvider.get();

		String token;

		if (tokens.length == 1) {
			token = tokens[0];
			if (Utils.inQuote(token)) {
				return List.of(
					CategorySemantics.of(
						"$QUOTE_TOKEN",
						Map.of(
							"tokenType", "TEXT",
							"value", token,
							"score", 100.0f
						)
					)
				);
			}

		}
		else {
			token = String.join(" ", tokens);
		}

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		MultiMatchQueryBuilder multiMatchQueryBuilder =
			new MultiMatchQueryBuilder(token);

		multiMatchQueryBuilder.type(
			MultiMatchQueryBuilder.Type.BOOL_PREFIX);

		multiMatchQueryBuilder.field("name.searchasyou");

		builder.must(multiMatchQueryBuilder);

		List<CategorySemantics> categorySemantics = new ArrayList<>();

		for (String autocompleteEntityType : autocompleteEntityTypes) {

			builder.must(QueryBuilders.termQuery("type", autocompleteEntityType));

			SearchRequest searchRequest;

			if (tenantId == -1) {
				searchRequest = new SearchRequest("*-entity");
			}
			else {
				searchRequest = new SearchRequest(tenantId + "-entity");
			}

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

			searchSourceBuilder.size(_annotatorConfig.autocompleteSize());

			searchSourceBuilder.query(builder);

			String[] autocompleteEntityFields =
				_annotatorConfig.autocompleteEntityFields();

			String[] includes =
				Stream.concat(
						normalizedKeywords.stream(),
						Arrays.stream(autocompleteEntityFields))
					.distinct()
					.toArray(String[]::new);

			searchSourceBuilder.fetchSource(includes, null);

			searchRequest.source(searchSourceBuilder);

			if (_log.isDebugEnabled()) {
				_log.debug(builder.toString());
			}

			try {

				SearchResponse search =
					restHighLevelClient.search(
						searchRequest, RequestOptions.DEFAULT);

				for (SearchHit hit : search.getHits()) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();

					if (!sourceAsMap.isEmpty()) {

						Map<String, Object> entitySemantics = new HashMap<>();

						entitySemantics.put("tokenType", "ENTITY");

						entitySemantics.put("score", hit.getScore());

						for (Map.Entry<String, Object> entitySourceField : sourceAsMap.entrySet()) {
							String key = entitySourceField.getKey();
							Object value = entitySourceField.getValue();

							switch (key) {
								case "id":
									entitySemantics.put("value", value);
									break;
								case "name":
									entitySemantics.put("entityName", value);
									break;
								case "type":
									entitySemantics.put("entityType", value);
									break;
								case "tenantId":
									entitySemantics.put("tenantId", value);
									break;
							}

						}

						categorySemantics.add(
							CategorySemantics.of("$" + entitySemantics.get("entityType"), entitySemantics)
						);

					}

				}

				if (_log.isDebugEnabled()) {
					_log.debug(
						"for token {} found {} category semantics", token,
						categorySemantics);
				}

			}
			catch (IOException e) {
				_log.error(e.getMessage(), e);
			}

		}

		return categorySemantics;

	}

	private boolean _arrayContains(
		List<String> autocompleteEntityFields, String keyword) {
		return autocompleteEntityFields.contains(keyword);

	}

	private boolean _arrayContains(
		String[] autocompleteEntityFields, String keyword) {

		for (String autocompleteEntityField : autocompleteEntityFields) {
			if (keyword.equals(autocompleteEntityField)) {
				return true;
			}
		}

		return false;

	}

	private Map<Long, List<String>> _createTenantKeywordsMap(
		List<String> keywords) {

		if (keywords == null) {
			return Map.of();
		}

		Map<Long, List<String>> tenantKeywordsMap = new HashMap<>();

		for (String keyword : keywords) {
			long tenantId = -1;
			if (keyword.contains(";")) {
				String[] split = keyword.split(";");
				tenantId = Long.parseLong(split[0]);
				keyword = split[1];
			}

			List<String> value = tenantKeywordsMap.computeIfAbsent(
				tenantId, (k) -> new ArrayList<>());

			if (!_arrayContains(value, keyword)) {
				value.add(keyword);
			}

		}

		List<String> allTenantKeywords = tenantKeywordsMap.get(-1L);

		if (allTenantKeywords != null) {
			for (Map.Entry<Long, List<String>> e : tenantKeywordsMap.entrySet()) {
				if (e.getKey() == -1L) {
					continue;
				}
				List<String> value = e.getValue();
				for (String allTenantKeyword : allTenantKeywords) {
					if (!_arrayContains(value, allTenantKeyword)) {
						value.add(allTenantKeyword);
					}
				}
			}
		}

		return tenantKeywordsMap;

	}

	protected void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		this.restHighLevelClientProvider = restHighLevelClientProvider;
	}

	protected RestHighLevelClientProvider restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		BaseAutoCompleteNerAnnotator.class);

}