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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RealmRepresentation.BruteForceStrategy;
import org.keycloak.representations.idm.RolesRepresentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link KeycloakDefaultRealmRepresentationFactory}.
 */
@DisplayName("KeycloakDefaultRealmRepresentationFactory")
class KeycloakDefaultRealmRepresentationFactoryTest {

	private static final ObjectMapper mapper = JsonMapper.builder()
		.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
		.enable(SerializationFeature.INDENT_OUTPUT)
		.build();

	private RealmRepresentation realm;

	@BeforeEach
	void setUp() {
		var factory = new KeycloakDefaultRealmRepresentationFactory(
			new DefaultConfig(), true);
		realm = factory.getDefaultRealmRepresentation();
	}

	// ---------------------------------------------------------------
	// Legacy profile equivalence
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Legacy profile equivalence")
	class LegacyEquivalence {

		private RealmRepresentation legacyRealm;

		@BeforeEach
		void loadLegacyBaseline() throws IOException {
			try (InputStream is = getClass().getClassLoader()
				.getResourceAsStream("legacy-realm-baseline.json")) {
				assertNotNull(is,
					"legacy-realm-baseline.json not found on classpath");
				legacyRealm = mapper.readValue(
					is, RealmRepresentation.class);
			}
		}

		@Test
		@DisplayName("enabled field matches legacy baseline")
		void enabledMatches() {
			assertEquals(legacyRealm.isEnabled(), realm.isEnabled());
		}

		@Test
		@DisplayName("roles match legacy baseline")
		void rolesMatch() {
			var legacyRoles = legacyRealm.getRoles();
			var newRoles = realm.getRoles();

			assertNotNull(legacyRoles);
			assertNotNull(newRoles);

			var legacyRoleNames = legacyRoles.getRealm().stream()
				.map(r -> r.getName() + ":" + r.getDescription())
				.sorted()
				.toList();
			var newRoleNames = newRoles.getRealm().stream()
				.map(r -> r.getName() + ":" + r.getDescription())
				.sorted()
				.toList();

			assertEquals(legacyRoleNames, newRoleNames);
		}

		@Test
		@DisplayName("client openk9 matches legacy baseline")
		void clientMatches() {
			var legacyClient = findOpenk9Client(legacyRealm);
			var newClient = findOpenk9Client(realm);

			assertNotNull(legacyClient);
			assertNotNull(newClient);

			assertEquals(
				legacyClient.getClientId(), 
				newClient.getClientId());
			assertEquals(
				legacyClient.isEnabled(), 
				newClient.isEnabled());
			assertEquals(
				legacyClient.getClientAuthenticatorType(),
				newClient.getClientAuthenticatorType());
			assertEquals(
				legacyClient.isStandardFlowEnabled(),
				newClient.isStandardFlowEnabled());
			assertEquals(
				legacyClient.isPublicClient(), 
				newClient.isPublicClient());
			assertEquals(
				legacyClient.getProtocol(), 
				newClient.getProtocol());
		}

		@Test
		@DisplayName(
			"new hardening fields are null in legacy-equivalent config")
		void newFieldsAreNull() {
			// With DefaultConfig (all features off), these
			// remain null like the legacy template
			assertNull(realm.getPasswordPolicy());
			assertNull(realm.isBruteForceProtected());
			assertNull(realm.getFailureFactor());
			assertNull(realm.isEventsEnabled());
			assertNull(realm.getSmtpServer());
		}

		@Test
		@DisplayName(
			"best-practice additive fields are always set")
		void bestPracticeFieldsAreSet() {
			assertEquals("external", realm.getSslRequired());
			assertFalse(realm.isRegistrationAllowed());
			assertFalse(realm.isDuplicateEmailsAllowed());
			assertFalse(realm.isEditUsernameAllowed());
			assertTrue(realm.isLoginWithEmailAllowed());
			assertNotNull(realm.getBrowserSecurityHeaders());
		}

		private ClientRepresentation findOpenk9Client(
			RealmRepresentation r) {

			if (r.getClients() == null) {
				return null;
			}
			return r.getClients().stream()
				.filter(c -> "openk9".equals(c.getClientId()))
				.findFirst()
				.orElse(null);
		}
	}

	// ---------------------------------------------------------------
	// Brute force protection defaults
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Brute force protection defaults")
	class BruteForceDefaults {

		private RealmRepresentation hardenedRealm;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
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
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
			hardenedRealm = factory.getDefaultRealmRepresentation();
		}

