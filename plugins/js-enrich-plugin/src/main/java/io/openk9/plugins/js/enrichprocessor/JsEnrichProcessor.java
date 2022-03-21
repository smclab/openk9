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

package io.openk9.plugins.js.enrichprocessor;

import io.openk9.core.api.constant.Constants;
import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.DatasourceContext;
import io.openk9.model.EnrichItem;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.api.SyncEnrichProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component(immediate = true, service = EnrichProcessor.class)
public class JsEnrichProcessor implements SyncEnrichProcessor {

	@interface Config {
		String url() default "http://js-scripts-executor:3000/";
		String path() default "/exec";
		int method() default HttpHandler.POST;
		String[] headers() default "Content-Type:application/json";
	}

	@Activate
	public void activate(Config config) {
		_config = config;
		this._httpClient = _httpClientFactory.getHttpClient(config.url());
	}

	@Modified
	public void modified(Config config) {
		activate(config);
	}

	@Override
	public Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext context,
		EnrichItem enrichItem, PluginDriverDTO pluginDriverName) {

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

			return Mono
				.from(_httpClient.request(getMethod(), getPath(), request.toString(), getHeaders()))
				.map(_jsonFactory::fromJsonToJsonNode)
				.map(JsonNode::toObjectNode)
				.map(objectNode::merge);
		});

	}

	protected ObjectNode prepareRequestRawContent(
		ObjectNode objectNode, ObjectNode datasourceConfiguration,
		DatasourceContext context, PluginDriverDTO pluginDriverDTO) {

		JsonNode rawContentNode = objectNode.get(Constants.RAW_CONTENT);

		JsonNode codeNode =
			datasourceConfiguration.get(Constants.CODE);

		ObjectNode request = _jsonFactory.createObjectNode();

		request.put(Constants.CODE, codeNode);

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

		request.put(Constants.DATASOURCE_ID, context.getDatasource().getDatasourceId());

		request.put(Constants.CONTENT_ID, objectNode.get(Constants.CONTENT_ID));

		return request;
	}

	@Override
	public String name() {
		return JsEnrichProcessor.class.getName();
	}

	protected Map<String, Object> getHeaders() {
		return Arrays
			.stream(_config.headers())
			.map(s -> s.split(":"))
			.collect(Collectors.toMap(e -> e[0], e -> e[1]));
	}

	protected int getMethod() {
		return _config.method();
	}

	protected String getPath() {
		return _config.path();
	}

	private Config _config;

	private HttpClient _httpClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private HttpClientFactory _httpClientFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

}
