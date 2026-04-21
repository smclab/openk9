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

package io.openk9.tika.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/form")
public class FormEndpoint {

	private static final String FORM_RESOURCE = "form/form.json";

	private byte[] formJson;

	/**
	 * Eagerly loads the form schema JSON from the classpath so that every
	 * {@code GET /form} call serves the same immutable byte array.
	 */
	@PostConstruct
	public void init() {
		try (InputStream is = Thread
			.currentThread()
			.getContextClassLoader()
			.getResourceAsStream(FORM_RESOURCE)) {

			if (is == null) {
				throw new IllegalStateException(
					"Missing classpath resource: " + FORM_RESOURCE);
			}

			formJson = is.readAllBytes();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Returns the form schema describing the configuration parameters
	 * accepted by the Tika enrich item. The schema follows the standard
	 * OpenK9 enrich-item form contract and is consumed by admin-ui to
	 * render a dynamic configuration form.
	 *
	 * @return the form schema as a JSON response
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Return the configuration form schema for the Tika enrich item")
	public Response getForm() {
		return Response
			.ok(formJson)
			.type(MediaType.APPLICATION_JSON)
			.build();
	}

}
