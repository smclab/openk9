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

package io.openk9.tenantmanager.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import jakarta.inject.Inject;

import io.openk9.event.tenant.TenantEvent;
import io.openk9.tenantmanager.service.OutboxEventService;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(TenantManagerResource.class)
class TenantManagerResourceTest {

	private static final String CLIENT_SECRET = "s3cr3t-of-the-tenant";

	@Inject
	OutboxEventService outboxService;

	@Test
	void createTenant() {

		given()
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body(JsonObject.of(
				"virtualHost",
				"my.openk9.local",
				"securityConfiguration",
				"OAUTH2_ADMIN_ONLY"
			).toString())
			.log().everything()
			.post()
			.then()
			.statusCode(200);

	}

	@Test
	void createTenant_withClientSecret_neverReturnsIt() {

		// 1. Create a tenant with a client secret
		var oAuth2Settings = JsonObject.of(
			"issuerUri", "https://idp.example.com/realms/secret",
			"clientId", "openk9",
			"clientSecret", CLIENT_SECRET
		);

		var id = given()
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body(JsonObject.of(
				"virtualHost",
				"secret.openk9.local",
				"securityConfiguration",
				"OAUTH2_ADMIN_ONLY",
				"oAuth2Settings",
				oAuth2Settings
			).toString())
			.post()
			.then()
			.statusCode(200)
			.body("clientId", equalTo("openk9"))
			.body(not(containsString(CLIENT_SECRET)))
			.extract()
			.path("id");

		// 2. The secret stays server side, in the TenantCreated event
		var createEvent = outboxService.lastEvents(1)
			.await().indefinitely().getFirst();
		Assertions.assertEquals(
			TenantEvent.TENANT_CREATED, createEvent.getEventType());
		Assertions.assertTrue(
			createEvent.getPayload().contains(CLIENT_SECRET));

		// 3. The GraphQL queries of the tenant do not return it
		var query = """
			{
				tenant(id: "%s") { clientId clientSecret }
				tenants(first: 100) {
					edges { node { virtualHost clientSecret } }
				}
			}
			""".formatted(id);

		given()
			.basePath("")
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body(JsonObject.of("query", query).toString())
			.post("/api/tenant-manager/graphql")
			.then()
			.statusCode(200)
			.body("data.tenant.clientId", equalTo("openk9"))
			.body(containsString("secret.openk9.local"))
			.body(not(containsString(CLIENT_SECRET)));

	}

	@Test
	void requestDeleteTenant_emptyBody_returns400() {

		given()
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body("{}")
			.post("/delete")
			.then()
			.statusCode(400);

	}

	@Test
	void requestDeleteTenant_blankVirtualHost_returns400() {

		given()
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body(JsonObject.of("virtualHost", "").toString())
			.post("/delete")
			.then()
			.statusCode(400);

	}

	@Test
	void deleteTenant_missingToken_returns400() {

		given()
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body(JsonObject.of(
				"virtualHost", "demo.openk9.io"
			).toString())
			.delete("/delete")
			.then()
			.statusCode(400);

	}

	@Test
	void deleteTenant_blankToken_returns400() {

		given()
			.contentType(ContentType.JSON)
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.body(JsonObject.of(
				"virtualHost", "demo.openk9.io",
				"token", ""
			).toString())
			.delete("/delete")
			.then()
			.statusCode(400);

	}

}
