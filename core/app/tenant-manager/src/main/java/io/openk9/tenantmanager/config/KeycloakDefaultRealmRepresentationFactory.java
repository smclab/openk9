package io.openk9.tenantmanager.config;

import io.quarkus.qute.Template;
import io.vertx.core.json.Json;
import org.keycloak.representations.idm.RealmRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KeycloakDefaultRealmRepresentationFactory {

	public RealmRepresentation getDefaultRealmRepresentation() {
		return Json.decodeValue(
			keycloakDefaultConfig.render(), RealmRepresentation.class);
	}

	@Inject
	Template keycloakDefaultConfig;

}
