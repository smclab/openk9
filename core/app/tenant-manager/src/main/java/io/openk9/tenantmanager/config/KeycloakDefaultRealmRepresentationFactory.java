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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;

/**
 * Factory that produces a default {@link RealmRepresentation}
 * for newly provisioned Keycloak realms, driven by
 * {@link RealmTemplateConfig}.
 */
@ApplicationScoped
public class KeycloakDefaultRealmRepresentationFactory {

	private final RealmTemplateConfig config;

	@Inject
	public KeycloakDefaultRealmRepresentationFactory(
		RealmTemplateConfig config) {
		this.config = config;
	}

	/**
	 * Package-private constructor for testing with a custom
	 * config without CDI.
	 */
	KeycloakDefaultRealmRepresentationFactory(
		RealmTemplateConfig config, boolean ignored) {
		this.config = config;
	}

	/**
	 * Returns a default {@link RealmRepresentation} with
	 * security hardening fields populated from the active
	 * configuration.
	 * <p>
	 * When a config section signals "not configured"
	 * (e.g. {@code failureFactor == 0} for brute force,
	 * {@code length == 0} for password policy,
	 * {@code enabled == false} for events), the corresponding
	 * fields are left {@code null} to match the legacy Qute
	 * template output.
	 * </p>
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
		String policy = buildPasswordPolicy();
		if (policy != null) {
			realm.setPasswordPolicy(policy);
		}

		// Brute force protection
		var bf = config.bruteForce();
		if (bf.failureFactor() > 0) {
			realm.setBruteForceProtected(true);
			realm.setPermanentLockout(bf.permanentLockout());
			realm.setFailureFactor(bf.failureFactor());
			realm.setMaxTemporaryLockouts(bf.maxTemporaryLockouts());
			realm.setWaitIncrementSeconds(bf.waitIncrementSeconds());
			realm.setMinimumQuickLoginWaitSeconds(
				bf.minimumQuickLoginWaitSeconds());
			realm.setQuickLoginCheckMilliSeconds(
				(long) bf.quickLoginCheckMilliSeconds());
			realm.setMaxFailureWaitSeconds(bf.maxFailureWaitSeconds());
			realm.setMaxDeltaTimeSeconds(bf.maxDeltaTimeSeconds());
			realm.setBruteForceStrategy(bf.strategy());
		}

		// Events / audit
		var ev = config.events();
		if (ev.enabled()) {
			realm.setEventsEnabled(true);
			realm.setEventsExpiration((long) ev.expirationSeconds());
			realm.setEventsListeners(new ArrayList<>(ev.listeners()));
			realm.setEnabledEventTypes(
				new ArrayList<>(ev.enabledTypes()));
			realm.setAdminEventsEnabled(ev.adminEnabled());
			realm.setAdminEventsDetailsEnabled(ev.adminDetailsEnabled());
		}

		// SMTP / Email Notifications
		config.smtp().ifPresentOrElse(
			smtp -> {
				if (smtp.host().isEmpty()
					|| smtp.port().isEmpty()
					|| smtp.from().isEmpty()) {
					realm.setVerifyEmail(false);
					return;
				}
				Map<String, String> smtpMap = new LinkedHashMap<>();
				smtpMap.put("host", smtp.host().get());
				smtpMap.put(
					"port", String.valueOf(smtp.port().get()));
				smtpMap.put("from", smtp.from().get());
				smtpMap.put(
					"auth", String.valueOf(smtp.auth()));
				smtpMap.put(
					"starttls", String.valueOf(smtp.starttls()));
				smtpMap.put(
					"ssl", String.valueOf(smtp.ssl()));
				smtp.user().ifPresent(
					u -> smtpMap.put("user", u));
				smtp.password().ifPresent(
					p -> smtpMap.put("password", p));
				realm.setSmtpServer(smtpMap);
				realm.setVerifyEmail(true);
			},
			() -> {
				realm.setVerifyEmail(false);
			}
		);

		// Roles
		realm.setRoles(createDefaultRoles());

		// Client openk9
		realm.setClients(List.of(createDefaultOpenk9Client()));

		return realm;
	}

	/**
	 * Builds the password policy string from configuration.
	 * Returns {@code null} when all policy sub-values are at
	 * their zero/unconfigured state.
	 */
	String buildPasswordPolicy() {
		var pp = config.passwordPolicy();

		var clauses = new ArrayList<String>();

		if (pp.length() > 0) {
			clauses.add("length(" + pp.length() + ")");
		}
		if (pp.upperCase() > 0) {
			clauses.add("upperCase(" + pp.upperCase() + ")");
		}
		if (pp.lowerCase() > 0) {
			clauses.add("lowerCase(" + pp.lowerCase() + ")");
		}
		if (pp.digits() > 0) {
			clauses.add("digits(" + pp.digits() + ")");
		}
		if (pp.specialChars() > 0) {
			clauses.add("specialChars(" + pp.specialChars() + ")");
		}
		clauses.add("notUsername(undefined)");
		clauses.add("notEmail(undefined)");
		if (pp.history() > 0) {
			clauses.add("passwordHistory(" + pp.history() + ")");
		}
		if (pp.expirationDays() > 0) {
			clauses.add("forceExpiredPasswordChange("
				+ pp.expirationDays() + ")");
		}
		pp.blacklist().ifPresent(bl ->
			clauses.add("passwordBlacklist(" + bl + ")"));

		if (clauses.size() <= 2) {
			// Only notUsername and notEmail — unconfigured
			return null;
		}

		return String.join(" and ", clauses);
	}

	/**
	 * Returns the default browser security headers map.
	 * Values aligned with Keycloak 26.x defaults.
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
