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

package io.openk9.datasource.web;

import io.openk9.common.model.dto.Problem;
import io.openk9.common.util.web.InternalHeaders;
import io.openk9.datasource.client.exception.InvalidUriException;
import io.openk9.datasource.client.exception.HealthEndpointException;
import io.openk9.datasource.client.exception.FormEndpointException;
import io.openk9.datasource.config.model.SchemaVersion;

import jakarta.ws.rs.core.Response;
import jakarta.validation.ValidationException;
import java.net.ConnectException;

/**
 * Factory methods for standard {@link Problem} responses used across
 * REST resources.
 */
public final class Problems {

	private Problems() {}

	/**
	 * Builds a 502 Problem for a failed {@code /form} call.
	 * The detail message varies based on the underlying cause:
	 * a validation failure indicates a malformed response,
	 * while other causes indicate an unreachable or missing
	 * endpoint.
	 */
	public static Problem formEndpointError(
		FormEndpointException exception) {

		var problem = new Problem();
		problem.setStatus(502);
		problem.setTitle("Form not available");
		problem.setDetail(formEndpointErrorDetail(exception));
		return problem;
	}

	public static Problem missingTenantId() {
		var problem = new Problem();
		problem.setStatus(400);
		problem.setTitle("Missing tenantId");
		problem.setDetail(InternalHeaders.TENANT_ID + " header is missing or empty");
		return problem;
	}

	public static Problem malformedTenantId(String tenantId) {
		var problem = new Problem();
		problem.setStatus(400);
		problem.setTitle("Malformed tenantId");
		problem.setDetail(
			"'" + tenantId + "' is not a valid " + InternalHeaders.TENANT_ID
			+ ": it must be the tenant schema name (a lowercase letter followed"
			+ " by lowercase letters or digits), not its virtual host");
		return problem;
	}

	public static Problem invalidTenantId(String tenantId) {
		var problem = new Problem();
		problem.setStatus(404);
		problem.setTitle("Invalid tenantId");
		problem.setDetail(
			"No tenant exists for the " + InternalHeaders.TENANT_ID
			+ " value '" + tenantId + "'");
		return problem;
	}

	public static Problem healthEndpointError(
		HealthEndpointException exception) {

		var problem = new Problem();
		problem.setStatus(502);
		problem.setTitle("Health not available");
		problem.setDetail(healthEndpointErrorDetail(exception));
		return problem;
	}

	public static Problem invalidUri(
		InvalidUriException exception,
		Response.Status status) {

		var problem = new Problem();
		problem.setStatus(status.getStatusCode());
		problem.setTitle("Invalid baseUri");
		problem.setDetail(exception.getMessage());
		return problem;
	}

	/**
	 * Builds a 400 Problem for a configuration package whose schema version this
	 * runtime cannot accept (see {@link SchemaVersion} for the compatibility
	 * policy).
	 */
	public static Problem unsupportedSchemaVersion(String rawVersion) {
		var problem = new Problem();
		problem.setStatus(400);
		problem.setTitle("Unsupported schema version");
		problem.setDetail(
			"Unsupported schema version '" + rawVersion
			+ "'; this runtime supports major " + SchemaVersion.CURRENT.major()
			+ ", minor <= " + SchemaVersion.CURRENT.minor()
			+ " (the major must match and the minor must be no newer than the "
			+ "current one).");
		return problem;
	}

	/**
	 * Builds a 400 Problem for a configuration package that is malformed or
	 * cannot be bound (e.g. an unknown entity type or an invalid attribute shape).
	 */
	public static Problem malformedPackage(String detail) {
		var problem = new Problem();
		problem.setStatus(400);
		problem.setTitle("Malformed configuration package");
		problem.setDetail(detail);
		return problem;
	}

	private static String formEndpointErrorDetail(
		FormEndpointException exception) {

		if (exception.getCause()
			instanceof ValidationException) {

			return "The service returned an invalid"
				+ " form response";
		}

		return "The service does not expose"
			+ " a form endpoint";
	}

	private static String healthEndpointErrorDetail(
		HealthEndpointException exception) {

		if (exception.getCause()
			instanceof ConnectException) {

			return "The service is currently unreachable.";
		}

		return "The service does not expose a health endpoint " +
			"or an error occurred during the request.";
	}

}
