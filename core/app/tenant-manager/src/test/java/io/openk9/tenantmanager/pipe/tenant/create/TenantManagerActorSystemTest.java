package io.openk9.tenantmanager.pipe.tenant.create;

import jakarta.inject.Inject;

import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.service.dto.CreateTenantRequest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TenantManagerActorSystemTest {

	@Inject
    private TenantManagerActorSystem actorSystem;

    @Test
    void testStartCreateTenant_Success() {
        // Prepare Request
        SecurityConfiguration securityConfig = SecurityConfiguration.LEGACY;
        CreateTenantRequest request = new CreateTenantRequest(
                "vh", securityConfig, null, null, "testStartCreateTenantSuccess");

        // Execute
        TenantResponseDTO tenant = actorSystem.startCreateTenant(request)
			.await().indefinitely();

		Assertions.assertEquals("testStartCreateTenantSuccess", tenant.schemaName());
	}

    @Test
    void testStartCreateTenant_SkipOAuth2() {
        // Prepare Request
        SecurityConfiguration securityConfig = SecurityConfiguration.LEGACY;
        CreateTenantRequest request = new CreateTenantRequest(
                "vh-skip", securityConfig, null, true, "testSkipOAuth2");

        // Execute
        TenantResponseDTO tenant = actorSystem.startCreateTenant(request)
			.await().indefinitely();

		Assertions.assertEquals("testSkipOAuth2", tenant.schemaName());
        Assertions.assertEquals("DISABLED", tenant.issuerUri());
        Assertions.assertEquals("DISABLED", tenant.clientId());
	}
}