		@Test
		@DisplayName("passwordPolicy contains all expected tokens")
		void passwordPolicyTokens() {
			var policy = hardenedRealm.getPasswordPolicy();
			assertNotNull(policy);
			assertTrue(policy.contains("length(8)"));
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
	// Password blacklist absent / present
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("Password blacklist")
	class PasswordBlacklist {

		@Test
		@DisplayName("blacklist absent: policy does not contain passwordBlacklist")
		void blacklistAbsent() {
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
			var policy = factory.getDefaultRealmRepresentation()
				.getPasswordPolicy();
			assertNotNull(policy);
			assertFalse(policy.contains("passwordBlacklist"));
		}

		@Test
		@DisplayName("blacklist present: policy contains passwordBlacklist(file)")
		void blacklistPresent() {
			var config = new HardenedConfig() {
				@Override
				public PasswordPolicyConfig passwordPolicy() {
					return new PasswordPolicyConfig() {
						@Override public int length() { return 8; }
						@Override public int upperCase() { return 1; }
						@Override public int lowerCase() { return 1; }
						@Override public int digits() { return 1; }
						@Override public int specialChars() { return 1; }
						@Override public int history() { return 5; }
						@Override public int expirationDays() { return 90; }
						@Override
						public Optional<String> blacklist() {
							return Optional.of("rockyou.txt");
						}
					};
				}
			};
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				config, true);
			var policy = factory.getDefaultRealmRepresentation()
				.getPasswordPolicy();
			assertNotNull(policy);
			assertTrue(
				policy.contains("passwordBlacklist(rockyou.txt)"));
		}
	}

	// ---------------------------------------------------------------
	// SMTP absent
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("SMTP absent")
	class SmtpAbsent {

