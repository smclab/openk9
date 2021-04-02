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

import io.openk9.core.api.constant.Constants;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.util.DatasourceContext;
import io.openk9.entity.manager.client.api.EntityManagerClient;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.http.client.HttpClient;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

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
				.map(jsonNodes -> {

					jsonNodes.put(Constants.TENANT_ID, context.getTenant().getTenantId());
					jsonNodes.put(Constants.DATASOURCE_ID, context.getDatasource().getDatasourceId());
					jsonNodes.put(Constants.CONTENT_ID, objectNode.get(Constants.CONTENT_ID));
					jsonNodes.put(Constants.RAW_CONTENT, objectNode.get(Constants.RAW_CONTENT));

					return jsonNodes;
				})
				.flatMap(this::_getEntityOrCreate)
				.map(entities -> objectNode.set(entitiesField(), entities));

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

		request.put(Constants.TENANT_ID, context.getTenant().getTenantId());

		request.put(
			Constants.DATASOURCE_ID, context.getDatasource().getDatasourceId());

		request.put(Constants.CONTENT_ID, objectNode.get(Constants.CONTENT_ID));

		return request;
	}


	private Mono<ArrayNode> _getEntityOrCreate(ObjectNode jsonNode) {

		return _entityManagerClient
			.getOrAddEntities(
				_jsonFactory.fromJsonNode(jsonNode, Request.class))
			.map(responseList -> {

				ArrayNode resultEntities = _jsonFactory.createArrayNode();

				JsonNode entitiesJsonNode = jsonNode.get(entitiesField());

				ArrayNode entitiesArrayNode = entitiesJsonNode.toArrayNode();

				for (JsonNode node : entitiesArrayNode) {

					Optional<Response> responseOptional =
						responseList
							.stream()
							.filter(response ->
								node.get("tmpId").asLong() == response.getTmpId())
							.findFirst();

					if (responseOptional.isPresent()) {

						JsonNode context = node.get("context");

						Response response = responseOptional.get();

						ObjectNode objectNode = _jsonFactory.createObjectNode();

						objectNode.put(
							"id", response.getEntity().getId());

						objectNode.put("context", context);

						resultEntities.add(objectNode);

					}

				}

				return resultEntities;

			});

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

	protected void setEntityManagerClient(
		EntityManagerClient entityManagerClient) {
		_entityManagerClient = entityManagerClient;
	}

	protected void setHttpClient(HttpClient httpClient) {
		_httpClient = httpClient;
	}

	protected void setJsonFactory(JsonFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	private HttpClient _httpClient;

	private JsonFactory _jsonFactory;

	private EntityManagerClient _entityManagerClient;

}
