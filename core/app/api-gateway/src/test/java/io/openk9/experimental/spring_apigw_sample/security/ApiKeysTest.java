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

package io.openk9.experimental.spring_apigw_sample.security;

import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.DEFAULT_KEY_PREFIX;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.DEFAULT_RANDOM_BYTES;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.DEFAULT_SALT_BYTES;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.KEY_PART_SEPARATOR;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.SaltDigest;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.generateApiKey;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.generateApiKeyWithChecksum;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.generateSalt;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.sha256;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.sha256WithSalt;
import static io.openk9.experimental.spring_apigw_sample.security.ApiKeys.verifyChecksum;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

		// Compute salted digest
		byte[] digest1 = sha256(apiKey);
		byte[] digest2 = sha256(apiKey);

		// Verify determinism
		assertArrayEquals(digest1, digest2, "Digests must match for same input");
	}


	@Test
	void apiKeyWithChecksum_andSaltedDigest_areConsistent() {
		String apiKey = generateApiKeyWithChecksum(DEFAULT_KEY_PREFIX, DEFAULT_RANDOM_BYTES);
		assertTrue(verifyChecksum(apiKey), "Checksum must be valid");

		// Strip checksum part
		int idx = apiKey.lastIndexOf(KEY_PART_SEPARATOR);
		String withoutChecksum = apiKey.substring(0, idx);

		// Generate salt
		byte[] salt = generateSalt(DEFAULT_SALT_BYTES);

		// Compute salted digest
		SaltDigest digest1 = sha256WithSalt(apiKey, salt);
		SaltDigest digest2 = sha256WithSalt(apiKey, salt);

		// Verify determinism with same salt
		assertArrayEquals(
			digest1.digest(),
			digest2.digest(),
			"Salted digests must match for same input and salt"
		);
		assertArrayEquals(digest1.salt(), digest2.salt(), "Salt values must be preserved");

		// Ensure digest differs with different salt
		byte[] newSalt = generateSalt(DEFAULT_SALT_BYTES);
		SaltDigest digest3 = sha256WithSalt(withoutChecksum, newSalt);

		assertNotEquals(
			new String(digest1.digest()), new String(digest3.digest()),
			"Digests must differ when salt is different"
		);
	}

}
