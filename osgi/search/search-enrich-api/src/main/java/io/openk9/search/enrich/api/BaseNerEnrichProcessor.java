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

package io.openk9.search.enrich.api;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.util.DatasourceContext;
import io.openk9.http.client.HttpClient;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.search.client.api.Index;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.util.SearchUtil;
import io.openk9.core.api.constant.Constants;
import io.openk9.search.enrich.mapper.api.EntityMapperProvider;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseNerEnrichProcessor implements EnrichProcessor {

	@Override
	public abstract String name();

	@Override
	public Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext context,
		EnrichItem enrichItem, String pluginDriverName) {

		return Mono.defer(() -> {

			JsonNode datasourceConfiguration =
				_jsonFactory.fromJsonToJsonNode(enrichItem.getJsonConfig());

			if (!datasourceConfiguration.isObject()) {
				return Mono.error(
					new RuntimeException(
						"jsonConfig must be an instance of ObjectNode "
						+ datasourceConfiguration.toString()));
			}

			ObjectNode request =
				prepareRequestRawContent(
					objectNode, datasourceConfiguration.toObjectNode(),
					context, pluginDriverName);

			return Mono.from(
				_httpClient.request(
					getMethod(), getPath(), request.toString(), getHeaders()))
				.map(_jsonFactory::fromJsonToJsonNode)
				.map(JsonNode::toObjectNode)
				.flatMap(responseObj -> this._getEntityOrCreate(
					context.getTenant().getTenantId(),
					responseObj.get(entitiesField()))
					.map(jsonNodes -> responseObj.set(entitiesField(), jsonNodes))
				)
				.map(objectNode::merge);

		});

	}

	protected ObjectNode prepareRequestRawContent(
		ObjectNode objectNode, ObjectNode datasourceConfiguration,
		DatasourceContext context, String pluginDriverName) {

		JsonNode entitiesNode = datasourceConfiguration.get(entitiesField());

		JsonNode rawContentNode = objectNode.get(Constants.RAW_CONTENT);

		JsonNode confidenceNode =
			datasourceConfiguration.get(Constants.CONFIDENCE);

		ObjectNode request = _jsonFactory.createObjectNode();

		request.put(entitiesField(), entitiesNode);

		request.put(Constants.CONFIDENCE, confidenceNode);

		request.put(Constants.CONTENT, rawContentNode);

		JsonNode typeNode = objectNode.get(Constants.TYPE);

		ObjectNode datasourcePayload = _jsonFactory.createObjectNode();

		if (typeNode != null && typeNode.isArray()) {

			ArrayNode types = typeNode.toArrayNode();

			for (JsonNode typeJsonNode : types) {
				String type = typeJsonNode.asText();
				datasourcePayload.put(type, objectNode.get(type));
			}

		}

		request.put(Constants.DATASOURCE_PAYLOAD, datasourcePayload);

		return request;
	}

	protected abstract Map<String, Object> getHeaders();

	protected abstract int getMethod();

	protected abstract String getPath();

	protected String typeField() {
		return Constants.TYPE;
	}

	protected String nameField() {
		return Constants.NAME;
	}

	protected String entitiesField() {
		return Constants.ENTITIES;
	}

	protected String idField() {
		return Constants.ID;
	}

	protected void setEntityMapperProvider(
		EntityMapperProvider entityMapperProvider) {
		_entityMapperProvider = entityMapperProvider;
	}

	protected void setHttpClient(HttpClient httpClient) {
		_httpClient = httpClient;
	}

	protected void setJsonFactory(JsonFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	protected void setIndex(Index index) {
		_index = index;
	}

	protected void setSearch(Search search) {
		_search = search;
	}

	private Mono<ObjectNode> _getEntityOrCreate(
		long tenantId, JsonNode jsonNode) {

		return Flux
			.fromIterable(jsonNode::fieldNames)
			.concatMap(fieldType -> {

				JsonNode internalObject = jsonNode.get(fieldType);

				if (internalObject.isEmpty()) {
					return Mono.just(
						Tuples.of(
							_jsonFactory
								.createObjectNode()
								.set(fieldType, _jsonFactory.createArrayNode()),
							fieldType,
							0
					));
				}

				return Flux
					.fromIterable(internalObject)
					.map(e -> Tuples.of(e, fieldType, 1));
			})
			.concatMap(t2 -> {

				JsonNode entityType = t2.getT1();
				String fieldType = t2.getT2();

				if (t2.getT3() == 0) {
					return Mono.just(entityType.toObjectNode());
				}

				BoolQueryBuilder bool = QueryBuilders.boolQuery();

				ObjectNode entityTypeObj = entityType
					.toObjectNode()
					.put(typeField(), fieldType);

				JsonNode contextField;

				if (entityTypeObj.has(Constants.CONTEXT)) {
					contextField =
						entityTypeObj.remove(Constants.CONTEXT);
				}
				else {
					contextField = _jsonFactory.createArrayNode();
				}

				String nameText = entityTypeObj.get(nameField()).asText();

				bool
					.filter(QueryBuilders.matchQuery(typeField(), fieldType))
					.must(_entityMapperProvider.query(fieldType, nameText));

				return _getOrCreateDocument(
					tenantId, bool, entityTypeObj, contextField);
			})
			.reduce(this::_mergeObjectNode);

	}

	private Mono<ObjectNode> _getOrCreateDocument(
		long tenantId, BoolQueryBuilder bool, JsonNode entityType,
		JsonNode contextFields) {

		return _search.search(factory -> {

			SearchRequest searchRequestEntity =
				factory.createSearchRequestEntity(tenantId);

			SearchSourceBuilder searchSourceBuilder =
				new SearchSourceBuilder();

			searchSourceBuilder.query(bool);

			return searchRequestEntity.source(searchSourceBuilder);
		})
			.onErrorReturn(SearchUtil.EMPTY_SEARCH_RESPONSE)
			.filter(searchResponse ->
				searchResponse.getHits().getHits().length > 0)
			.cast(ActionResponse.class)
			.switchIfEmpty(
				_createIndex(
					tenantId, entityType.toString())
					.flatMap(item -> _searchIndex(tenantId, item.getId())))
			.cast(SearchResponse.class)
			.flatMapIterable(SearchResponse::getHits)
			.next()
			.map(_merge(entityType))
			.flatMap(t3 -> {
				if (t3.getT1()) {
					return _updateIndex(
						tenantId, t3.getT2(), t3.getT3().toString())
						.flatMap(br -> _searchIndex(tenantId, br.getId()))
						.flatMapIterable(SearchResponse::getHits)
						.next()
						.map(hit -> Tuples.of(
							hit.getId(), _jsonFactory.fromJsonToJsonNode(
								hit.getSourceAsString()).toObjectNode()));
				}

				return Mono.just(Tuples.of(t3.getT2(), t3.getT3()));
			})
			.map(t2 ->
				_jsonFactory
					.createObjectNode()
					.set(
						t2.getT2().get(typeField()).asText(),
						_jsonFactory.createArrayNode()
							.add(
								_jsonFactory
									.createObjectNode()
									.put(idField(), t2.getT1())
									.set(Constants.CONTEXT, contextFields)
							)
					)
			);
	}

	private ObjectNode _mergeObjectNode(ObjectNode on1, ObjectNode on2) {

		Map<String, ArrayNode> entities =
			Stream.of(on1, on2)
			.flatMap(ObjectNode::stream)
			.collect(Collectors.groupingBy(
				Map.Entry::getKey,
				Collectors.reducing(
					_jsonFactory.createArrayNode(),
					(jsonNode) ->
						_jsonFactory.createArrayNode()
							.addAll(jsonNode.getValue().toArrayNode()),
					(jsonNodes, jsonNodes2) ->
						_jsonFactory
							.createArrayNode()
							.addAll(jsonNodes)
							.addAll(jsonNodes2)

				)
			));

		ObjectNode objectNode = _jsonFactory.createObjectNode();

		objectNode.setAll(entities);

		return objectNode;
	}

	private Function<SearchHit, Tuple3<Boolean, String, ObjectNode>>
		_merge(JsonNode internalObject) {

		return hit -> {

			ObjectNode copy = internalObject.deepCopy().toObjectNode();

			Map<String, Object> documentFields = hit.getSourceAsMap();

			boolean notEqual = false;

			for (Map.Entry<String, Object> entry
				: documentFields.entrySet()) {

				String documentFieldKey = entry.getKey();

				JsonNode jsonNode = internalObject.get(documentFieldKey);

				Object value = entry.getValue();

				String documentFieldText = value.toString();

				if (jsonNode != null && !jsonNode.isEmpty()) {
					String jsonNodeText = jsonNode.asText();

					if (!jsonNodeText.equals(documentFieldText)) {
						notEqual = true;
					}

				}
				else {
					copy.putPOJO(documentFieldKey, documentFieldText);
				}

			}

			return Tuples.of(notEqual, hit.getId(), copy);

		};
	}

	private Mono<SearchResponse> _searchIndex(long tenantId, String id) {
		return _search
			.search(sFactory ->
				sFactory
					.createSearchRequestEntity(tenantId)
					.source(new SearchSourceBuilder()
						.query(
							QueryBuilders
								.idsQuery()
								.addIds(id))));
	}

	private Mono<IndexResponse> _createIndex(long tenantId, String source) {
		return _index.sendIndexRequest(
			factory -> factory
				.createEntityIndexRequest(tenantId)
				.source(source, XContentType.JSON)
				.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
		);
	}

	private Mono<UpdateResponse> _updateIndex(
		long tenantId, String id, String source) {
		return _index.sendUpdateRequest(
			factory -> factory
				.createEntityUpdateRequest(tenantId, id)
				.doc(source, XContentType.JSON)
				.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
		);
	}

	private HttpClient _httpClient;

	private JsonFactory _jsonFactory;

	private Search _search;

	private Index _index;

	private EntityMapperProvider _entityMapperProvider;

}
