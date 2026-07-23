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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

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

		// 2. Post the exact exported body back in SKIP mode with dryRun=false, so
		// the apply path actually runs: re-importing a tenant into itself must
		// commit the plan (applied=true) yet create nothing and skip every match.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.queryParam("mode", "SKIP")
			.queryParam("dryRun", "false")
			.body(exported)
			.when()
			.post("/import")
			.then()
			.statusCode(200)
			.body("applied", equalTo(true))
			.body("created", equalTo(0))
			.body("skipped", greaterThan(0));
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void default_import_is_a_dry_run_preview() {
		// 1. Export the current tenant's configuration over HTTP
		String exported = given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.when()
			.get("/export")
			.then()
			.statusCode(200)
			.extract().asString();

		// 2. Post it back WITHOUT a dryRun param: the new default is preview, so
		// the report must plan the work yet write nothing (applied=false).
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(exported)
			.when()
			.post("/import")
			.then()
			.statusCode(200)
			.body("dryRun", equalTo(true))
			.body("applied", equalTo(false))
			.body("created", equalTo(0));
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
		// a schema version whose major does not match is refused with a speaking
		// 400 that names the offending version, before any entity is bound.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{\"schemaVersion\":\"0.9\"}")
			.when()
			.post("/import")
			.then()
			.statusCode(400)
			.body(containsString("0.9"))
			.body(containsString("schema version"));
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void import_with_newer_minor_is_rejected() {
		// forward compatibility is not assumed: a same-major but newer-minor
		// package (produced by a more recent runtime) is refused with a speaking 400.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{\"schemaVersion\":\"1.9\"}")
			.when()
			.post("/import")
			.then()
			.statusCode(400)
			.body(containsString("1.9"))
			.body(containsString("schema version"));
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void import_with_unknown_type_is_speaking_bad_request() {
		// a compatible version but an unknown entity type must still be a speaking
		// 400 that names the offending value, not the opaque empty 400 the raw enum
		// binding in ConfigEntityDeserializer would otherwise produce; the message
		// must NOT leak the internal enum's fully-qualified class name.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{\"schemaVersion\":\"1.0\",\"entities\":[{\"ref\":\"x\","
				+ "\"type\":\"PIPPO_TYPE\",\"key\":\"x\",\"attributes\":{\"name\":\"x\"}}]}")
			.when()
			.post("/import")
			.then()
			.statusCode(400)
			.body(containsString("PIPPO_TYPE"))
			.body(containsString("Malformed configuration package"))
			.body(not(containsString("io.openk9")));
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void export_with_unknown_type_is_bad_request() {
		// an unknown ConfigEntityType name must be a speaking 400, not the opaque
		// 404 the raw enum @QueryParam binding would otherwise produce.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.queryParam("types", "PIPPO")
			.when()
			.get("/export")
			.then()
			.statusCode(400)
			.body(containsString("PIPPO"))
			.body(containsString("valid values"));
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void import_with_unknown_mode_is_bad_request() {
		// an unknown import mode must be a speaking 400, not the opaque 404 the raw
		// enum @QueryParam binding would otherwise produce; the body is a valid,
		// compatible package so the only error surfaced is the bad mode.
		given()
			.header(InternalHeaders.TENANT_ID, TENANT_ID)
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.queryParam("mode", "PIPPO")
			.body("{\"schemaVersion\":\"1.0\",\"entities\":[]}")
			.when()
			.post("/import")
			.then()
			.statusCode(400)
			.body(containsString("PIPPO"))
			.body(containsString("valid values"));
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
