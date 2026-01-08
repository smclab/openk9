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
import jakarta.inject.Inject;

import io.openk9.event.tenant.ApiGroup;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ApiKeyServiceTest {

	@Inject
	ApiKeyService apiKeyService;

	@Test
	void should_generate_an_api_key() {

		String tenantId = "pikachu";
		String name = "Search APIs";
		OffsetDateTime expirationDate = OffsetDateTime.now().plusMonths(6);

		CreateApiKeyRequest createApiKeyRequest =
			CreateApiKeyRequest.of(
				tenantId,
				name,
				ApiGroup.SEARCH,
				expirationDate
			);

		apiKeyService.create(createApiKeyRequest)
			.await().indefinitely();

	}

}
