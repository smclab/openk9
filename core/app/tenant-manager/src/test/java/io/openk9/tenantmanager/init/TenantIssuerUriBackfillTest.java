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

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.service.TenantDbService;

@QuarkusTest
public class TenantIssuerUriBackfillTest {

	@Inject
	TenantDbService tenantDbService;

	@ConfigProperty(name = "openk9.tenant-manager.keycloak-base-issuer-uri")
	String baseIssuerUri;

	@Test
	void should_backfill_tenant_issuer_uri_column() {
		// 1. Fetch the pre-existing tenants (Charmander and Pikachu)
		// Ensure your DTO now includes the new 'issuerUri' field
		var charmender = tenantDbService.findById(1L).await().indefinitely();
		var pikachu = tenantDbService.findById(2L).await().indefinitely();

		Assertions.assertNotNull(charmender, "Tenant 1 should exist");
		Assertions.assertNotNull(pikachu, "Tenant 2 should exist");

		// 2. Verify Tenant 1 (Charmander)
		// We reconstruct what the URI *should* be based on the known seed data realm name.
		// If your seed data had realm_name="charmender", use that here.
		String expectedCharmenderUri = baseIssuerUri + "shiny-charmender";

		Assertions.assertNotNull(charmender.issuerUri(), "Issuer URI should not be null");
		Assertions.assertEquals(expectedCharmenderUri, charmender.issuerUri());

		// 3. Verify Tenant 2 (Pikachu)
		// If your seed data had realm_name="pikachu", use that here.
		String expectedPikachuUri = baseIssuerUri + "shiny-pikachu";

		Assertions.assertNotNull(pikachu.issuerUri(), "Issuer URI should not be null");
		Assertions.assertEquals(expectedPikachuUri, pikachu.issuerUri());
	}
}
