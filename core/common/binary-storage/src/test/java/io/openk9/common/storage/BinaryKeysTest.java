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

package io.openk9.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BinaryKeysTest {

	@Test
	void keyHasNoTenantSegment() {
		assertEquals(
			"42/content-1/file-1",
			BinaryKeys.key(42L, "content-1", "file-1"));
	}

	@Test
	void bucketUsesDefaultTemplate() {
		assertEquals(
			"openk9-acme",
			BinaryKeys.bucket("acme", BinaryKeys.DEFAULT_BUCKET_TEMPLATE));
	}

	@Test
	void bucketHonoursCustomTemplate() {
		assertEquals(
			"bin-acme-store",
			BinaryKeys.bucket("acme", "bin-{tenant}-store"));
	}

	@Test
	void datasourcePrefixMatchesEveryKeyOfTheDatasource() {
		// the prefix ends with the separator so it cannot match a sibling
		// datasource whose id shares the same digits (e.g. 42 vs 420)
		assertEquals("42/", BinaryKeys.datasourcePrefix(42L));

		// every key of the datasource starts with its prefix
		assertTrue(
			BinaryKeys.key(42L, "content-1", "file-1")
				.startsWith(BinaryKeys.datasourcePrefix(42L)));
	}

}
