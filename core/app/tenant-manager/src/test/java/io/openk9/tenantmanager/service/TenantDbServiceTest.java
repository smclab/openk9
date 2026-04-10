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
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.inject.Inject;

import io.openk9.event.tenant.ApiGroup;
import io.openk9.event.tenant.TenantEvent;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.OutboxEvent;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.service.dto.CreateApiKeyRequest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@QuarkusTest
public class TenantDbServiceTest {

	@Inject
	TenantDbService tenantDbService;
	@Inject
	ApiKeyService apiKeyService;
	@InjectSpy(delegate = true)
	OutboxEventService outboxService;

	@Test
	@DisplayName("Tenant should be fetched from database.")
	void should_fetch_existing_tenants() {

		// fetch all tenants
		List<TenantResponseDTO> tenants = tenantDbService.findAll()
			.await().indefinitely();

		// tenants must be equals or greater than 2 because there are
		// at least 2 tenants created from liquibase.
		Assertions.assertTrue(tenants.size() >= 2);
	}

	@Test
	@DisplayName(
		"Tenant should be persisted and then deleted, with "
		+ "events persisted too.")
	void should_persist_and_then_delete_tenant_persisting_events() {

		TenantParameters parameters =
			generateTenantParameters("shinybulbasaur");

		// create tenant will be committed
		TenantResponseDTO tenantCreated = tenantDbService.persist(
				parameters.virtualHost(),
				parameters.tenantName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				SecurityConfiguration.OAUTH2_ADMIN_ONLY,
				OffsetDateTime.now(),
				OffsetDateTime.now(),
				true
			)
			.await()
			.indefinitely();

		// verify that creation is committed
		Assertions.assertNotNull(tenantCreated);

		// get the last event persisted
		var createEvent = outboxService.lastEvents(1)
			.await().indefinitely().getFirst();

		// verify that the last event is of the right type
		var createEventType = createEvent.getEventType();
		Assertions.assertEquals(
			TenantEvent.TENANT_CREATED, createEventType);

		// delete tenant

		long tenantId = Long.parseLong(tenantCreated.id());

		tenantDbService.deleteTenant(tenantId)
			.await()
			.indefinitely();

		TenantResponseDTO deleted = tenantDbService
			.findById(tenantId)
			.await()
			.indefinitely();

		// verify that deletions is committed

		Assertions.assertNull(deleted);

		// verify that the last event is of the right type
		OutboxEvent deleteEvent = outboxService
			.lastEvents(1)
			.await()
			.indefinitely()
			.getFirst();
		String deleteEventType = deleteEvent.getEventType();

		Assertions.assertEquals(
			TenantEvent.TENANT_DELETED, deleteEventType);

	}

	@Test
	@DisplayName(
		"Tenant with API keys should be deleted, cascading "
		+ "API key removal via ON DELETE CASCADE.")
	void should_delete_tenant_with_api_keys() {

		TenantParameters parameters =
			generateTenantParameters("shinymew");

		// 1. Create tenant
		TenantResponseDTO tenant = tenantDbService.persist(
				parameters.virtualHost(),
				parameters.tenantName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				SecurityConfiguration.OAUTH2_ADMIN_ONLY,
				OffsetDateTime.now(),
				OffsetDateTime.now(),
				true
			)
			.await()
			.indefinitely();

		long tenantId = Long.parseLong(tenant.id());

		// 2. Create two API keys for this tenant
		apiKeyService.create(CreateApiKeyRequest.of(
				parameters.tenantName(),
				"Key 1",
				ApiGroup.SEARCH,
				OffsetDateTime.now().plusMonths(1)))
			.await().indefinitely();

		apiKeyService.create(CreateApiKeyRequest.of(
				parameters.tenantName(),
				"Key 2",
				ApiGroup.ADMINISTRATION,
				OffsetDateTime.now().plusMonths(1)))
			.await().indefinitely();

		// verify keys exist
		var keysBefore = apiKeyService
			.findAllByTenantId(tenantId)
			.await().indefinitely();
		Assertions.assertEquals(2, keysBefore.size());

		// 3. Delete tenant — must not throw FK violation
		tenantDbService.deleteTenant(tenantId)
			.await()
			.indefinitely();

		// 4. Verify tenant is gone
		TenantResponseDTO deleted = tenantDbService
			.findById(tenantId)
			.await()
			.indefinitely();
		Assertions.assertNull(deleted);

		// 5. Verify API keys are gone (cascaded)
		var keysAfter = apiKeyService
			.findAllByTenantId(tenantId)
			.await().indefinitely();
		Assertions.assertTrue(keysAfter.isEmpty());

		// 6. Verify TenantDeleted event was published
		OutboxEvent deleteEvent = outboxService
			.lastEvents(1)
			.await()
			.indefinitely()
			.getFirst();
		Assertions.assertEquals(
			TenantEvent.TENANT_DELETED,
			deleteEvent.getEventType());
	}

