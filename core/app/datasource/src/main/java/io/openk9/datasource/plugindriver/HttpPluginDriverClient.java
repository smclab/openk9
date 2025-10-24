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

import java.net.URI;
import java.net.URISyntaxException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.plugindriver.exception.InvalidUriException;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.web.dto.HealthDTO;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HttpPluginDriverClient {

	public static final String FORM_PATH = "/form";
	public static final String HEALTH_PATH = "/health";
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";
	public static final String INVOKE_PATH = "/invoke";
	public static final String SAMPLE_PATH = "/sample";
	@Inject
	Logger logger;
	@Inject
	Validator validator;
	@Inject
	WebClient webClient;

	public Uni<FormTemplate> getForm(HttpPluginDriverInfo pluginDriverInfo) {
		return webClient
			.requestAbs(
				HttpMethod.GET,
				createAbsUri(
					pluginDriverInfo.isSecure(),
					pluginDriverInfo.getBaseUri(),
					FORM_PATH)
			)
			.send()
			.flatMap(this::validateResponse)
			.map(res -> res.bodyAsJson(FormTemplate.class))
			.flatMap(this::validateDto);
	}

	public Uni<HealthDTO> getHealth(HttpPluginDriverInfo pluginDriverInfo) {
		return webClient
			.requestAbs(
				HttpMethod.GET,
				createAbsUri(
					pluginDriverInfo.isSecure(),
					pluginDriverInfo.getBaseUri(),
					HEALTH_PATH)
			)
			.send()
			.flatMap(this::validateResponse)
			.map(res -> res.bodyAsJson(HealthDTO.class))
			.flatMap(this::validateDto)
			.onFailure(ConstraintViolationException.class)
			.recoverWithItem(HealthDTO
				.builder()
				.status(HealthDTO.Status.UNKOWN)
				.build()
			);
	}

	public Uni<IngestionPayload> getSample(HttpPluginDriverInfo pluginDriverInfo) {
		return webClient
			.requestAbs(
				HttpMethod.GET,
				createAbsUri(
					pluginDriverInfo.isSecure(),
					pluginDriverInfo.getBaseUri(),
					SAMPLE_PATH)
			)
			.timeout(10000)
			.send()
			.flatMap(this::validateResponse)
			.map(res -> res.bodyAsJson(IngestionPayload.class))
			.flatMap(this::validateDto);
	}

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

		String baseUri = httpPluginDriverInfo.getBaseUri();

		if (baseUri == null) {
			baseUri = "localhost:8080";
		}

		return webClient.requestAbs(
				httpMethod.getHttpMethod(),
				createAbsUri(httpPluginDriverInfo.isSecure(), baseUri, path)
			)
			.sendJson(httpPluginDriverContext)
			.flatMap(this::validateResponse);
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

	private String createAbsUri(boolean isSecure, String baseUri, String path) {
		var scheme = isSecure ? HTTPS : HTTP;
		try {
			return new URI(scheme + normalize(baseUri) + "/" + normalize(path)).toString();
		}
		catch (URISyntaxException e) {
			throw new InvalidUriException(e);
		}
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

	private <T> Uni<T> validateDto(T dto) {
		var violations = validator.validate(dto);
		if (violations.isEmpty()) {
			return Uni.createFrom().item(dto);
		}
		else {
			return Uni.createFrom().failure(new ConstraintViolationException(violations));
		}
	}

	private Uni<HttpResponse<Buffer>> validateResponse(HttpResponse<Buffer> response) {
		if (response.statusCode() >= 200 && response.statusCode() <= 299) {
			return Uni.createFrom().item(response);
		}
		else {
			return Uni.createFrom().failure(new ValidationException(
				String.format(
					"Unexpected Response Status: %d, Message: %s",
					response.statusCode(),
					response.statusMessage()
				))
			);
		}
	}

}
