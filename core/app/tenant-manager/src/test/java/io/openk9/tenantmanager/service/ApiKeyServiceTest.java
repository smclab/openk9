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
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ApiKeyServiceTest {

	@Inject
	ApiKeyService apiKeyService;
	@Inject
	TenantDbService tenantDbService;
	@Inject
	OutboxEventService outboxService;

	@Test
	void should_create_and_delete_an_apiKey() {

		String tenantName = "shiny-pikachu";

		var tenantId = tenantDbService
			.findByTenantName(tenantName)
			.map(TenantResponseDTO::id)
			.await()
			.indefinitely();

		String name = "Search APIs";
		OffsetDateTime expirationDate = OffsetDateTime.now().plusMonths(6);

		CreateApiKeyRequest createApiKeyRequest =
			CreateApiKeyRequest.of(
				tenantName,
				name,
				ApiGroup.SEARCH,
				expirationDate
			);

		// 1. Create the API Key
		var apiKeyId = apiKeyService.create(createApiKeyRequest)
			.map(CreateApiKeyResponse::id)
			.await()
			.indefinitely();

		var createEvent = outboxService.lastEvents(1).await().indefinitely().getFirst();
		Assertions.assertEquals("ApiKeyCreated", createEvent.getEventType());

		// 2. Revoke and Delete the API Key
		apiKeyService.revoke(apiKeyId).await().indefinitely();
		apiKeyService.delete(apiKeyId).await().indefinitely();

		// revoke and delete publish the same event type,
		// so we expect two equals events in the outbox.
		outboxService.lastEvents(2).await().indefinitely()
			.forEach(event -> Assertions.assertEquals("ApiKeyRevoked", event.getEventType()));

		// 3. API Key Table must be empty
		var list = apiKeyService.findAllByTenantId(Long.parseLong(tenantId)).await().indefinitely();
		Assertions.assertTrue(list.isEmpty());

	}

}
