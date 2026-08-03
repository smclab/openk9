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

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checks the admin authorization policy the way a client sees it: every
 * endpoint of the admin resources answers 401 without credentials, and keeps
 * answering with an admin identity.
 *
 * <p>The policy is declared for the production profile only, so the test
 * profile replays it — taking the very paths that ship in the image.
 */
@QuarkusTest
@TestProfile(AdminEndpointsSecurityTest.AdminPolicyProfile.class)
class AdminEndpointsSecurityTest {

	private static final String ADMIN_USER = "admin";
	private static final String ADMIN_PASSWORD = "test-admin-password";

	// no datasource, scheduler or search config has this id: an authorized
	// call fails on the lookup instead of writing anything.
	private static final String UNKNOWN_ID = "999999";

	private static final Set<String> BODY_METHODS =
		Set.of("POST", "PUT", "PATCH");

	@ParameterizedTest
	@MethodSource("adminEndpoints")
	void should_reject_anonymous_call(WebEndpoints.Endpoint endpoint) {
		// no credentials: the policy must answer before the method runs
		var response = call(endpoint, RestAssured.given());

		Assertions.assertEquals(
			401, response.statusCode(),
			() -> endpoint + " is served without credentials");
	}

	@ParameterizedTest
	@MethodSource("adminEndpoints")
	void should_accept_admin_call(WebEndpoints.Endpoint endpoint) {
		// the same call with an identity holding k9-admin
		var response = call(endpoint, RestAssured.given()
			.auth().preemptive().basic(ADMIN_USER, ADMIN_PASSWORD));

		// the status depends on the payload, which is not the point here:
		// what matters is that authorization no longer blocks the call.
		Assertions.assertFalse(
			response.statusCode() == 401 || response.statusCode() == 403,
			() -> endpoint + " is blocked for an admin identity: "
				+ response.statusCode());
	}

	@Test
	void should_reject_anonymous_graphql_schema() {
		// the schema endpoint is a sub-path of graphql and serves the whole
		// admin API definition
		RestAssured.given()
			.get("/graphql/schema.graphql")
			.then()
			.statusCode(401);
	}

	@Test
	void should_keep_serving_the_public_bucket_endpoints() {
		// the search frontend calls this one anonymously
		var statusCode = RestAssured.given()
			.get("/buckets/current")
			.statusCode();

		Assertions.assertFalse(
			statusCode == 401 || statusCode == 403,
			() -> "/buckets/current is no longer public: " + statusCode);
	}

	private static List<WebEndpoints.Endpoint> adminEndpoints() {
		return WebEndpoints.adminEndpoints();
	}

	private static Response call(
		WebEndpoints.Endpoint endpoint, RequestSpecification request) {

		var path = endpoint.path().replaceAll("\\{[^}]+}", UNKNOWN_ID);

		// an empty JSON body keeps content negotiation from answering 415
		// before the authorization check on the resources that consume one.
		if (BODY_METHODS.contains(endpoint.httpMethod())) {
			request = request.contentType(ContentType.JSON).body("{}");
		}

		return request.request(endpoint.httpMethod(), path);
	}

	/**
	 * Replays the production authorization configuration: the policy is
	 * declared under {@code %prod}, and the test profile runs under
	 * {@code test}.
	 */
	public static class AdminPolicyProfile implements QuarkusTestProfile {

		@Override
		public Map<String, String> getConfigOverrides() {
			return Map.of(
				"quarkus.http.auth.basic", "true",
				"quarkus.http.auth.policy.administrators.roles-allowed",
				"k9-admin",
				"quarkus.http.auth.permission.administrator.paths",
				AdminPolicyPaths.joined(),
				"quarkus.http.auth.permission.administrator.policy",
				"administrators");
		}

	}

}
