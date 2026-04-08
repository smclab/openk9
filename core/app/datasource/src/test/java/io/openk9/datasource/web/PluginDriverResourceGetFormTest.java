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

import java.time.Duration;

import jakarta.ws.rs.WebApplicationException;

import io.openk9.datasource.client.exception.FormEndpointException;
import io.openk9.datasource.model.form.FormField;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.service.PluginDriverService;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PluginDriverResourceGetFormTest {

	private PluginDriverResource resource;
	private PluginDriverService service;

	@BeforeEach
	void setUp() {
		resource = new PluginDriverResource();
		service = Mockito.mock(PluginDriverService.class);
		resource.service = service;
	}

	@Test
	void should_return_form_template_when_connector_exposes_form() {
		// setup: connector returns a valid form template
		var expected = FormTemplate.builder()
			.field(FormField.builder().name("url").build())
			.build();

		Mockito.when(service.getForm(1L))
			.thenReturn(Uni.createFrom().item(expected));

		// action
		FormTemplate result = resource.getForm(1L)
			.await().atMost(Duration.ofSeconds(5));

		// verify the form template is returned as-is
		Assertions.assertEquals(expected, result);
	}

	@Test
	void should_throw_502_when_connector_has_no_form() {
		// setup: connector does not expose /form (404)
		Mockito.when(service.getForm(1L))
			.thenReturn(Uni.createFrom().failure(
				new FormEndpointException(
					"Connector returned 404")));

		// action + verify 502 WebApplicationException
		var exception = Assertions.assertThrows(
			WebApplicationException.class,
			() -> resource.getForm(1L)
				.await().atMost(Duration.ofSeconds(5)));

		Assertions.assertEquals(
			502, exception.getResponse().getStatus());
	}

	@Test
	void should_throw_502_when_connector_is_unreachable() {
		// setup: connector is unreachable
		Mockito.when(service.getForm(1L))
			.thenReturn(Uni.createFrom().failure(
				new FormEndpointException(
					new java.net.ConnectException(
						"Connection refused"))));

		// action + verify 502 WebApplicationException
		var exception = Assertions.assertThrows(
			WebApplicationException.class,
			() -> resource.getForm(1L)
				.await().atMost(Duration.ofSeconds(5)));

		Assertions.assertEquals(
			502, exception.getResponse().getStatus());
	}

}
