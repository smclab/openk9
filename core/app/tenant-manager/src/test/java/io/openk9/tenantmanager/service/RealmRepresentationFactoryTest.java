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

import java.util.List;

import io.openk9.tenantmanager.config.KeycloakDefaultRealmRepresentationFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RealmRepresentationFactory}.
 */
@DisplayName("RealmRepresentationFactory")
class RealmRepresentationFactoryTest {

	@RepeatedTest(200)
	@DisplayName(
		"generated password satisfies length(12), upper, lower, digit, special")
	void generatedPasswordSatisfiesPolicy() {
		// 1. Generate a password
		String pwd = RealmRepresentationFactory.generateAdminPassword();

		// 2. Verify all policy constraints
		assertTrue(
			pwd.length() >= 12,
			"password must be at least 12 characters");
		assertTrue(
			pwd.chars().anyMatch(Character::isUpperCase),
			"password must contain at least one uppercase letter");
		assertTrue(
			pwd.chars().anyMatch(Character::isLowerCase),
			"password must contain at least one lowercase letter");
		assertTrue(
			pwd.chars().anyMatch(Character::isDigit),
			"password must contain at least one digit");
		assertTrue(
			pwd.chars().anyMatch(
				c -> "!@#$%^&*()-_=+{}[]|;:,.<>?".indexOf(c) >= 0),
			"password must contain at least one special character");
	}

	@Test
	@DisplayName(
		"default client scopes survive realm representation merge")
	void clientScopesPreservedAfterMerge() {
		// 1. Create hardened default and merge with runtime info
		var hardened =
			new KeycloakDefaultRealmRepresentationFactory()
				.getDefaultRealmRepresentation();
		var merged = RealmRepresentationFactory
			.createRealmRepresentation(
				"demo.openk9.localhost", "demo", hardened);

		// 2. Verify client openk9 properties
		var openk9 = merged.getClients().stream()
			.filter(c -> "openk9".equals(c.getClientId()))
			.findFirst()
			.orElseThrow();

		assertTrue(
			openk9.getDefaultClientScopes()
				.containsAll(
					List.of("profile", "email", "roles", "basic")),
			"defaultClientScopes must contain profile, email, roles, basic");
		assertEquals(
			List.of("+"), openk9.getWebOrigins());
		assertEquals(
			List.of("https://demo.openk9.localhost/*"),
			openk9.getRedirectUris());
	}

}
