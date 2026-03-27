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

package io.openk9.tenantmanager.init;

import java.util.List;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.OutboxEvent;
import io.openk9.tenantmanager.service.OutboxEventService;
import io.openk9.tenantmanager.service.TenantDbService;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that {@link BackfillTenantCreatedEventsTask} correctly
 * generates TenantCreated outbox events from pre-existing tenant
 * rows.
 * <p>
 * The backfill task produces v0 event payloads (with
 * {@code schemaName} and legacy {@code routeAuthorizationMap}
 * keys). This test deserializes the raw JSON rather than mapping
 * to {@code TenantEvent.TenantCreated} to avoid coupling to the
 * current v1 schema.
 */
@QuarkusTest
public class OutboxBackfillTest {

	@Inject
	TenantDbService tenantDbService;
	@Inject
	OutboxEventService outboxEventService;
	@ConfigProperty(
		name = "openk9.tenant-manager.keycloak-base-issuer-uri")
	String baseIssuerUri;

	@Test
	void should_backfill_outbox_table() {

		// fetch pre-existing tenants
		TenantResponseDTO charmender =
			tenantDbService.findById(1L)
				.await().indefinitely();
		TenantResponseDTO pikachu =
			tenantDbService.findById(2L)
				.await().indefinitely();

		assertNotNull(charmender);
		assertNotNull(pikachu);

		List<OutboxEvent> backfilledEvents = outboxEventService
			.lastEvents(2).await().indefinitely();

		assertEquals(2, backfilledEvents.size());

		// verify that issuer uris are correct
		// (v0 payload uses schemaName as tenantId)
		for (OutboxEvent event : backfilledEvents) {
			JsonObject payload =
				new JsonObject(event.getPayload());

			String tenantId = payload.getString("tenantId");
			String issuerUri = payload.getString("issuerUri");

			assertNotNull(tenantId);
			assertEquals(
				baseIssuerUri + tenantId, issuerUri);
		}
	}

}
