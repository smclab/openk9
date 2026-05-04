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
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import org.keycloak.representations.idm.RealmRepresentation.BruteForceStrategy;

/**
 * Configuration mapping for Keycloak realm template defaults.
 * <p>
 * All values have secure defaults so that, in the absence of
 * explicit configuration, every newly provisioned realm is
 * hardened with production-ready security settings.
 * </p>
 */
@ConfigMapping(prefix = "io.openk9.tenantmanager.keycloak.realm")
public interface RealmTemplateConfig {

	/**
	 * Password policy settings applied to the realm.
	 */
	PasswordPolicyConfig passwordPolicy();

	/**
	 * Brute force detection settings.
	 */
	BruteForceConfig bruteForce();

	/**
	 * Event and audit logging settings.
	 */
	EventsConfig events();

	/**
	 * Optional SMTP configuration. When absent, email
	 * notifications and self-service password reset are
	 * disabled and a WARN log is emitted at provisioning time.
	 */
	Optional<SmtpConfig> smtp();

	interface PasswordPolicyConfig {

		@WithDefault("8")
		int length();

		@WithDefault("1")
		int upperCase();

		@WithDefault("1")
		int lowerCase();

		@WithDefault("1")
		int digits();

		@WithDefault("1")
		int specialChars();

		@WithDefault("5")
		int history();

		@WithDefault("90")
		int expirationDays();

		/**
		 * Optional path to a password blacklist file on the
		 * Keycloak volume. When empty, the
		 * {@code passwordBlacklist(...)} term is omitted from
		 * the password policy string.
		 */
		Optional<String> blacklist();
	}

	interface BruteForceConfig {

		@WithDefault("10")
		int failureFactor();

		@WithDefault("true")
		boolean permanentLockout();

		@WithDefault("60")
		int waitIncrementSeconds();

		@WithDefault("900")
		int maxFailureWaitSeconds();

		@WithDefault("43200")
		int maxDeltaTimeSeconds();

		@WithDefault("60")
		int minimumQuickLoginWaitSeconds();

		@WithDefault("1000")
		int quickLoginCheckMilliSeconds();

		@WithDefault("MULTIPLE")
		BruteForceStrategy strategy();

		@WithDefault("5")
		int maxTemporaryLockouts();
	}

	interface EventsConfig {

		@WithDefault("true")
		boolean enabled();

		@WithDefault("2592000")
		int expirationSeconds();

		@WithDefault("jboss-logging")
		List<String> listeners();

		@WithDefault("LOGIN,LOGIN_ERROR,LOGOUT,LOGOUT_ERROR,"
			+ "CODE_TO_TOKEN,CODE_TO_TOKEN_ERROR,"
			+ "REFRESH_TOKEN,REFRESH_TOKEN_ERROR,"
			+ "REGISTER,REGISTER_ERROR,"
			+ "UPDATE_PASSWORD,UPDATE_PASSWORD_ERROR,"
			+ "RESET_PASSWORD,RESET_PASSWORD_ERROR,"
			+ "SEND_RESET_PASSWORD,"
			+ "REMOVE_TOTP,UPDATE_TOTP,"
			+ "USER_DISABLED_BY_PERMANENT_LOCKOUT,"
			+ "USER_DISABLED_BY_TEMPORARY_LOCKOUT,"
			+ "PERMISSION_TOKEN")
		List<String> enabledTypes();

		@WithDefault("true")
		boolean adminEnabled();

		@WithDefault("true")
		boolean adminDetailsEnabled();
	}

	interface SmtpConfig {

		Optional<String> host();

		Optional<Integer> port();

		Optional<String> from();

		@WithDefault("false")
		boolean auth();

		@WithDefault("false")
		boolean starttls();

		@WithDefault("false")
		boolean ssl();

		Optional<String> user();

		Optional<String> password();
	}

}
