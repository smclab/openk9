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

/**
 * Thrown when a tenant name does not conform to PostgreSQL
 * identifier rules: must start with a lowercase letter,
 * contain only lowercase letters, digits, and underscores,
 * and be at most 63 characters long.
 */
public class InvalidTenantNameException extends RuntimeException {

	/**
	 * Creates a new exception for the given invalid tenant name.
	 *
	 * @param tenantName the invalid name that was rejected
	 */
	public InvalidTenantNameException(String tenantName) {
		super(String.format(
			"Invalid tenant name '%s'. Must match [a-z][a-z0-9_]{0,62}: "
			+ "start with a lowercase letter, contain only lowercase "
			+ "letters, digits, and underscores, max 63 characters.",
			tenantName));
	}
}
