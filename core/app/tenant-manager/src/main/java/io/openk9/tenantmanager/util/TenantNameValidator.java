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

package io.openk9.tenantmanager.util;

import java.util.regex.Pattern;

import io.openk9.tenantmanager.service.InvalidTenantNameException;

/**
 * Validates tenant names against PostgreSQL identifier rules.
 *
 * <p>A valid tenant name must:
 * <ul>
 *   <li>Start with a lowercase letter</li>
 *   <li>Contain only lowercase letters and digits</li>
 *   <li>Be at most 63 characters long (PostgreSQL limit)</li>
 * </ul>
 *
 * <p>This ensures the name is safe for use as a PostgreSQL
 * schema name without quoting.
 */
public final class TenantNameValidator {

	/**
	 * PostgreSQL unquoted identifier pattern: starts with a
	 * lowercase letter, followed by up to 62 lowercase
	 * alphanumeric characters.
	 */
	private static final Pattern VALID_NAME =
		Pattern.compile("^[a-z][a-z0-9]{0,62}$");

	private TenantNameValidator() {}

	/**
	 * Validates that the given tenant name is a valid
	 * PostgreSQL schema identifier.
	 *
	 * @param tenantName the name to validate
	 * @throws InvalidTenantNameException if the name is null
	 *         or does not match the required pattern
	 */
	public static void validate(String tenantName) {
		if (tenantName == null
			|| !VALID_NAME.matcher(tenantName).matches()) {

			throw new InvalidTenantNameException(tenantName);
		}
	}
}
