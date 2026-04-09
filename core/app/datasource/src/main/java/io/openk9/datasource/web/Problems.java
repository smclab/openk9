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

import jakarta.validation.ValidationException;

import io.openk9.common.model.dto.Problem;
import io.openk9.datasource.client.exception.FormEndpointException;

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

}
