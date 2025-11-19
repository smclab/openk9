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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.inject.Inject;

import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.OutboxEvent;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

@QuarkusTest
public class TenantServiceTest {

	@Inject
	TenantService tenantService;
	@InjectSpy(delegate = true)
	OutboxEventService outboxService;

	@Test
	@DisplayName("Tenant should be persisted and then deleted, with events persisted too.")
	void should_persist_and_then_delete_tenant_persisting_events() {

		TenantParameters parameters =
			generateTenantParameters("shiny-bulbasaur");

		// create tenant will be committed
		TenantResponseDTO tenantCreated = tenantService.persist(
				parameters.virtualHost(),
				parameters.schemaName(),
				parameters.liquibaseSchemaName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				OffsetDateTime.now(),
				OffsetDateTime.now()
			)
			.await()
			.indefinitely();

		// verify that creation is committed

		Assertions.assertNotNull(tenantCreated);

		// get the last event persisted
		var createEvent = outboxService.lastEvents(1).await().indefinitely().getFirst();

		// verify that the last event is of the right type
		var createEventType = createEvent.getEventType();
		Assertions.assertEquals("TenantCreated", createEventType);

		// delete tenant

		long tenantId = Long.parseLong(tenantCreated.id());

		tenantService.deleteTenant(tenantId)
			.await()
			.indefinitely();

		TenantResponseDTO deleted = tenantService.findById(tenantId)
			.await()
			.indefinitely();

		// verify that deletions is committed

		Assertions.assertNull(deleted);

		// verify that the last event is of the right type
		OutboxEvent deleteEvent = outboxService.lastEvents(1).await().indefinitely().getFirst();
		String deleteEventType = deleteEvent.getEventType();

		Assertions.assertEquals("TenantDeleted", deleteEventType);

	}

	@Test
	@DisplayName("Persist have to rollback if outboxService throws an error.")
	void should_rollback_persist_tx_on_outboxService_error() {
		// makes outboxService throw an error on persist
		doThrow(new RuntimeException())
			.when(outboxService)
			.persist(argThat(StubOnceMatcher.TENANT_CREATED_INSTANCE));

		int outboxUnsentSizeFirst
			= outboxService.unsentEvents().await().indefinitely().size();

		TenantParameters parameters = generateTenantParameters("shiny-squirtle");

		// create tenant will be rolled back
		TenantResponseDTO tenantCreated = tenantService.persist(
				parameters.virtualHost,
				parameters.schemaName(),
				parameters.liquibaseSchemaName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				OffsetDateTime.now(),
				OffsetDateTime.now()
			)
			.await()
			.indefinitely();

		int outboxUnsentSizeAfter
			= outboxService.unsentEvents().await().indefinitely().size();

		// verify that creation is rolled back
		Assertions.assertNull(tenantCreated);
		Assertions.assertEquals(outboxUnsentSizeFirst, outboxUnsentSizeAfter);
	}

	@Test
	@DisplayName("Delete have to rollback if outboxService throws an error.")
	void should_rollback_delete_tx_on_outboxService_error() {
		TenantParameters parameters = generateTenantParameters("shiny-eevee");

		// create a tenant
		TenantResponseDTO tenant = tenantService.persist(
				parameters.virtualHost(),
				parameters.schemaName(),
				parameters.liquibaseSchemaName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				OffsetDateTime.now(),
				OffsetDateTime.now()
			)
			.await()
			.indefinitely();

		long tenantId = Long.parseLong(tenant.id());

		int outboxUnsentSizeBefore =
			outboxService.unsentEvents().await().indefinitely().size();

		// makes outboxService throw an error on persist
		doThrow(new RuntimeException())
			.when(outboxService)
			.persist(argThat(StubOnceMatcher.TENANT_DELETED_INSTANCE));

		// deletes the tenant, this operation will be rolled back
		tenantService.deleteTenant(tenantId)
			.await()
			.indefinitely();

		int outboxUnsentSizeAfter =
			outboxService.unsentEvents().await().indefinitely().size();

		TenantResponseDTO stillThere = tenantService
			.findById(tenantId)
			.await()
			.indefinitely();

		// verify that deletion is rolled back
		Assertions.assertNotNull(stillThere);
		Assertions.assertEquals(outboxUnsentSizeBefore, outboxUnsentSizeAfter);

		// clean-up tenant

		tenantService.deleteTenant(tenantId)
			.await()
			.indefinitely();

		OutboxEvent deleteEvent = outboxService.lastEvents(1)
			.await()
			.indefinitely()
			.getFirst();

		String eventType = deleteEvent.getEventType();
		Assertions.assertEquals("TenantDeleted", eventType);

	}

	private static final String CLIENT_ID = "openk9";

	private static TenantParameters generateTenantParameters(String identifier) {

		return new TenantParameters(
			identifier + ".local",
			identifier,
			identifier + "-liquibase",
			identifier
		);

	}

	private record TenantParameters(
		String virtualHost,
		String schemaName,
		String liquibaseSchemaName,
		String realmName) {}


	/**
	 * In order to stub the call to {@link OutboxEventService#persist(TenantManagementEvent)}
	 * only once, we need to declare a custom {@link ArgumentMatcher} that can
	 * count the method's calls with that same argument.
	 */
	private static class StubOnceMatcher
		implements ArgumentMatcher<TenantManagementEvent> {

		private static final StubOnceMatcher TENANT_DELETED_INSTANCE =
			new StubOnceMatcher(TenantManagementEvent.TenantDeleted.class);
		private static final StubOnceMatcher TENANT_CREATED_INSTANCE =
			new StubOnceMatcher(TenantManagementEvent.TenantCreated.class);

		private final AtomicInteger callCount = new AtomicInteger(1);
		private final Class<? extends TenantManagementEvent> clazz;

		private StubOnceMatcher(Class<? extends TenantManagementEvent> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean matches(TenantManagementEvent event) {
			// if it's already called then use real method
			if (callCount.get() == 0) {
				return false;
			}

			// if it isn't a TenantCreated event then use the real method
			if (!event.getClass().isAssignableFrom(clazz)) {
				return false;
			}

			// otherwise decrement counter and use the stub
			callCount.decrementAndGet();
			return true;
		}
	}

}
