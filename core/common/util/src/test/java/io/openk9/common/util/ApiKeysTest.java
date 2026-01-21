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

package io.openk9.common.util;

import static io.openk9.common.util.ApiKeys.DEFAULT_KEY_PREFIX;
import static io.openk9.common.util.ApiKeys.DEFAULT_RANDOM_BYTES;
import static io.openk9.common.util.ApiKeys.KEY_PART_SEPARATOR;
import static io.openk9.common.util.ApiKeys.generateApiKey;
import static io.openk9.common.util.ApiKeys.generateApiKeyWithChecksum;
import static io.openk9.common.util.ApiKeys.sha256;
import static io.openk9.common.util.ApiKeys.verifyChecksum;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ApiKeysTest {

	@Test
	void generateApiKey_withPrefix_hasCorrectFormat() {
		String key = generateApiKey("k9", 8);

		assertTrue(key.startsWith("k9" + KEY_PART_SEPARATOR));
		assertFalse(key.endsWith("_")); // should not end with separator

		assertEquals("pp_xxxxxxxxxxxxxxxx".length(), key.length());
	}

	@Test
	void generateApiKey_withoutPrefix_defaultPrefix() {
		String key = generateApiKey(null, DEFAULT_RANDOM_BYTES);
		assertTrue(key.startsWith(DEFAULT_KEY_PREFIX));
		assertFalse(key.endsWith("_")); // should not end with separator
	}

	@Test
	void generateApiKey_withoutRandomBytes_defaultRandomBytes() {
		String key = generateApiKey(DEFAULT_KEY_PREFIX, 0);

		assertTrue(key.startsWith(DEFAULT_KEY_PREFIX + KEY_PART_SEPARATOR));
		assertFalse(key.endsWith("_")); // should not end with separator

		assertEquals("pp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".length(), key.length());
	}

	@Test
	void apiKeyWithChecksum_areConsistent() {
		String apiKey = generateApiKeyWithChecksum(DEFAULT_KEY_PREFIX, DEFAULT_RANDOM_BYTES);
		assertTrue(verifyChecksum(apiKey), "Checksum must be valid");

		// Compute digests
		byte[] digest1 = sha256(apiKey);
		byte[] digest2 = sha256(apiKey);

		// Verify determinism
		assertArrayEquals(digest1, digest2, "Digests must match for same input");
	}

}
