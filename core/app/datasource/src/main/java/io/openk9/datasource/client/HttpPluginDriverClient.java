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

package io.openk9.datasource.client;

import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HttpPluginDriverClient extends HttpDatasourceServiceClient {

	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";
	public static final String INVOKE_PATH = "/invoke";
	public static final String SAMPLE_PATH = "/sample";
	@Inject
	Logger logger;
	@Inject
	WebClient webClient;

	public Uni<IngestionPayload> getSample(ResourceUri resourceUri) {
		return webClient
			.requestAbs(
				HttpMethod.GET, resourceUri.getBaseUri() + SAMPLE_PATH
			)
			.timeout(10000)
			.send()
			.flatMap(this::validateResponse)
			.map(res -> res.bodyAsJson(IngestionPayload.class))
			.flatMap(this::validateDto);
	}

	public Uni<HttpResponse<Buffer>> invoke(
		ResourceUri resourceUri,
		HttpPluginDriverContext httpPluginDriverContext) {

		String path = resourceUri.getPath();

		if (path == null) {
			path = INVOKE_PATH;
		}

		String baseUri = resourceUri.getBaseUri();

		if (baseUri == null) {
			baseUri = HTTP + "localhost:8080";
		}

		return webClient.requestAbs(
				HttpMethod.POST, baseUri + path
			)
			.sendJson(httpPluginDriverContext)
			.flatMap(this::validateResponse);
	}

	public void invokeAndForget(
		ResourceUri resourceUri,
		HttpPluginDriverContext httpPluginDriverContext) {

		invoke(resourceUri, httpPluginDriverContext)
			.subscribe()
			.with(
				response -> {
					if (response.statusCode() != 200) {
						logger.warn(
							"response.statusCode() != 200 (" + response.statusCode() + ")" +
								" response.bodyAsString() = " + response.bodyAsString() + " " +
								"response.statusMessage() = " + response.statusMessage());
					} else {
						logger.info("invoke success " + resourceUri);
					}
				},
				t -> logger.error("HttpPluginDriverClient.invokeAndForget", t)
			);
	}

	private String normalize(String string) {
		if (string == null) {
			return "";
		}

		if (string.startsWith("/")) {
			string = string.substring(1);
		}
		if (string.endsWith("/")) {
			string = string.substring(0, string.length() - 1);
		}

		return string;
	}
}
