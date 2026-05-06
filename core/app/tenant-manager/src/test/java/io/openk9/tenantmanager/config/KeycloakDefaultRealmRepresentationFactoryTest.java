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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RealmRepresentation.BruteForceStrategy;
import org.keycloak.representations.idm.RolesRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link KeycloakDefaultRealmRepresentationFactory}.
 */
@DisplayName("KeycloakDefaultRealmRepresentationFactory")
class KeycloakDefaultRealmRepresentationFactoryTest {

	// ---------------------------------------------------------------
	// Brute force protection defaults
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Brute force protection defaults")
	class BruteForceDefaults {

		private RealmRepresentation hardenedRealm;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory();
			hardenedRealm = factory.getDefaultRealmRepresentation();
		}

		@Test
		@DisplayName("bruteForceProtected is true")
		void bruteForceProtected() {
			assertTrue(hardenedRealm.isBruteForceProtected());
		}

		@Test
		@DisplayName("failureFactor is 10")
		void failureFactor() {
			assertEquals(10, hardenedRealm.getFailureFactor().intValue());
		}

		@Test
		@DisplayName("maxTemporaryLockouts is 5")
		void maxTemporaryLockouts() {
			assertEquals(
				5, hardenedRealm.getMaxTemporaryLockouts().intValue());
		}

		@Test
		@DisplayName("permanentLockout is true")
		void permanentLockout() {
			assertTrue(hardenedRealm.isPermanentLockout());
		}

