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

package io.openk9.datasource.enricher;

import java.io.IOException;
import java.io.InputStream;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.TestUtils;
import io.openk9.datasource.client.HttpDatasourceServiceClient;
import io.openk9.datasource.client.HttpEnricherClient;
import io.openk9.datasource.client.exception.FormEndpointException;
import io.openk9.datasource.client.exception.UnexpectedResponseStatusException;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.model.form.FormTemplate;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.vertx.core.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(WireMockEnricher.class)
class HttpEnricherClientTest {

	private static final ResourceUri resourceUri =
		ResourceUri.builder()
			.baseUri(WireMockEnricher.HOST
				+ ":" + WireMockEnricher.PORT)
			.build();

	@Inject
	HttpEnricherClient httpEnricherClient;

	@InjectWireMock
	WireMockServer wireMockServer;

	@Test
	@RunOnVertxContext
	void should_get_form(UniAsserter asserter)
		throws IOException {

		FormTemplate expected;

		try (InputStream in = TestUtils.getResourceAsStream(
			WireMockEnricher.FORM_JSON_FILE)) {

			expected = Json.decodeValue(
				new String(in.readAllBytes()),
				FormTemplate.class
			);
		}

		// action + verify: enricher returns a valid form
		asserter.assertThat(
			() -> httpEnricherClient.getForm(resourceUri),
			res -> Assertions.assertEquals(expected, res)
		);
	}

	@Test
	@RunOnVertxContext
	void should_throw_form_endpoint_exception_when_not_found(
		UniAsserter asserter) {

		// setup: enricher does not expose /form (404)
		var notFoundStub = wireMockServer.stubFor(WireMock
			.get(HttpDatasourceServiceClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(404)
				.withStatusMessage("Not Found")
			)
		);

		asserter.assertFailedWith(
			() -> httpEnricherClient.getForm(resourceUri),
			err -> {
				Assertions.assertInstanceOf(
					FormEndpointException.class, err);
				Assertions.assertInstanceOf(
					UnexpectedResponseStatusException.class,
					err.getCause());

				wireMockServer.removeStub(notFoundStub);
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_throw_form_endpoint_exception_when_response_is_invalid(
		UniAsserter asserter) {

		// setup: enricher returns 200 with unparseable body
		var invalidBodyStub = wireMockServer.stubFor(WireMock
			.get(HttpDatasourceServiceClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(200)
				.withHeader("Content-Type", "application/json")
				.withBody("not json")
			)
		);

		asserter.assertFailedWith(
			() -> httpEnricherClient.getForm(resourceUri),
			err -> {
				Assertions.assertInstanceOf(
					FormEndpointException.class, err);
				Assertions.assertInstanceOf(
					ValidationException.class,
					err.getCause());

				wireMockServer.removeStub(invalidBodyStub);
			}
		);
	}

}