		@Test
		@DisplayName("smtpServer is null, verifyEmail is false, no exception")
		void smtpNotConfigured() {
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new DefaultConfig(), true);
			var r = factory.getDefaultRealmRepresentation();
			assertNull(r.getSmtpServer());
			assertFalse(r.isVerifyEmail());
		}
	}

	// ---------------------------------------------------------------
	// SMTP present
	// ---------------------------------------------------------------

	@Nested
	@DisplayName("SMTP present")
	class SmtpPresent {

		@Test
		@DisplayName("SMTP fully configured: server map and verifyEmail")
		void smtpConfigured() {
			var config = new DefaultConfig() {
				@Override
				public Optional<SmtpConfig> smtp() {
					return Optional.of(new SmtpConfig() {
						@Override public Optional<String> host() { return Optional.of("smtp.example.com"); }
						@Override public Optional<Integer> port() { return Optional.of(587); }
						@Override public Optional<String> from() { return Optional.of("noreply@example.com"); }
						@Override public boolean auth() { return true; }
						@Override public boolean starttls() { return true; }
						@Override public boolean ssl() { return false; }
						@Override public Optional<String> user() { return Optional.of("user"); }
						@Override public Optional<String> password() { return Optional.of("pass"); }
					});
				}
			};
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				config, true);
			var r = factory.getDefaultRealmRepresentation();
			var smtp = r.getSmtpServer();
			assertNotNull(smtp);
			assertEquals("smtp.example.com", smtp.get("host"));
			assertEquals("587", smtp.get("port"));
			assertEquals("noreply@example.com", smtp.get("from"));
			assertEquals("true", smtp.get("auth"));
			assertEquals("true", smtp.get("starttls"));
			assertEquals("false", smtp.get("ssl"));
			assertEquals("user", smtp.get("user"));
			assertEquals("pass", smtp.get("password"));
			assertTrue(r.isVerifyEmail());
		}

		@Test
		@DisplayName("SMTP without TLS: same behavior, no enforcement")
		void smtpWithoutTls() {
			var config = new DefaultConfig() {
				@Override
				public Optional<SmtpConfig> smtp() {
					return Optional.of(new SmtpConfig() {
						@Override public Optional<String> host() { return Optional.of("localhost"); }
						@Override public Optional<Integer> port() { return Optional.of(25); }
						@Override public Optional<String> from() { return Optional.of("test@localhost"); }
						@Override public boolean auth() { return false; }
						@Override public boolean starttls() { return false; }
						@Override public boolean ssl() { return false; }
						@Override public Optional<String> user() { return Optional.empty(); }
						@Override public Optional<String> password() { return Optional.empty(); }
					});
				}
			};
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				config, true);
			var r = factory.getDefaultRealmRepresentation();
			var smtp = r.getSmtpServer();
			assertNotNull(smtp);
			assertEquals("localhost", smtp.get("host"));
			assertEquals("25", smtp.get("port"));
			assertTrue(r.isVerifyEmail());
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
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
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
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
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
	@DisplayName("TC-U8 — Browser security headers")
	class BrowserSecurityHeadersTest {

		private Map<String, String> headers;

		@BeforeEach
		void setUp() {
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
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
			var factory = new KeycloakDefaultRealmRepresentationFactory(
				new HardenedConfig(), true);
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
				.collect(java.util.stream.Collectors.toMap(
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

	// ---------------------------------------------------------------
	// Config implementations for testing
	// ---------------------------------------------------------------

	/**
	 * Config with ALL hardening features disabled/zeroed.
	 * Used to verify legacy equivalence, the factory
	 * should leave all new fields null.
	 */
	static class DefaultConfig implements RealmTemplateConfig {

		@Override
		public PasswordPolicyConfig passwordPolicy() {
			return new PasswordPolicyConfig() {
				@Override public int length() { return 0; }
				@Override public int upperCase() { return 0; }
				@Override public int lowerCase() { return 0; }
				@Override public int digits() { return 0; }
				@Override public int specialChars() { return 0; }
				@Override public int history() { return 0; }
				@Override public int expirationDays() { return 0; }
				@Override public Optional<String> blacklist() { return Optional.empty(); }
			};
		}

		@Override
		public BruteForceConfig bruteForce() {
			return new BruteForceConfig() {
				@Override public int failureFactor() { return 0; }
				@Override public boolean permanentLockout() { return false; }
				@Override public int waitIncrementSeconds() { return 0; }
				@Override public int maxFailureWaitSeconds() { return 0; }
				@Override public int maxDeltaTimeSeconds() { return 0; }
				@Override public int minimumQuickLoginWaitSeconds() { return 0; }
				@Override public int quickLoginCheckMilliSeconds() { return 0; }
				@Override public BruteForceStrategy strategy() { return BruteForceStrategy.MULTIPLE; }
				@Override public int maxTemporaryLockouts() { return 0; }
			};
		}

		@Override
		public EventsConfig events() {
			return new EventsConfig() {
				@Override public boolean enabled() { return false; }
				@Override public int expirationSeconds() { return 0; }
				@Override public List<String> listeners() { return List.of(); }
				@Override public List<String> enabledTypes() { return List.of(); }
				@Override public boolean adminEnabled() { return false; }
				@Override public boolean adminDetailsEnabled() { return false; }
			};
		}

		@Override
		public Optional<SmtpConfig> smtp() { return Optional.empty(); }
	}

	/**
	 * Config with all hardening features set to the
	 * production secure defaults.
	 */
	static class HardenedConfig implements RealmTemplateConfig {

		@Override
		public PasswordPolicyConfig passwordPolicy() {
			return new PasswordPolicyConfig() {
				@Override public int length() { return 8; }
				@Override public int upperCase() { return 1; }
				@Override public int lowerCase() { return 1; }
				@Override public int digits() { return 1; }
				@Override public int specialChars() { return 1; }
				@Override public int history() { return 5; }
				@Override public int expirationDays() { return 90; }
				@Override public Optional<String> blacklist() { return Optional.empty(); }
			};
		}

		@Override
		public BruteForceConfig bruteForce() {
			return new BruteForceConfig() {
				@Override public int failureFactor() { return 10; }
				@Override public boolean permanentLockout() { return true; }
				@Override public int waitIncrementSeconds() { return 60; }
				@Override public int maxFailureWaitSeconds() { return 900; }
				@Override public int maxDeltaTimeSeconds() { return 43200; }
				@Override public int minimumQuickLoginWaitSeconds() { return 60; }
				@Override public int quickLoginCheckMilliSeconds() { return 1000; }
				@Override public BruteForceStrategy strategy() { return BruteForceStrategy.MULTIPLE; }
				@Override public int maxTemporaryLockouts() { return 5; }
			};
		}

		@Override
		public EventsConfig events() {
			return new EventsConfig() {
				@Override public boolean enabled() { return true; }
				@Override public int expirationSeconds() { return 2592000; }
				@Override public List<String> listeners() {
					return List.of("jboss-logging");
				}
				@Override public List<String> enabledTypes() {
					return List.of(
						"LOGIN", "LOGIN_ERROR",
						"LOGOUT", "LOGOUT_ERROR",
						"CODE_TO_TOKEN", "CODE_TO_TOKEN_ERROR",
						"REFRESH_TOKEN", "REFRESH_TOKEN_ERROR",
						"REGISTER", "REGISTER_ERROR",
						"UPDATE_PASSWORD", "UPDATE_PASSWORD_ERROR",
						"RESET_PASSWORD", "RESET_PASSWORD_ERROR",
						"SEND_RESET_PASSWORD",
						"REMOVE_TOTP", "UPDATE_TOTP",
						"USER_DISABLED_BY_PERMANENT_LOCKOUT",
						"USER_DISABLED_BY_TEMPORARY_LOCKOUT",
						"PERMISSION_TOKEN");
				}
				@Override public boolean adminEnabled() { return true; }
				@Override public boolean adminDetailsEnabled() { return true; }
			};
		}

		@Override
		public Optional<SmtpConfig> smtp() { return Optional.empty(); }
	}

}
