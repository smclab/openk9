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

package io.openk9.datasource.pipeline.resource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * The Quarkus test profile mocks the actor initializers, so the cluster
 * sharding is not started: only the token validation is checked here. The
 * 202/404 answers are covered by {@code EnrichPipelineCallbackTest} on the
 * entity behavior and by the end-to-end scenario on the running stack.
 */
@QuarkusTest
@TestHTTPEndpoint(PipelineResource.class)
class PipelineResourceTest {

	@Test
	void should_answer_400_on_a_malformed_token() {
		// a value that is not a callback token
		given()
			.contentType(ContentType.JSON)
			.body("{}")
			.post("/callback/not-a-token")
			.then()
			.statusCode(400);
	}

}
