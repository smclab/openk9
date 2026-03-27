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
import io.openk9.event.tenant.TenantEvent;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;
import io.openk9.tenantmanager.service.dto.CreateApiKeyResponse;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class ApiKeyServiceTest {

	private static final String TENANT_NAME = "shinypikachu";

	@Inject
	ApiKeyService apiKeyService;
	@Inject
	TenantDbService tenantDbService;
	@Inject
	OutboxEventService outboxService;

	@Test
	void should_create_and_delete_an_apiKey() {

		var tenantId = tenantDbService
			.findByTenantName(TENANT_NAME)
			.map(TenantResponseDTO::id)
			.await()
			.indefinitely();

		String name = "Search APIs";
		OffsetDateTime expirationDate =
			OffsetDateTime.now().plusMonths(6);

		CreateApiKeyRequest createApiKeyRequest =
			CreateApiKeyRequest.of(
				TENANT_NAME,
				name,
				ApiGroup.SEARCH,
				expirationDate
			);

		// 1. Create the API Key
		var apiKeyId = apiKeyService.create(createApiKeyRequest)
			.map(CreateApiKeyResponse::id)
			.await()
			.indefinitely();

		var createEvent = outboxService.lastEvents(1)
			.await().indefinitely().getFirst();
		Assertions.assertEquals(
			TenantEvent.API_KEY_CREATED,
			createEvent.getEventType());

		// 2. Revoke and Delete the API Key
		apiKeyService.revoke(apiKeyId)
			.await().indefinitely();
		apiKeyService.delete(apiKeyId)
			.await().indefinitely();

		// revoke and delete publish the same event type,
		// so we expect two equal events in the outbox.
		outboxService.lastEvents(2)
			.await().indefinitely()
			.forEach(event -> Assertions.assertEquals(
				TenantEvent.API_KEY_REVOKED,
				event.getEventType()));

		// 3. API Key Table must be empty
		var list = apiKeyService
			.findAllByTenantId(Long.parseLong(tenantId))
			.await().indefinitely();
		Assertions.assertTrue(list.isEmpty());
	}

	@Test
	@DisplayName(
		"full lifecycle exercises all SQL queries")
	void fullLifecycle() {
		// resolve the seeded tenant
		var tenantId = tenantDbService
			.findByTenantName(TENANT_NAME)
			.map(TenantResponseDTO::id)
			.await().indefinitely();
		assertNotNull(tenantId, "Seeded tenant must exist");

		// 1. INSERT_SQL — create an API key
		var request = CreateApiKeyRequest.of(
			TENANT_NAME,
			"Integration test key",
			ApiGroup.SEARCH,
			OffsetDateTime.now().plusMonths(1));

		var createResponse = apiKeyService.create(request)
			.await().indefinitely();
		assertNotNull(createResponse);

		var apiKeyId = createResponse.id();
		assertNotNull(apiKeyId);

		// 2. FETCH_BY_ID_SQL — find by id
		var byId = apiKeyService
			.findById(Long.parseLong(apiKeyId))
			.await().indefinitely();
		assertNotNull(byId, "findById should return the key");
		assertEquals(apiKeyId, byId.id());
		assertEquals("ACTIVE", byId.status());

		// 3. FETCH_ALL_BY_TENANT_ID_SQL — find all by tenant
		var all = apiKeyService
			.findAllByTenantId(Long.parseLong(tenantId))
			.await().indefinitely();
		assertFalse(all.isEmpty(),
			"findAllByTenantId should return at least one key");

		// 4. REVOKE_SQL + FETCH_TENANT_NAME_AND_HASH_BY_ID_SQL
		var revoked = apiKeyService.revoke(apiKeyId)
			.await().indefinitely();
		assertTrue(revoked, "revoke should return true");

		var afterRevoke = apiKeyService
			.findById(Long.parseLong(apiKeyId))
			.await().indefinitely();
		assertEquals("REVOKED", afterRevoke.status());

		// 5. DELETE_SQL + FETCH_TENANT_NAME_AND_HASH_BY_ID_SQL
		var deleted = apiKeyService.delete(apiKeyId)
			.await().indefinitely();
		assertTrue(deleted, "delete should return true");

		// 6. Verify the key is gone
		var afterDelete = apiKeyService
			.findById(Long.parseLong(apiKeyId))
			.await().indefinitely();
		assertNull(afterDelete,
			"findById should return null after deletion");
	}

}
