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

import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.parser.impl.AclQueryParser;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseKeywordAutoCompleteAnnotator extends BaseAnnotator {

	public BaseKeywordAutoCompleteAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords,
		RestHighLevelClient restHighLevelClient,
		String includeField, String searchKeyword, JWT jwt) {
		super(bucket, annotator, stopWords, null);
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
					String keyword_value;

					if (annotator.getDocTypeField().getParentDocTypeField() == null) {
						label = annotator.getDocTypeField().getName();
						keyword_value = annotator.getDocTypeField().getPath();
					}
					else {
						label = annotator.getDocTypeField().getParentDocTypeField().getName();
						keyword_value = annotator.getDocTypeField().getParentDocTypeField().getPath();
					}


					if (value instanceof String) {
						if (((String) value).startsWith(token)) {
							categorySemantics.add(
								CategorySemantics.of(
									"$KEYWORD_AUTOCOMPLETE",
									Map.of(
										"tokenType", "TEXT",
										"keywordKey", keyword_value + ".keyword",
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
												"$KEYWORD_AUTOCOMPLETE",
												Map.of(
													"tokenType", "TEXT",
													"keywordKey", keyword_value + ".keyword",
													"label", label,
													"value", name,
													"score", 0.2f,
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
											"$KEYWORD_AUTOCOMPLETE",
											Map.of(
												"tokenType", "TEXT",
												"keywordKey", keyword_value + ".keyword",
												"label", label,
												"value", e2.getValue(),
												"score", 0.2f,
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
		BaseKeywordAutoCompleteAnnotator.class);

	@Override
	public int getLastTokenCount() {
		return 5;
	}

}