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

package io.openk9.tenantmanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.openk9.tenantmanager.pipe.tenant.create.factory.RealmRepresentationFactory;

import io.quarkus.qute.Engine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ClientRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@code openk9} client of the realm produced by
 * {@link KeycloakDefaultRealmRepresentationFactory}.
 */
@DisplayName("KeycloakDefaultRealmRepresentationFactory")
class KeycloakDefaultRealmRepresentationFactoryTest {

	private static final String TEMPLATE =
		"templates/keycloakDefaultConfig";

	private ClientRepresentation openk9;

	@BeforeEach
	void setUp() throws IOException {
		// render the realm template as the Quarkus bean does
		var factory = new KeycloakDefaultRealmRepresentationFactory();
		factory.keycloakDefaultConfig = Engine.builder()
			.addDefaults()
			.build()
			.parse(readTemplate());

		// merge it with the tenant data, as the realm creation does
		var realm = RealmRepresentationFactory.createRealmRepresentation(
			"demo.openk9.localhost", "demo",
			factory.getDefaultRealmRepresentation());

		openk9 = realm.getClients().stream()
			.filter(c -> "openk9".equals(c.getClientId()))
			.findFirst()
			.orElseThrow();
	}

	@Test
	@DisplayName("only the standard flow of a public client is enabled")
	void onlyStandardFlow() {
		assertTrue(openk9.isPublicClient());
		assertTrue(openk9.isStandardFlowEnabled());
		assertEquals(Boolean.FALSE, openk9.isDirectAccessGrantsEnabled());
		assertEquals(Boolean.FALSE, openk9.isImplicitFlowEnabled());
		assertEquals(Boolean.FALSE, openk9.isServiceAccountsEnabled());
	}

	@Test
	@DisplayName("PKCE S256 is required")
	void pkceS256Required() {
		var attributes = openk9.getAttributes();

		assertNotNull(attributes);
		assertEquals("S256", attributes.get("pkce.code.challenge.method"));
		assertEquals("openk9", attributes.get("login_theme"));
	}

	private static String readTemplate() throws IOException {
		try (InputStream in = Thread.currentThread()
			.getContextClassLoader()
			.getResourceAsStream(TEMPLATE)) {

			assertNotNull(in, TEMPLATE + " not found");

			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}
