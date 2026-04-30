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

package io.openk9.datasource.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TenantIdResolverTest {

	private final TenantIdResolver resolver = new TenantIdResolver();

	@BeforeEach
	void setUp() {
		resolver.tenantRegistry = mock(TenantRegistry.class);
	}

	@Test
	void shortCircuitsWhenTenantIdSet() {
		String result = resolver.resolve("public", "anything")
			.await().indefinitely();

		assertEquals("public", result);
		verifyNoInteractions(resolver.tenantRegistry);
	}

	@Test
	void usesRegistryFallbackWhenOnlyVirtualHostSet() {
		when(resolver.tenantRegistry.getTenantId("vh"))
			.thenReturn(Uni.createFrom().item("tenantA"));

		String result = resolver.resolve(null, "vh")
			.await().indefinitely();

		assertEquals("tenantA", result);
	}

	@Test
	void usesRegistryFallbackWhenTenantIdBlank() {
		when(resolver.tenantRegistry.getTenantId("vh"))
			.thenReturn(Uni.createFrom().item("tenantA"));

		String result = resolver.resolve("   ", "vh")
			.await().indefinitely();

		assertEquals("tenantA", result);
	}

	@Test
	void failsInvalidArgumentWhenBothBlank() {
		StatusRuntimeException thrown = assertThrows(
			StatusRuntimeException.class,
			() -> resolver.resolve(null, "").await().indefinitely()
		);

		assertEquals(Status.Code.INVALID_ARGUMENT, thrown.getStatus().getCode());
		verifyNoInteractions(resolver.tenantRegistry);
	}

	@Test
	void failsNotFoundWhenRegistryReturnsBlank() {
		when(resolver.tenantRegistry.getTenantId("unknown.host"))
			.thenReturn(Uni.createFrom().item(""));

		StatusRuntimeException thrown = assertThrows(
			StatusRuntimeException.class,
			() -> resolver.resolve(null, "unknown.host").await().indefinitely()
		);

		assertEquals(Status.Code.NOT_FOUND, thrown.getStatus().getCode());
	}

	@Test
	void failsNotFoundWhenRegistryReturnsNull() {
		when(resolver.tenantRegistry.getTenantId("unknown.host"))
			.thenReturn(Uni.createFrom().nullItem());

		StatusRuntimeException thrown = assertThrows(
			StatusRuntimeException.class,
			() -> resolver.resolve(null, "unknown.host").await().indefinitely()
		);

		assertEquals(Status.Code.NOT_FOUND, thrown.getStatus().getCode());
	}
}