	@Test
	@DisplayName(
		"Persist have to rollback if outboxService throws an error.")
	void should_rollback_persist_tx_on_outboxService_error() {

		// makes outboxService throw an error on persist
		doThrow(new RuntimeException("outbox failure"))
			.when(outboxService)
			.persist(argThat(StubOnceMatcher.TENANT_CREATED_INSTANCE));

		int outboxUnsentSizeFirst
			= outboxService.unsentEvents()
				.await().indefinitely().size();

		TenantParameters parameters =
			generateTenantParameters("shinysquirtle");

		// create tenant must fail and propagate the error
		Assertions.assertThrows(RuntimeException.class, () ->
			tenantDbService.persist(
				parameters.virtualHost(),
				parameters.tenantName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				SecurityConfiguration.OAUTH2_ADMIN_ONLY,
				OffsetDateTime.now(),
				OffsetDateTime.now(),
				true
			)
			.await()
			.indefinitely()
		);

		int outboxUnsentSizeAfter
			= outboxService.unsentEvents()
				.await().indefinitely().size();

		// verify that creation is rolled back
		Assertions.assertEquals(
			outboxUnsentSizeFirst, outboxUnsentSizeAfter);
	}

	@Test
	@DisplayName(
		"Delete have to rollback if outboxService throws an error.")
	void should_rollback_delete_tx_on_outboxService_error() {

		TenantParameters parameters =
			generateTenantParameters("shinyeevee");

		// create a tenant
		TenantResponseDTO tenant = tenantDbService.persist(
				parameters.virtualHost(),
				parameters.tenantName(),
				parameters.realmName(),
				CLIENT_ID,
				null,
				SecurityConfiguration.OAUTH2_ADMIN_ONLY,
				OffsetDateTime.now(),
				OffsetDateTime.now(),
				true
			)
			.await()
			.indefinitely();

		long tenantId = Long.parseLong(tenant.id());

		int outboxUnsentSizeBefore =
			outboxService.unsentEvents()
				.await().indefinitely().size();

		// makes outboxService throw an error on persist
		doThrow(new RuntimeException("outbox failure"))
			.when(outboxService)
			.persist(argThat(StubOnceMatcher.TENANT_DELETED_INSTANCE));

		// deletes the tenant, must fail and propagate the error
		Assertions.assertThrows(RuntimeException.class, () ->
			tenantDbService.deleteTenant(tenantId)
				.await()
				.indefinitely()
		);

		int outboxUnsentSizeAfter =
			outboxService.unsentEvents()
				.await().indefinitely().size();

		TenantResponseDTO stillThere = tenantDbService
			.findById(tenantId)
			.await()
			.indefinitely();

		// verify that deletion is rolled back
		Assertions.assertNotNull(stillThere);
		Assertions.assertEquals(
			outboxUnsentSizeBefore, outboxUnsentSizeAfter);

		// clean-up tenant

		tenantDbService.deleteTenant(tenantId)
			.await()
			.indefinitely();

		OutboxEvent deleteEvent = outboxService.lastEvents(1)
			.await()
			.indefinitely()
			.getFirst();

		String eventType = deleteEvent.getEventType();
		Assertions.assertEquals(
			TenantEvent.TENANT_DELETED, eventType);

	}

	private static final String CLIENT_ID = "openk9";

	private static TenantParameters generateTenantParameters(
		String identifier) {

		return new TenantParameters(
			identifier + ".local",
			identifier,
			identifier
		);

	}

	private record TenantParameters(
		String virtualHost,
		String tenantName,
		String realmName) {}


	/**
	 * In order to stub the call to
	 * {@link OutboxEventService#persist(TenantEvent)} only once,
	 * we need to declare a custom {@link ArgumentMatcher} that
	 * can count the method's calls with that same argument.
	 */
	private static class StubOnceMatcher
		implements ArgumentMatcher<TenantEvent> {

		private static final StubOnceMatcher TENANT_DELETED_INSTANCE =
			new StubOnceMatcher(TenantEvent.TenantDeleted.class);
		private static final StubOnceMatcher TENANT_CREATED_INSTANCE =
			new StubOnceMatcher(TenantEvent.TenantCreated.class);

		private final AtomicInteger callCount =
			new AtomicInteger(1);
		private final Class<? extends TenantEvent> clazz;

		private StubOnceMatcher(
			Class<? extends TenantEvent> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean matches(TenantEvent event) {
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
