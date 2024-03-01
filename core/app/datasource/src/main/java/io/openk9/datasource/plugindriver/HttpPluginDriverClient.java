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

package io.openk9.datasource.plugindriver;

import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.openk9.datasource.web.dto.form.PluginDriverFormDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HttpPluginDriverClient {

	public static final String HEALTH_PATH = "/health";
	public static final String SAMPLE_PATH = "/sample";
	public static final String FORM_PATH = "/form";
	public static final String INVOKE_PATH = "/invoke";
	@Inject
	WebClient webClient;
	@Inject
	Logger logger;

	public Uni<HttpResponse<Buffer>> invoke(
		HttpPluginDriverInfo httpPluginDriverInfo,
		HttpPluginDriverContext httpPluginDriverContext) {

		String path = httpPluginDriverInfo.getPath();

		if (path == null) {
			path = INVOKE_PATH;
		}

		HttpPluginDriverInfo.Method httpMethod = httpPluginDriverInfo.getMethod();

		if (httpMethod == null) {
			httpMethod = HttpPluginDriverInfo.Method.POST;
		}

		String host = httpPluginDriverInfo.getHost();

		if (host == null) {
			host = "localhost";
		}

		Integer port = httpPluginDriverInfo.getPort();

		if (port == null || port < 1 || port > 65535) {
			port = 8080;
		}

		return webClient.request(
				httpMethod.getHttpMethod(),
				port,
				host,
				path
			)
			.ssl(httpPluginDriverInfo.isSecure())
			.sendJson(httpPluginDriverContext);
	}

	public void invokeAndForget(
		HttpPluginDriverInfo httpPluginDriverInfo,
		HttpPluginDriverContext httpPluginDriverContext) {

		invoke(httpPluginDriverInfo, httpPluginDriverContext)
			.subscribe()
			.with(
				response -> {
					if (response.statusCode() != 200) {
						logger.warn(
							"response.statusCode() != 200 (" + response.statusCode() + ")" +
							" response.bodyAsString() = " + response.bodyAsString() + " " +
							"response.statusMessage() = " + response.statusMessage());
					}
					else {
						logger.info("invoke success " + httpPluginDriverInfo);
					}
				},
				t -> logger.error("HttpPluginDriverClient.invokeAndForget", t)
			);
	}

	public Uni<PluginDriverHealthDTO> getHealth(HttpPluginDriverInfo pluginDriverInfo) {
		return webClient
			.request(
				HttpMethod.GET,
				pluginDriverInfo.getPort(),
				pluginDriverInfo.getHost(),
				HEALTH_PATH
			)
			.ssl(pluginDriverInfo.isSecure())
			.send()
			.map(res -> res.bodyAsJson(PluginDriverHealthDTO.class));
	}

	public Uni<HttpResponse<Buffer>> getSample(HttpPluginDriverInfo pluginDriverInfo) {
		return webClient
			.request(
				HttpMethod.GET,
				pluginDriverInfo.getPort(),
				pluginDriverInfo.getHost(),
				SAMPLE_PATH
			)
			.ssl(pluginDriverInfo.isSecure())
			.send();
	}

	public Uni<PluginDriverFormDTO> getForm(HttpPluginDriverInfo pluginDriverInfo) {
		return webClient
			.request(
				HttpMethod.GET,
				pluginDriverInfo.getPort(),
				pluginDriverInfo.getHost(),
				FORM_PATH
			)
			.ssl(pluginDriverInfo.isSecure())
			.send()
			.map(res -> res.bodyAsJson(PluginDriverFormDTO.class));
	}

}
