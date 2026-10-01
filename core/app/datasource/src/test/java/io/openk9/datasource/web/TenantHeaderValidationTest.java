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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.openk9.common.util.web.InternalHeaders;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Checks the tenant guard the way a client sees it: on every REST endpoint of
 * the module, and on GraphQL, a missing, blank or unknown
 * {@code X-TENANT-ID} is rejected with a Problem body before any database
 * work, while a known tenant goes through unchanged.
 */
@QuarkusTest
class TenantHeaderValidationTest {

	// MockTenantRegistry knows this tenant only
	private static final String KNOWN_TENANT = "public";
	private static final String UNKNOWN_TENANT = "nonexistent";

	// no entity has this id: a call that passes the guard fails on the lookup
	// instead of writing anything.
	private static final String UNKNOWN_ID = "999999";

	private static final Set<String> BODY_METHODS =
		Set.of("POST", "PUT", "PATCH");

	@ParameterizedTest
	@MethodSource("endpoints")
	void should_reject_missing_tenant_header(WebEndpoints.Endpoint endpoint) {
		// no X-TENANT-ID at all: 400 with a Problem that names the header
		call(endpoint, given())
			.then()
			.statusCode(400)
			.body("status", equalTo(400))
			.body("detail", containsString(InternalHeaders.TENANT_ID));
	}

	@ParameterizedTest
	@MethodSource("endpoints")
	void should_reject_blank_tenant_header(WebEndpoints.Endpoint endpoint) {
		// X-TENANT-ID present but blank: same answer as a missing header
		call(endpoint, given().header(InternalHeaders.TENANT_ID, " "))
			.then()
			.statusCode(400)
			.body("status", equalTo(400))
			.body("detail", containsString(InternalHeaders.TENANT_ID));
	}

	@ParameterizedTest
	@MethodSource("endpoints")
	void should_reject_unknown_tenant(WebEndpoints.Endpoint endpoint) {
		// a tenant that is not registered: 404 with a Problem that names it,
		// not a database error
		call(endpoint, given().header(InternalHeaders.TENANT_ID, UNKNOWN_TENANT))
			.then()
			.statusCode(404)
			.body("status", equalTo(404))
			.body("detail", containsString(UNKNOWN_TENANT));
	}

	@Test
	void should_serve_known_tenant() {
		// the query of the issue, with a registered tenant, works as before
		given()
			.header(InternalHeaders.TENANT_ID, KNOWN_TENANT)
			.contentType(ContentType.JSON)
			.body("{\"query\": \"{ datasources { edges { node { name } } } }\"}")
			.when()
			.post("/graphql")
			.then()
			.statusCode(200)
			.body("errors", nullValue());
	}

	private static List<WebEndpoints.Endpoint> endpoints() {
		var endpoints = new ArrayList<>(WebEndpoints.adminEndpoints());

		// the public search frontend and the GraphQL API sit behind the same
		// guard, but are not listed among the admin resources
		endpoints.add(new WebEndpoints.Endpoint(
			"GET", "/buckets/current", "BucketResource#getCurrentBucket",
			false));
		endpoints.add(new WebEndpoints.Endpoint(
			"POST", "/graphql", "GraphQL", false));

		return endpoints;
	}

	private static Response call(
		WebEndpoints.Endpoint endpoint, RequestSpecification request) {

		var path = endpoint.path().replaceAll("\\{[^}]+}", UNKNOWN_ID);

		// an empty JSON body keeps content negotiation from answering 415
		// before the guard on the resources that consume one.
		if (BODY_METHODS.contains(endpoint.httpMethod())) {
			request = request.contentType(ContentType.JSON).body("{}");
		}

		return request.request(endpoint.httpMethod(), path);
	}

}
