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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import io.openk9.datasource.model.dto.response.SearchPipelineResponseDTO;
import io.openk9.datasource.service.SearchConfigService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

@QuarkusTest
@TestHTTPEndpoint(SearchConfigResource.class)
public class SearchConfigResourceTest {

	@InjectMock
	SearchConfigService service;

	@Test
	void should_reject_anonymous_configure_hybrid_search() {
		// anonymous request is rejected
		given()
			.contentType(ContentType.JSON)
			.body("{}")
			.post("/1/configure-hybrid-search")
			.then()
			.statusCode(401);

		// the pipeline is never written
		BDDMockito.then(service).shouldHaveNoInteractions();
	}

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void should_configure_hybrid_search_as_admin() {
		BDDMockito.given(service.configureHybridSearch(eq(1L), any()))
			.willReturn(Uni.createFrom().item(
				new SearchPipelineResponseDTO(200, "{}", null)));

		// admin request is served
		given()
			.contentType(ContentType.JSON)
			.body("{}")
			.post("/1/configure-hybrid-search")
			.then()
			.statusCode(200);

		// the pipeline is written
		BDDMockito.then(service).should()
			.configureHybridSearch(eq(1L), any());
	}

}
