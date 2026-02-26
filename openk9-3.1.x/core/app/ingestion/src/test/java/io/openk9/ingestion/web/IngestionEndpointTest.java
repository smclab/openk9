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

package io.openk9.ingestion.web;

import io.openk9.ingestion.client.filemanager.FileManagerClient;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@QuarkusTest
@TestHTTPEndpoint(IngestionEndpoint.class)
class IngestionEndpointTest {

	@InjectMock
	@RestClient
	FileManagerClient fileManagerClient;

	@InjectSpy
	IngestionEmitter ingestionEmitter;

	@Test
	void should_ingest_a_payload() {

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(IngestionDTO.builder()
				.datasourcePayload(Map.of())
				.build())
			.post()
			.then()
			//a 406 is expected because no queue exists with name "null#null"
			.statusCode(406);

		then(fileManagerClient).shouldHaveNoInteractions();

		then(ingestionEmitter)
			.should(times(1))
			.emit(any(IngestionDTO.class));
	}

	@Test
	void should_ingest_a_binary_payload() {

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(IngestionDTO.builder()
				.datasourceId(100L)
				.tenantId("mew")
				.datasourcePayload(Map.of("title", "Lorem ipsum"))
				.resources(ResourcesDTO.builder()
					.binaries(List.of(BinaryDTO.builder()
						.data(
							Base64.getEncoder().encode(
								"""
									Lorem ipsum dolor sit amet, consectetur adipiscing elit,
									sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
									Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris
									nisi ut aliquip ex ea commodo consequat.
									Duis aute irure dolor in reprehenderit in voluptate velit esse
									cillum dolore eu fugiat nulla pariatur.
									Excepteur sint occaecat cupidatat non proident,
									sunt in culpa qui officia deserunt mollit anim id est laborum.
									""".getBytes()
							)
						)
						.contentType("text/plain")
						.id("1102394")
						.build())

					)
					.build()
				)
				.build()
			)
			.post()
			.then()
			//a 406 is expected because no queue exists with name "mew#null"
			.statusCode(406);

		then(fileManagerClient).should(times(1))
			.upload(any(), any(), any(), any());

		then(ingestionEmitter).should(times(1))
			.emit(any(IngestionDTO.class));
	}

}