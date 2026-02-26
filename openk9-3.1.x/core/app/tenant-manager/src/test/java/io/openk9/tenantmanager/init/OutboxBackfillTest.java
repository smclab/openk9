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

import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.OutboxEvent;
import io.openk9.tenantmanager.service.OutboxEventService;
import io.openk9.tenantmanager.service.TenantService;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class OutboxBackfillTest {

	@Inject
	TenantService tenantService;
	@Inject
	OutboxEventService outboxEventService;
	@ConfigProperty(name = "openk9.tenant-manager.keycloak-base-issuer-uri")
	String baseIssuerUri;

	@Test
	void should_backfill_outbox_table() {

		// fetch pre-existing tenants
		TenantResponseDTO charmender = tenantService.findById(1L).await().indefinitely();
		TenantResponseDTO pikachu = tenantService.findById(2L).await().indefinitely();

		Assertions.assertNotNull(charmender);
		Assertions.assertNotNull(pikachu);

		List<OutboxEvent> backfilledEvents = outboxEventService.lastEvents(2).await().indefinitely();

		Assertions.assertEquals(2, backfilledEvents.size());

		// verify that issuer uris are correct
		for (OutboxEvent event : backfilledEvents) {
			var tenantCreatedEvent = Json.decodeValue(
				event.getPayload(),
				TenantManagementEvent.TenantCreated.class
			);

			String tenantId  = tenantCreatedEvent.tenantId();
			String issuerUri = tenantCreatedEvent.issuerUri();

			Assertions.assertEquals(baseIssuerUri + tenantId, issuerUri);
		}

	}

}
