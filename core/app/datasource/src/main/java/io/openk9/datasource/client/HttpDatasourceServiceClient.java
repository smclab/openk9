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

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import io.openk9.datasource.client.exception.FormEndpointException;
import io.openk9.datasource.client.exception.InvalidUriException;
import io.openk9.datasource.client.exception.UnexpectedResponseStatusException;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.client.exception.HealthEndpointException;
import io.openk9.datasource.web.dto.HealthExpectedStatusDTO;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.web.dto.HealthDTO;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

import java.net.URI;
import java.util.List;

public abstract class HttpDatasourceServiceClient {

	@Inject
	WebClient webClient;
	@Inject
	Validator validator;

	public static final String FORM_PATH = "/form";
	public static final String HEALTH_PATH = "/health";

	/**
	 * Retrieves the form template from the remote service.
	 * Returns a failed {@link Uni} with
	 * {@link FormEndpointException} if the service does not
	 * expose a form endpoint, is unreachable, or returns an
	 * invalid response.
	 */
	public Uni<FormTemplate> getForm(ResourceUri resourceUri) {
		return webClient
			.requestAbs(
				HttpMethod.GET,
				resourceUri.getBaseUri() + FORM_PATH
			)
			.send()
			.flatMap(this::checkResponseStatus)
			.flatMap(res -> parseBody(res, FormTemplate.class))
			.onFailure()
			.transform(FormEndpointException::new);
	}

	/** Retrieves the health status from the remote service. */
	public Uni<HealthDTO> getHealth(ResourceUri resourceUri) {
		return webClient
			.requestAbs(
				HttpMethod.GET,
				resourceUri.getBaseUri() + HEALTH_PATH
			)
			.send()
			.onFailure()
			.transform(HealthEndpointException::new)
			.flatMap(this::checkHealthResponseStatus);
	}

	/**
	 * Maps an HTTP response from {@code /health} endpoint to a {@link HealthDTO}.
	 *
	 * <p>Returns {@link HealthDTO.Status#UP} or {@link HealthDTO.Status#DOWN}
	 * if the response is 2xx and the body contains a known status.
	 * Returns {@link HealthDTO.Status#DOWN} if the response status code is 503.
	 * Returns {@link HealthDTO.Status#UNKNOWN} otherwise.
	 *
	 * @param response the HTTP response from the {@code /health} endpoint
	 * @return a {@link Uni} emitting the resulting {@link HealthDTO}
	 */
	protected Uni<HealthDTO> checkHealthResponseStatus(
		HttpResponse<Buffer> response) {

		if (response.statusCode() >= 200
			&& response.statusCode() <= 299) {

			try {
				var dto = response.bodyAsJson(HealthExpectedStatusDTO.class);

				if(dto.getStatus() == null)
					return Uni.createFrom().item(
						HealthDTO.builder()
							.status(HealthDTO.Status.UNKNOWN)
							.build()
					);

				return Uni.createFrom().item(
					switch (dto.getStatus()) {
						case UP -> HealthDTO.builder()
							.status(HealthDTO.Status.UP)
							.build();

						case DOWN -> HealthDTO.builder()
							.status(HealthDTO.Status.DOWN)
							.build();
					}
				);

			} catch (Exception e) {
				return Uni.createFrom().item(
					HealthDTO.builder()
					.status(HealthDTO.Status.UNKNOWN)
					.build()
				);
			}

		}

		else if (response.statusCode() == 503) {
			return Uni.createFrom().item(
				HealthDTO.builder()
					.status(HealthDTO.Status.DOWN)
					.build()
			);
		}

		else
			return Uni.createFrom().item(
				HealthDTO.builder()
					.status(HealthDTO.Status.UNKNOWN)
					.build()
			);
	}

	/**
	 * Checks that the HTTP response status is 2xx.
	 * Returns a failed {@link Uni} with an
	 * {@link UnexpectedResponseStatusException} otherwise.
	 */
	protected Uni<HttpResponse<Buffer>> checkResponseStatus(
		HttpResponse<Buffer> response) {

		if (response.statusCode() >= 200
			&& response.statusCode() <= 299) {

			return Uni.createFrom().item(response);
		}

		return Uni.createFrom().failure(
			new UnexpectedResponseStatusException(
				response.statusCode(),
				response.statusMessage()));
	}

	/**
	 * Deserializes the response body into the given type and
	 * validates it. Deserialization errors are wrapped as
	 * {@link ValidationException}; bean validation failures
	 * propagate as {@link ConstraintViolationException}.
	 */
	protected <T> Uni<T> parseBody(
		HttpResponse<Buffer> response, Class<T> type) {

		T dto;

		try {
			dto = response.bodyAsJson(type);
		}
		catch (Exception e) {
			return Uni.createFrom().failure(
				new ValidationException(
					"Invalid response body", e));
		}

		var violations = validator.validate(dto);

		if (violations.isEmpty()) {
			return Uni.createFrom().item(dto);
		}

		return Uni.createFrom().failure(
			new ConstraintViolationException(violations));
	}

	public Uni<ResourceUri> validateBaseUri(ResourceUri resourceUri, List<String> regexValidations) {
		try {
			URI uri = URI.create(resourceUri.getBaseUri());

			for (String regex : regexValidations) {
				if(uri.getHost().matches(regex))
					return Uni.createFrom().item(resourceUri);
			}
			return Uni.createFrom().failure(
				new InvalidUriException("The provided baseUri " +
					"is not allowed by the configured whitelist")
			);
		} catch (NullPointerException | IllegalArgumentException e) {
			return Uni.createFrom().failure(
				new InvalidUriException("The provided baseUri is not a valid URI")
			);
		}
	}

}
