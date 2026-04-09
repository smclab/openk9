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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.openk9.datasource.client.HttpDatasourceServiceClient;
import io.openk9.datasource.enricher.InjectWireMock;
import io.openk9.datasource.enricher.WireMockEnricher;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(WireMockEnricher.class)
@TestHTTPEndpoint(EnricherResource.class)
class EnricherResourceGetFormTest {

	private static final String ENRICHER_URI =
		"{\"baseUri\":\"" + WireMockEnricher.HOST
			+ ":" + WireMockEnricher.PORT + "\"}";

	@InjectWireMock
	WireMockServer wireMockServer;

	@Test
	void should_return_form_when_enricher_exposes_it() {
		// action + verify: 200 with form fields
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(ENRICHER_URI)
			.post("/form")
			.then()
			.statusCode(200)
			.body("fields", notNullValue());
	}

	@Test
	void should_return_502_when_enricher_has_no_form() {
		// setup: enricher returns 404
		var stub = wireMockServer.stubFor(WireMock
			.get(HttpDatasourceServiceClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(404)
				.withStatusMessage("Not Found")
			)
		);

		// action + verify: 502 with Problem body
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(ENRICHER_URI)
			.post("/form")
			.then()
			.statusCode(502)
			.body("title", equalTo("Form not available"));

		wireMockServer.removeStub(stub);
	}

	@Test
	void should_return_502_when_enricher_returns_invalid_body() {
		// setup: enricher returns 200 with unparseable body
		var stub = wireMockServer.stubFor(WireMock
			.get(HttpDatasourceServiceClient.FORM_PATH)
			.willReturn(ResponseDefinitionBuilder
				.responseDefinition()
				.withStatus(200)
				.withHeader("Content-Type", "application/json")
				.withBody("not json")
			)
		);

		// action + verify: 502 with validation Problem detail
		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(ENRICHER_URI)
			.post("/form")
			.then()
			.statusCode(502)
			.body("title", equalTo("Form not available"))
			.body("detail", equalTo(
				"The service returned an invalid"
					+ " form response"));

		wireMockServer.removeStub(stub);
	}

}