		@Test
		@DisplayName("bruteForceStrategy is MULTIPLE")
		void bruteForceStrategy() {
			assertEquals(
				BruteForceStrategy.MULTIPLE,
				hardenedRealm.getBruteForceStrategy());
		}
	}

	// ---------------------------------------------------------------
	// Password policy defaults
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Password policy defaults")
	class PasswordPolicyDefaults {

		private RealmRepresentation hardenedRealm;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory();
			hardenedRealm = factory.getDefaultRealmRepresentation();
		}

		@Test
		@DisplayName("passwordPolicy contains all expected tokens")
		void passwordPolicyTokens() {
			var policy = hardenedRealm.getPasswordPolicy();
			assertNotNull(policy);
			assertTrue(policy.contains("length(12)"));
			assertTrue(policy.contains("upperCase(1)"));
			assertTrue(policy.contains("lowerCase(1)"));
			assertTrue(policy.contains("digits(1)"));
			assertTrue(policy.contains("specialChars(1)"));
			assertTrue(policy.contains("notUsername(undefined)"));
			assertTrue(policy.contains("notEmail(undefined)"));
			assertTrue(policy.contains("passwordHistory(5)"));
			assertTrue(policy.contains(
				"forceExpiredPasswordChange(90)"));
			assertFalse(policy.contains("passwordBlacklist"));
		}
	}

	// ---------------------------------------------------------------
	// Events configuration
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Events configuration")
	class EventsConfigTest {

		private RealmRepresentation hardenedRealm;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory();
			hardenedRealm = factory.getDefaultRealmRepresentation();
		}

		@Test
		@DisplayName("eventsEnabled is true")
		void eventsEnabled() {
			assertTrue(hardenedRealm.isEventsEnabled());
		}

		@Test
		@DisplayName("eventsExpiration is 2592000 (30 days)")
		void eventsExpiration() {
			assertEquals(2592000, hardenedRealm.getEventsExpiration().intValue());
		}

		@Test
		@DisplayName("eventsListeners contains jboss-logging")
		void eventsListeners() {
			var listeners = hardenedRealm.getEventsListeners();
			assertNotNull(listeners);
			assertTrue(listeners.contains("jboss-logging"));
		}

		@Test
		@DisplayName("adminEventsEnabled is true")
		void adminEventsEnabled() {
			assertTrue(hardenedRealm.isAdminEventsEnabled());
		}

		@Test
		@DisplayName("adminEventsDetailsEnabled is true")
		void adminEventsDetailsEnabled() {
			assertTrue(hardenedRealm.isAdminEventsDetailsEnabled());
		}

		@Test
		@DisplayName("enabledEventTypes contains all expected types")
		void enabledEventTypes() {
			var types = hardenedRealm.getEnabledEventTypes();
			assertNotNull(types);
			assertTrue(types.contains("LOGIN"));
			assertTrue(types.contains("LOGIN_ERROR"));
			assertTrue(types.contains("LOGOUT"));
			assertTrue(types.contains("LOGOUT_ERROR"));
			assertTrue(types.contains("CODE_TO_TOKEN"));
			assertTrue(types.contains("CODE_TO_TOKEN_ERROR"));
			assertTrue(types.contains("REFRESH_TOKEN"));
			assertTrue(types.contains("REFRESH_TOKEN_ERROR"));
			assertTrue(types.contains("REGISTER"));
			assertTrue(types.contains("REGISTER_ERROR"));
			assertTrue(types.contains("UPDATE_PASSWORD"));
			assertTrue(types.contains("UPDATE_PASSWORD_ERROR"));
			assertTrue(types.contains("RESET_PASSWORD"));
			assertTrue(types.contains("RESET_PASSWORD_ERROR"));
			assertTrue(types.contains("SEND_RESET_PASSWORD"));
			assertTrue(types.contains("REMOVE_TOTP"));
			assertTrue(types.contains("UPDATE_TOTP"));
			assertTrue(types.contains(
				"USER_DISABLED_BY_PERMANENT_LOCKOUT"));
			assertTrue(types.contains(
				"USER_DISABLED_BY_TEMPORARY_LOCKOUT"));
			assertTrue(types.contains("PERMISSION_TOKEN"));
		}
	}

	// ---------------------------------------------------------------
	// Basic realm settings
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Basic realm settings")
	class BasicRealmSettings {

		private RealmRepresentation hardenedRealm;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory();
			hardenedRealm = factory.getDefaultRealmRepresentation();
		}

		@Test
		@DisplayName("sslRequired is external")
		void sslRequired() {
			assertEquals("external", hardenedRealm.getSslRequired());
		}

		@Test
		@DisplayName("registrationAllowed is false")
		void registrationAllowed() {
			assertFalse(hardenedRealm.isRegistrationAllowed());
		}

		@Test
		@DisplayName("loginWithEmailAllowed is true")
		void loginWithEmailAllowed() {
			assertTrue(hardenedRealm.isLoginWithEmailAllowed());
		}

		@Test
		@DisplayName("duplicateEmailsAllowed is false")
		void duplicateEmailsAllowed() {
			assertFalse(hardenedRealm.isDuplicateEmailsAllowed());
		}

		@Test
		@DisplayName("editUsernameAllowed is false")
		void editUsernameAllowed() {
			assertFalse(hardenedRealm.isEditUsernameAllowed());
		}
	}

	// ---------------------------------------------------------------
	// Browser security headers
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Browser security headers")
	class BrowserSecurityHeadersTest {

		private Map<String, String> headers;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory();
			headers = factory.getDefaultRealmRepresentation()
				.getBrowserSecurityHeaders();
		}

		@Test
		@DisplayName("all expected header keys are present")
		void headerKeysPresent() {
			assertNotNull(headers);
			assertTrue(headers.containsKey("xFrameOptions"));
			assertTrue(headers.containsKey("contentSecurityPolicy"));
			assertTrue(headers.containsKey("xContentTypeOptions"));
			assertTrue(headers.containsKey("strictTransportSecurity"));
			assertTrue(headers.containsKey("xRobotsTag"));
			assertTrue(headers.containsKey("xXSSProtection"));
			assertTrue(headers.containsKey("referrerPolicy"));
		}

		@Test
		@DisplayName("xFrameOptions is SAMEORIGIN")
		void xFrameOptions() {
			assertEquals("SAMEORIGIN", headers.get("xFrameOptions"));
		}

		@Test
		@DisplayName("strictTransportSecurity has includeSubDomains")
		void strictTransportSecurity() {
			var hsts = headers.get("strictTransportSecurity");
			assertNotNull(hsts);
			assertTrue(hsts.contains("includeSubDomains"));
		}
	}

	// ---------------------------------------------------------------
	// Roles and client
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Roles and client")
	class RolesAndClient {

		private RealmRepresentation hardenedRealm;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory();
			hardenedRealm = factory.getDefaultRealmRepresentation();
		}

		@Test
		@DisplayName("realm roles contain k9-admin, k9-write, k9-read")
		void realmRoles() {
			RolesRepresentation roles = hardenedRealm.getRoles();
			assertNotNull(roles);
			var realmRoles = roles.getRealm();
			assertNotNull(realmRoles);

			var roleMap = realmRoles.stream()
				.collect(Collectors.toMap(
					r -> r.getName(), r -> r));

			assertTrue(roleMap.containsKey("k9-admin"));
			assertEquals("K9 Admin Role", roleMap.get("k9-admin").getDescription());

			assertTrue(roleMap.containsKey("k9-write"));
			assertEquals("K9 Write Role", roleMap.get("k9-write").getDescription());

			assertTrue(roleMap.containsKey("k9-read"));
			assertEquals("K9 Read Role", roleMap.get("k9-read").getDescription());
		}

		@Test
		@DisplayName("client openk9 exists with expected attributes")
		void clientOpenk9() {
			var clients = hardenedRealm.getClients();
			assertNotNull(clients);

			var openk9 = clients.stream()
				.filter(c -> "openk9".equals(c.getClientId()))
				.findFirst()
				.orElse(null);
			assertNotNull(openk9);

			assertEquals("client-secret",
				openk9.getClientAuthenticatorType());
			assertTrue(openk9.isEnabled());
			assertTrue(openk9.isStandardFlowEnabled());
			assertTrue(openk9.isPublicClient());
			assertEquals("openid-connect", openk9.getProtocol());
			assertEquals(
				Map.of("login_theme", "openk9"),
				openk9.getAttributes());
			assertTrue(openk9.getDefaultClientScopes()
				.containsAll(List.of("profile", "email", "roles", "basic")));
		}

		@Test
		@DisplayName("realm is enabled")
		void realmEnabled() {
			assertTrue(hardenedRealm.isEnabled());
		}
	}

}
