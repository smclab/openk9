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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RealmRepresentation.BruteForceStrategy;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;

/**
 * Factory that produces a default {@link RealmRepresentation}
 * for newly provisioned Keycloak realms with security hardening
 * values hardcoded as constants.
 * <p>
 * All tuning of realm parameters after provisioning could be done
 * via the admin console.
 * </p>
 */
@ApplicationScoped
public class KeycloakDefaultRealmRepresentationFactory {

	// Brute force protection (Keycloak recommended)
	private static final int BF_FAILURE_FACTOR = 10;
	private static final boolean BF_PERMANENT_LOCKOUT = true;
	private static final int BF_WAIT_INCREMENT_SECONDS = 60;
	private static final int BF_MAX_FAILURE_WAIT_SECONDS = 900;
	private static final int BF_MAX_DELTA_TIME_SECONDS = 43200;
	private static final int BF_MIN_QUICK_LOGIN_WAIT_SECONDS = 60;
	private static final long BF_QUICK_LOGIN_CHECK_MS = 1000L;
	private static final int BF_MAX_TEMPORARY_LOCKOUTS = 5;
	private static final BruteForceStrategy BF_STRATEGY =
		BruteForceStrategy.MULTIPLE;

	// Events / audit (compliance defaults)
	private static final boolean EVENTS_ENABLED = true;
	private static final long EVENTS_EXPIRATION_SECONDS = 2_592_000L;
	private static final List<String> EVENTS_LISTENERS =
		List.of("jboss-logging");
	private static final List<String> EVENTS_ENABLED_TYPES = List.of(
		"LOGIN", "LOGIN_ERROR", "LOGOUT", "LOGOUT_ERROR",
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
	private static final boolean EVENTS_ADMIN_ENABLED = true;
	private static final boolean EVENTS_ADMIN_DETAILS_ENABLED = true;

	// Pre-built password policy string
	private static final String DEFAULT_PASSWORD_POLICY =
		"length(8) and upperCase(1) and lowerCase(1) and digits(1)"
		+ " and specialChars(1) and notUsername(undefined)"
		+ " and notEmail(undefined) and passwordHistory(5)"
		+ " and forceExpiredPasswordChange(90)";

	/**
	 * Returns a default {@link RealmRepresentation} with
	 * security hardening.
	 *
	 * @return a new {@link RealmRepresentation} instance
	 */
	public RealmRepresentation getDefaultRealmRepresentation() {
		var realm = new RealmRepresentation();

		// Basic realm settings
		realm.setEnabled(true);
		realm.setSslRequired("external");
		realm.setRegistrationAllowed(false);
		realm.setLoginWithEmailAllowed(true);
		realm.setDuplicateEmailsAllowed(false);
		realm.setEditUsernameAllowed(false);

		// Browser security headers
		realm.setBrowserSecurityHeaders(defaultBrowserSecurityHeaders());

		// Password policy
		realm.setPasswordPolicy(DEFAULT_PASSWORD_POLICY);

		// Brute force protection
		realm.setBruteForceProtected(true);
		realm.setPermanentLockout(BF_PERMANENT_LOCKOUT);
		realm.setFailureFactor(BF_FAILURE_FACTOR);
		realm.setMaxTemporaryLockouts(BF_MAX_TEMPORARY_LOCKOUTS);
		realm.setWaitIncrementSeconds(BF_WAIT_INCREMENT_SECONDS);
		realm.setMinimumQuickLoginWaitSeconds(
			BF_MIN_QUICK_LOGIN_WAIT_SECONDS);
		realm.setQuickLoginCheckMilliSeconds(BF_QUICK_LOGIN_CHECK_MS);
		realm.setMaxFailureWaitSeconds(BF_MAX_FAILURE_WAIT_SECONDS);
		realm.setMaxDeltaTimeSeconds(BF_MAX_DELTA_TIME_SECONDS);
		realm.setBruteForceStrategy(BF_STRATEGY);

		// Events / audit
		realm.setEventsEnabled(EVENTS_ENABLED);
		realm.setEventsExpiration(EVENTS_EXPIRATION_SECONDS);
		realm.setEventsListeners(new ArrayList<>(EVENTS_LISTENERS));
		realm.setEnabledEventTypes(
			new ArrayList<>(EVENTS_ENABLED_TYPES));
		realm.setAdminEventsEnabled(EVENTS_ADMIN_ENABLED);
		realm.setAdminEventsDetailsEnabled(EVENTS_ADMIN_DETAILS_ENABLED);

		// Roles
		realm.setRoles(createDefaultRoles());

		// Client openk9
		realm.setClients(List.of(createDefaultOpenk9Client()));

		return realm;
	}

	/**
	 * Returns the default browser security headers map.
	 */
	static Map<String, String> defaultBrowserSecurityHeaders() {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("xFrameOptions", "SAMEORIGIN");
		headers.put("contentSecurityPolicy",
			"frame-src 'self'; frame-ancestors 'self';"
				+ " object-src 'none';");
		headers.put("xContentTypeOptions", "nosniff");
		headers.put("strictTransportSecurity",
			"max-age=31536000; includeSubDomains");
		headers.put("xRobotsTag", "none");
		headers.put("xXSSProtection", "1; mode=block");
		headers.put("referrerPolicy", "no-referrer");
		return headers;
	}

	/**
	 * Creates the default realm roles.
	 */
	static RolesRepresentation createDefaultRoles() {
		var roles = new RolesRepresentation();
		roles.setRealm(List.of(
			createRole("k9-admin", "K9 Admin Role"),
			createRole("k9-write", "K9 Write Role"),
			createRole("k9-read", "K9 Read Role")
		));
		return roles;
	}

	private static RoleRepresentation createRole(
		String name, String description) {

		var role = new RoleRepresentation();
		role.setName(name);
		role.setDescription(description);
		role.setComposite(false);
		role.setClientRole(false);
		return role;
	}

	/**
	 * Creates the default {@code openk9} client representation.
	 */
	static ClientRepresentation createDefaultOpenk9Client() {
		var client = new ClientRepresentation();
		client.setClientId("openk9");
		client.setName("openk9");
		client.setEnabled(true);
		client.setClientAuthenticatorType("client-secret");
		client.setStandardFlowEnabled(true);
		client.setPublicClient(true);
		client.setProtocol("openid-connect");
		client.setAttributes(Map.of("login_theme", "openk9"));
		client.setDefaultClientScopes(
			List.of("profile", "email", "roles", "basic"));
		return client;
	}

}
