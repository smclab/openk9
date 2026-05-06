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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class RealmRepresentationFactory {

	private RealmRepresentationFactory() {}

	/**
	 * Merges runtime data (virtual host, realm name, openk9 client URIs,
	 * admin user) into a hardened default realm representation produced by
	 * {@link io.openk9.tenantmanager.config.KeycloakDefaultRealmRepresentationFactory}.
	 *
	 * @param virtualHost                 the tenant virtual host, used for the
	 *                                    realm display name and the openk9
	 *                                    client redirect URIs.
	 * @param realmName                   the Keycloak realm identifier.
	 * @param defaultRealmRepresentation  the hardened realm template; must not
	 *                                    be {@code null}.
	 * @return the same {@code defaultRealmRepresentation} instance, mutated
	 *         in place with runtime values, an admin user, and (when missing)
	 *         the default k9 roles.
	 * @throws NullPointerException if {@code defaultRealmRepresentation} is
	 *         {@code null}.
	 */
	public static RealmRepresentation createRealmRepresentation(
		String virtualHost, String realmName,
		RealmRepresentation defaultRealmRepresentation) {

		Objects.requireNonNull(
			defaultRealmRepresentation,
			"defaultRealmRepresentation is null");

		defaultRealmRepresentation.setRealm(realmName);
		defaultRealmRepresentation.setEnabled(true);
		defaultRealmRepresentation.setDisplayName(virtualHost);

		List<ClientRepresentation> clients =
			defaultRealmRepresentation.getClients();

		if (clients == null) {
			clients = new ArrayList<>(1);
			defaultRealmRepresentation.setClients(clients);
		}

		clients
			.stream()
			.filter(c -> c.getClientId().equals("openk9"))
			.findFirst()
			.ifPresent(c -> {
				c.setWebOrigins(List.of("+"));
				c.setRedirectUris(
					List.of("https://" + virtualHost + "/*"));
			});

		RolesRepresentation roles =
			defaultRealmRepresentation.getRoles();

		if (!(roles != null
			&& (roles.getRealm() != null
				&& !roles.getRealm().isEmpty()))) {

			defaultRealmRepresentation.setRoles(
				createRolesRepresentation());
		}

		defaultRealmRepresentation.setUsers(
			List.of(
				createAdminUserRepresentation()
			)
		);

		return defaultRealmRepresentation;
	}

	private static UserRepresentation createAdminUserRepresentation() {
		UserRepresentation userRepresentation =
			new UserRepresentation();
		userRepresentation.setUsername("k9admin");
		userRepresentation.setFirstName("k9admin");
		userRepresentation.setLastName("k9admin");
		userRepresentation.setEmail("k9admin@openk9.io");
		userRepresentation.setRealmRoles(
			List.of("k9-admin", "k9-write", "k9-read")
		);
		userRepresentation.setEnabled(true);
		userRepresentation.setCredentials(
			List.of(
				createAdminCredentialRepresentation()
			)
		);
		userRepresentation.setRequiredActions(
			List.of("UPDATE_PASSWORD"));
		return userRepresentation;
	}

	private static final String PASSWORD_UPPER =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String PASSWORD_LOWER =
		"abcdefghijklmnopqrstuvwxyz";
	private static final String PASSWORD_DIGITS =
		"0123456789";
	private static final String PASSWORD_SPECIAL =
		"!@#$%^&*()-_=+{}[]|;:,.<>?";
	private static final String PASSWORD_ALL =
		PASSWORD_UPPER + PASSWORD_LOWER
			+ PASSWORD_DIGITS + PASSWORD_SPECIAL;
	private static final int PASSWORD_LENGTH = 12;
	private static final SecureRandom PASSWORD_RANDOM =
		new SecureRandom();

	private static CredentialRepresentation createAdminCredentialRepresentation() {
		CredentialRepresentation credentialRepresentation =
			new CredentialRepresentation();
		credentialRepresentation.setType(
			CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(generateAdminPassword());
		credentialRepresentation.setTemporary(false);
		return credentialRepresentation;
	}

	/**
	 * Generates a random password that satisfies the realm
	 * password policy (at least 1 upper, 1 lower, 1 digit,
	 * 1 special char, minimum 12 chars).
	 */
	static String generateAdminPassword() {
		char[] pwd = new char[PASSWORD_LENGTH];
		pwd[0] = PASSWORD_UPPER.charAt(
			PASSWORD_RANDOM.nextInt(PASSWORD_UPPER.length()));
		pwd[1] = PASSWORD_LOWER.charAt(
			PASSWORD_RANDOM.nextInt(PASSWORD_LOWER.length()));
		pwd[2] = PASSWORD_DIGITS.charAt(
			PASSWORD_RANDOM.nextInt(PASSWORD_DIGITS.length()));
		pwd[3] = PASSWORD_SPECIAL.charAt(
			PASSWORD_RANDOM.nextInt(PASSWORD_SPECIAL.length()));
		for (int i = 4; i < PASSWORD_LENGTH; i++) {
			pwd[i] = PASSWORD_ALL.charAt(
				PASSWORD_RANDOM.nextInt(PASSWORD_ALL.length()));
		}
		// Fisher-Yates shuffle
		for (int i = PASSWORD_LENGTH - 1; i > 0; i--) {
			int j = PASSWORD_RANDOM.nextInt(i + 1);
			char tmp = pwd[i];
			pwd[i] = pwd[j];
			pwd[j] = tmp;
		}
		return new String(pwd);
	}

	private static RolesRepresentation createRolesRepresentation() {
		RolesRepresentation rolesRepresentation =
			new RolesRepresentation();
		rolesRepresentation.setRealm(
			List.of(
				createRoleRepresentation(
					"k9-admin", "K9 Admin Role"),
				createRoleRepresentation(
					"k9-write", "K9 Write Role"),
				createRoleRepresentation(
					"k9-read", "K9 Read Role")
			)
		);
		return rolesRepresentation;
	}

	private static RoleRepresentation createRoleRepresentation(
		String name, String description) {

		RoleRepresentation k9admin = new RoleRepresentation();
		k9admin.setName(name);
		k9admin.setDescription(description);
		k9admin.setComposite(false);
		k9admin.setClientRole(false);
		return k9admin;
	}

}
