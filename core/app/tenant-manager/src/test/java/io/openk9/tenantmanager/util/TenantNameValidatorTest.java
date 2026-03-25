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

import io.openk9.tenantmanager.service.InvalidTenantNameException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TenantNameValidator")
class TenantNameValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = {
		"pikachu",
		"demo",
		"tenant_1",
		"a",
		"abc123",
		"my_tenant_name",
	})
	@DisplayName("accepts valid tenant names")
	void acceptsValidNames(String name) {
		assertDoesNotThrow(() -> TenantNameValidator.validate(name));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		"my-tenant",
		"My_Tenant",
		"UPPER",
		"123abc",
		"_leading_underscore",
		"has space",
		"has.dot",
		"schema; DROP TABLE",
		"a\"b",
	})
	@DisplayName("rejects invalid tenant names")
	void rejectsInvalidNames(String name) {
		assertThrows(
			InvalidTenantNameException.class,
			() -> TenantNameValidator.validate(name));
	}

	@Test
	@DisplayName("rejects names exceeding 63 characters")
	void rejectsNameExceedingMaxLength() {
		String tooLong = "a".repeat(64);

		assertThrows(
			InvalidTenantNameException.class,
			() -> TenantNameValidator.validate(tooLong));
	}

	@Test
	@DisplayName("accepts name at exactly 63 characters")
	void acceptsNameAtMaxLength() {
		String maxLength = "a".repeat(63);

		assertDoesNotThrow(
			() -> TenantNameValidator.validate(maxLength));
	}
}
