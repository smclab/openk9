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
        SecurityConfiguration securityConfig =
			SecurityConfiguration.OAUTH2_ADMIN_ONLY;
        CreateTenantRequest request = new CreateTenantRequest(
			"vh", securityConfig, "testcreatetenant");

        TenantResponseDTO tenant = actorSystem
			.startCreateTenant(request)
			.await().indefinitely();

		Assertions.assertEquals(
			"testcreatetenant",
			tenant.tenantName());
	}

    @Test
    void testStartCreateTenant_BasicAuth() {
        SecurityConfiguration securityConfig =
			SecurityConfiguration.NO_GATEWAY_AUTH;
        CreateTenantRequest request = new CreateTenantRequest(
			"vh_basic", securityConfig, "testbasicauth");

        TenantResponseDTO tenant = actorSystem
			.startCreateTenant(request)
			.await().indefinitely();

		Assertions.assertEquals(
			"testbasicauth", tenant.tenantName());
		Assertions.assertNull(tenant.issuerUri());
		Assertions.assertNull(tenant.clientId());
	}
}
