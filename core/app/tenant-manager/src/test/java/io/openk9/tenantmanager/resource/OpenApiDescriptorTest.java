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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OpenApiDescriptorTest {

	@Test
	void exposesExpectedOperationIdsAndTags() {

		given()
			.header(
				Constants.AUTHORIZATION_HEADER,
				Constants.BASIC_CREDENTIALS)
			.get("/q/openapi")
			.then()
			.statusCode(200)
			.body(allOf(
				containsString("operationId: initTenant"),
				containsString("operationId: createConnector"),
				containsString("operationId: createTenant"),
				containsString("operationId: createTables"),
				containsString("operationId: requestDeleteTenant"),
				containsString("operationId: deleteTenant"),
				containsString("- Tenant Provisioning"),
				containsString("- Tenant Management")
			));
	}

}
