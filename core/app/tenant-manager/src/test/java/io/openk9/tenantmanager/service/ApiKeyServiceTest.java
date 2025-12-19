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

package io.openk9.tenantmanager.service;

import java.time.OffsetDateTime;
import java.util.List;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.model.Route;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ApiKeyServiceTest {

	@Inject
	ApiKeyService apiKeyService;

	@Test
	void should_generate_an_api_key() {

		String tenantId = "pikachu";
		String name = "Search APIs";
		List<Route> routes = List.of(
			Route.DATASOURCE_PUBLIC, Route.SEARCHER, Route.RAG);
		OffsetDateTime expirationDate = OffsetDateTime.now().plusMonths(6);

		CreateApiKeyRequest createApiKeyRequest =
			CreateApiKeyRequest.of(tenantId, name, routes, expirationDate);

		Uni<CreateApiKeyResponse> apiKey =
			apiKeyService.create(createApiKeyRequest);

	}

}
