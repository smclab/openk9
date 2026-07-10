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

import io.openk9.common.util.web.InternalHeaders;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Exercises the import/export HTTP contract end-to-end against the booted
 * {@code public} tenant (populated by {@code createDefault}). It exports the
 * tenant over HTTP and posts the exact response body back, proving the whole
 * wire path — including the JSON (de)serialization of the polymorphic
 * {@code attributes} and the {@code mode} query param — round-trips and, against
 * the same tenant, is a no-op.
 */
@QuarkusTest
@TestHTTPEndpoint(ConfigResource.class)
public class ConfigResourceTest {

	private static final String TENANT_ID = "public";

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void round_trip_over_http_is_a_no_op() {
		// 1. Export the current tenant's configuration over HTTP
		String exported = given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.when()
			.get("/export")
			.then()
			.statusCode(200)
			.extract().asString();

		// 2. Post the exact exported body back in SKIP mode: re-importing a
		// tenant into itself must create nothing and skip every matched entity.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.queryParam("mode", "SKIP")
			.body(exported)
			.when()
			.post("/import")
			.then()
			.statusCode(200)
			.body("created", equalTo(0))
			.body("skipped", greaterThan(0));
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void export_without_tenant_header_is_bad_request() {
		// authenticated but no X-TENANT-ID: the tenant cannot be resolved,
		// so the request must fail cleanly with 400, not a 500 NPE.
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/export")
			.then()
			.statusCode(400);
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void import_with_empty_body_is_bad_request() {
		// an empty object deserializes to a package with no schema version and no
		// entities: the endpoint must reject it with a speaking 400 before opening
		// the transaction, not blow up with a 500 NPE while sorting a null list.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{}")
			.when()
			.post("/import")
			.then()
			.statusCode(400);
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void import_with_unsupported_schema_version_is_bad_request() {
		// a schema version the importer does not understand must be refused with
		// 400, regardless of the rest of the package.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{\"schemaVersion\":\"0.9\"}")
			.when()
			.post("/import")
			.then()
			.statusCode(400);
	}

	@Test
	void export_without_admin_role_is_rejected() {
		// no @TestSecurity: an unauthenticated caller must not reach the endpoint
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.when()
			.get("/export")
			.then()
			.statusCode(anyOf(equalTo(401), equalTo(403)));
	}

}
