package io.openk9.tenantmanager.config;

import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import lombok.Getter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Getter
public class KeycloakContext {

	@Inject
	KeycloakDefaultRealmRepresentationFactory keycloakDefaultRealmRepresentationFactory;

	@Inject
	KeycloakAdminClientConfig keycloakAdminClientConfig;

}
