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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.impl.AclQueryParser;
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

public class BaseAutoCompleteAnnotator extends BaseAnnotator {

	public BaseAutoCompleteAnnotator(
		TenantWithBucket bucket,
		Annotator annotator,
		List<String> stopWords,
		RestHighLevelClient restHighLevelClient,
		String includeField, String searchKeyword, JWT jwt) {
		super(bucket, annotator, stopWords);
		this.includeField = includeField;
		this.searchKeyword = searchKeyword;
		this.restHighLevelClient = restHighLevelClient;
		this.jwt = jwt;
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		String token = String.join("", tokens);

		BoolQueryBuilder builder = QueryBuilders.boolQuery();

		MultiMatchQueryBuilder multiMatchQueryBuilder =
			new MultiMatchQueryBuilder(token);

		multiMatchQueryBuilder.type(
			MultiMatchQueryBuilder.Type.PHRASE_PREFIX);

		multiMatchQueryBuilder.field(searchKeyword);

		builder.must(multiMatchQueryBuilder);

		var bucket = tenantWithBucket.getBucket();

		Iterator<AclMapping> iterator = bucket.getDatasources()
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

		SearchRequest searchRequest = new SearchRequest(tenantWithBucket.getIndexNames());

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(annotator.getSize());

		searchSourceBuilder.query(builder);

		searchSourceBuilder.fetchSource(new String[] {includeField}, null);

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

				for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {

					String keyword = entry.getKey();
					Object value = entry.getValue();

					String label;

					if (annotator.getDocTypeField().getParentDocTypeField() == null) {
						label = annotator.getDocTypeField().getName();
					}
					else {
						label = annotator.getDocTypeField().getParentDocTypeField().getName();
					}


					if (value instanceof String) {
						if (!value.equals(token)) {
							categorySemantics.add(
								CategorySemantics.of(
									"$AUTOCOMPLETE",
									Map.of(
										"tokenType", "TEXT",
										"label", label,
										"value", value,
										"score", 0.2f,
										"extra", annotator.getExtraParams()
									)
								));
						}
					}
					else if (value instanceof Map) {
						for (Map.Entry<?, ?> e2 : ((Map<?, ?>) value).entrySet()) {
							if (e2.getValue() instanceof ArrayList) {
								for (String name : ((ArrayList<String>) e2.getValue())) {
									if (!name.equals(token) &&
										(name.contains(token))) {
										categorySemantics.add(
											CategorySemantics.of(
												"$AUTOCOMPLETE",
												Map.of(
													"tokenType", "TEXT",
													"label", label,
													"value", name,
													"score", 0.1f,
													"extra", annotator.getExtraParams()
												)
											)
										);
									}
								}
							}
							else {
								if (!e2.getValue().equals(token)) {
									categorySemantics.add(
										CategorySemantics.of(
											"$AUTOCOMPLETE",
											Map.of(
												"tokenType", "TEXT",
												"label", label,
												"value", e2.getValue(),
												"score", 0.1f,
												"extra", annotator.getExtraParams()
											)
										)
									);
								}
							}

						}
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

	protected final RestHighLevelClient restHighLevelClient;

	protected final String searchKeyword;

	protected final String includeField;

	protected final JWT jwt;

	private static final Logger _log = Logger.getLogger(
		BaseAutoCompleteAnnotator.class);

	@Override
	public int getLastTokenCount() {
		return 6;
	}

}