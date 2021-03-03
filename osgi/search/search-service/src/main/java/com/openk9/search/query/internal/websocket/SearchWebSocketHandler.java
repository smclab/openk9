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

package com.openk9.search.query.internal.websocket;

import com.openk9.json.api.JsonFactory;
import com.openk9.ingestion.driver.manager.api.DocumentType;
import com.openk9.ingestion.driver.manager.api.DocumentTypeProvider;
import com.openk9.ingestion.driver.manager.api.PluginDriver;
import com.openk9.ingestion.driver.manager.api.PluginDriverRegistry;
import com.openk9.ingestion.driver.manager.api.SearchKeyword;
import com.openk9.http.socket.WebSocketHandler;
import com.openk9.http.socket.WebSocketMessage;
import com.openk9.http.socket.WebSocketSession;
import com.openk9.http.web.Endpoint;
import com.openk9.search.api.query.SearchToken;
import com.openk9.search.client.api.ReactorActionListener;
import com.openk9.search.client.api.RestHighLevelClientProvider;
import com.openk9.search.client.api.Search;
import com.openk9.search.client.api.util.SearchUtil;
import com.openk9.search.query.internal.response.WebSocketResponse;
import lombok.Data;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class SearchWebSocketHandler implements WebSocketHandler {
	@Override
	public String getPath() {
		return "/v1/search";
	}

	@Override
	public Publisher<Void> apply(WebSocketSession webSocketSession) {

		return Flux
			.from(webSocketSession.receive())
			.map(WebSocketMessage::getPayloadAsString)
			.map(message -> _jsonFactory.fromJson(message, Message.class))
			.switchMap(message -> {
				switch (message.type) {
					case SEARCH:
						return _toQuerySearchRequest(1, message);
					case CLOSE:

						RestHighLevelClient restHighLevelClient =
							_restHighLevelClientProvider.get();

						ClearScrollRequest clearScrollRequest =
							new ClearScrollRequest();

						clearScrollRequest.addScrollId(message.scrollId);

						return Mono.<ClearScrollResponse>create(sink ->
							restHighLevelClient.clearScrollAsync(
								clearScrollRequest, RequestOptions.DEFAULT,
								new ReactorActionListener<>(sink)));

					case CONTINUE:
						return Mono.<SearchResponse>create(
							sink -> _restHighLevelClientProvider
								.get()
								.scrollAsync(
									new SearchScrollRequest(message.scrollId)
										.scroll(TimeValue.timeValueMinutes(2)),
									RequestOptions.DEFAULT,
									new ReactorActionListener<>(sink)
								)
						);
				}

				return Mono.just(SearchUtil.EMPTY_SEARCH_RESPONSE);

			})
			.ofType(SearchResponse.class)
			.map(response -> _searchHitToResponse(response.getHits(), response.getScrollId()))
			.map(_jsonFactory::toJson)
			.map(webSocketSession::textMessage)
			.transform(webSocketSession::send);

	}

	private WebSocketResponse _searchHitToResponse(SearchHits hits, String scrollId) {

		List<Map<String, Object>> result = new ArrayList<>();

		for (SearchHit hit : hits.getHits()) {

			Map<String, Object> sourceAsMap = hit.getSourceAsMap();

			Map<String, Object> sourceMap = new HashMap<>(
				sourceAsMap.size() + 1, 1);

			sourceMap.putAll(sourceAsMap);

			sourceMap.put("id", hit.getId());

			Map<String, HighlightField> highlightFields =
				hit.getHighlightFields();

			Map<String, Object> highlightMap = new HashMap<>(
				highlightFields.size(), 1);

			for (HighlightField value : highlightFields.values()) {
				highlightMap.put(
					value.getName(),
					Arrays
						.stream(value.getFragments())
						.map(Text::string)
						.toArray(String[]::new)
				);
			}

			Map<String, Object> hitMap = new HashMap<>(2, 1);

			hitMap.put("source", sourceMap);
			hitMap.put("highlight", highlightMap);

			result.add(hitMap);

		}

		TotalHits totalHits = hits.getTotalHits();

		return new WebSocketResponse(
			result,
			totalHits.value,
			totalHits.relation == TotalHits.Relation.EQUAL_TO,
			scrollId
		);
	}

	private Mono<SearchResponse> _toQuerySearchRequest(
		long tenantId, Message message) {

		return _search.search(factory -> {

			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			Map<String, List<SearchToken>> tokenTypeGroup =
				message
					.getSearchTokens()
					.stream()
					.collect(Collectors.groupingBy(SearchToken::getTokenType));

			List<SearchToken> datasource = tokenTypeGroup.get("DATASOURCE");

			Collection<PluginDriver> pluginDriverList =
				_pluginDriverRegistry.getPluginDriverList();

			Map<PluginDriver, List<DocumentType>> pluginDriverListDocumentType =
				pluginDriverList
					.stream()
					.collect(
						Collectors.toMap(
							Function.identity(),
							pd -> {

								List<DocumentType> supportedDocumentTypes =
									_documentTypeProvider.getDocumentTypeList(
										pd.getName());

								if (supportedDocumentTypes.isEmpty()) {
									return Collections.singletonList(
										_documentTypeProvider
											.getDefaultDocumentType(
												pd.getName())
									);
								}

								return supportedDocumentTypes;
							},
							(o1, o2) -> {
								throw new IllegalStateException(
									String.format("Duplicate key %s", o1));
							},
							IdentityHashMap::new
						)
					);


			Stream<Map.Entry<PluginDriver, List<DocumentType>>>
				documentTypeStream =
				pluginDriverListDocumentType.entrySet().stream();

			if (datasource != null) {

				List<String> datasourceValues = datasource
					.stream()
					.map(SearchToken::getValues)
					.flatMap(Arrays::stream)
					.distinct()
					.collect(Collectors.toList());

				documentTypeStream =
					documentTypeStream
						.filter(
							entry ->
								datasourceValues.contains(
									entry.getKey().getName()));

			}

			List<Map.Entry<PluginDriver, List<DocumentType>>> documentTypeList =
				documentTypeStream.collect(Collectors.toList());

			if (documentTypeList.isEmpty()) {
				return SearchUtil.EMPTY_SEARCH_REQUEST;
			}

			Stream.of(
				tokenTypeGroup
					.getOrDefault("ENTITY", Collections.emptyList())
					.stream()
					.map(this::_entityEnrichBoolQuery),
				this._textEntityQuery(
					tokenTypeGroup.getOrDefault(
						"TEXT", Collections.emptyList()), documentTypeList)
			)
				.flatMap(Function.identity())
				.reduce(Consumer::andThen)
				.orElse(_DEFAULT_CONSUMER)
				.accept(boolQuery);

			org.elasticsearch.action.search.SearchRequest elasticSearchQuery;

			if (datasource != null) {

				String[] indexNames = documentTypeList
					.stream()
					.map(Map.Entry::getKey)
					.map(PluginDriver::getName)
					.distinct()
					.toArray(String[]::new);

				elasticSearchQuery =
					factory.createSearchRequestData(tenantId, indexNames);
			}
			else {
				elasticSearchQuery =
					factory.createSearchRequestData(tenantId, "*");
			}

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

			searchSourceBuilder.size(message.size);

			searchSourceBuilder.query(boolQuery);

			HighlightBuilder highlightBuilder = new HighlightBuilder();

			documentTypeList
				.stream()
				.map(Map.Entry::getValue)
				.flatMap(Collection::stream)
				.map(DocumentType::getSearchKeywords)
				.flatMap(Collection::stream)
				.map(SearchKeyword::getKeyword)
				.distinct()
				.forEach(highlightBuilder::field);

			highlightBuilder.forceSource(true);

			highlightBuilder.tagsSchema("default");

			searchSourceBuilder.highlighter(highlightBuilder);

			if (_log.isDebugEnabled()) {
				_log.debug(searchSourceBuilder.toString());
			}

			elasticSearchQuery.scroll(TimeValue.timeValueMinutes(2));

			return elasticSearchQuery.source(searchSourceBuilder);

		});

	}

	private Stream<Consumer<BoolQueryBuilder>> _textEntityQuery(
		List<SearchToken> tokenTextList,
		List<Map.Entry<PluginDriver, List<DocumentType>>> entityMapperList) {

		return tokenTextList
			.stream()
			.map(searchToken -> _termQueryPrefixValues(
				searchToken.getValues(), entityMapperList));
	}

	private Consumer<BoolQueryBuilder> _termQueryPrefixValues(
		String[] values,
		List<Map.Entry<PluginDriver, List<DocumentType>>> entityMapperList) {

		return query -> {

			if (values.length == 0) {
				return;
			}

			Map<String, Float> keywordBoostMap =
				entityMapperList
					.stream()
					.map(Map.Entry::getValue)
					.flatMap(Collection::stream)
					.map(DocumentType::getSearchKeywords)
					.flatMap(Collection::stream)
					.distinct()
					.map(SearchKeyword::getFieldBoost)
					.collect(
						Collectors.toMap(
							Map.Entry::getKey, Map.Entry::getValue));


			for (String value : values) {

				MultiMatchQueryBuilder multiMatchQueryBuilder =
					new MultiMatchQueryBuilder(value);

				multiMatchQueryBuilder.fields(keywordBoostMap);

				query.should(multiMatchQueryBuilder);

			}

		};

	}

	private Stream<DocumentType> _pluginDriverListToDocumentTypeList(
		Collection<PluginDriver> pluginDriverList) {

		return pluginDriverList
			.stream()
			.flatMap(pd -> {

				List<DocumentType> supportedDocumentTypes =
					_documentTypeProvider.getDocumentTypeList(
						pd.getName());

				if (supportedDocumentTypes.isEmpty()) {
					return Stream.of(
						_documentTypeProvider.getDefaultDocumentType(
							pd.getName()));
				}

				return supportedDocumentTypes.stream();

			});

	}

	private Consumer<BoolQueryBuilder> _entityEnrichBoolQuery(
		SearchToken searchToken) {

		return boolQueryBuilder -> {

			String[] ids = searchToken.getValues();

			String entityType = searchToken.getEntityType();

			String nestEntityPath = ENTITIES + "." + entityType;

			String nestIdPath = nestEntityPath + ".id";

			BoolQueryBuilder innerNestBoolQuery = QueryBuilders
				.boolQuery()
				.must(_multiMatchValues(nestIdPath, ids));

			String keywordKey = searchToken.getKeywordKey();

			if (keywordKey != null && !keywordKey.isEmpty()) {
				innerNestBoolQuery
					.must(QueryBuilders.matchQuery(
						nestEntityPath + ".context", keywordKey));
			}

			boolQueryBuilder.filter(
				QueryBuilders.nestedQuery(
					nestEntityPath,
					innerNestBoolQuery,
					ScoreMode.Max)
			);

		};
	}

	private QueryBuilder _multiMatchValues(String field, String[] ids) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(QueryBuilders.termQuery(field, id));
		}

		return boolQuery;

	}

	private static final Consumer<BoolQueryBuilder> _DEFAULT_CONSUMER =
		(bool) -> {};

	private static final String ENTITIES = "entities";

	@Data
	public static class Message {
		private Type type;
		private List<SearchToken> searchTokens;
		private int size;
		private String scrollId;


		enum Type {SEARCH, CONTINUE, CLOSE}

	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private Search _search;

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference
	private DocumentTypeProvider _documentTypeProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		SearchWebSocketHandler.class);

}
