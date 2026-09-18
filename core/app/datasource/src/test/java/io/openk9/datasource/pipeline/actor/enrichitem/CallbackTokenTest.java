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

package io.openk9.datasource.pipeline.actor.enrichitem;

import java.util.UUID;
import java.util.regex.Pattern;

import io.openk9.common.util.ingestion.ShardingKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CallbackTokenTest {

	private static final ShardingKey PROCESS_KEY =
		ShardingKey.fromStrings("tenant", "schedule-1", "m3k9x", "42");

	// the token is a path segment of the callback endpoint
	private static final Pattern URL_SAFE = Pattern.compile("[A-Za-z0-9_-]+");

	@Test
	void should_round_trip_process_key_and_nonce() {
		// encode a freshly generated token
		var token = CallbackToken.generate(PROCESS_KEY);
		var encoded = token.encode();

		// decoding gives back the same key and nonce
		var decoded = CallbackToken.decode(encoded);
		Assertions.assertEquals(PROCESS_KEY, decoded.processKey());
		Assertions.assertEquals(token.nonce(), decoded.nonce());
	}

	@Test
	void should_only_use_url_safe_characters() {
		// many random nonces, the '#' separated key stays fixed
		for (int i = 0; i < 1000; i++) {
			var encoded = new CallbackToken(
				PROCESS_KEY, UUID.randomUUID().toString()).encode();

			Assertions.assertTrue(
				URL_SAFE.matcher(encoded).matches(),
				"not url safe: " + encoded);
		}
	}

	@Test
	void should_reject_malformed_values() {
		// not base64 at all
		Assertions.assertThrows(
			IllegalArgumentException.class, () -> CallbackToken.decode("%%%"));

		// base64 of something without the separator
		Assertions.assertThrows(
			IllegalArgumentException.class, () -> CallbackToken.decode("bm90LWEtdG9rZW4"));

		// the legacy json token format
		var legacy = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
			"{\"tenantId\":\"t\",\"scheduleId\":\"s\",\"token\":\"x\"}".getBytes());
		Assertions.assertThrows(
			IllegalArgumentException.class, () -> CallbackToken.decode(legacy));

		// a key with a single segment cannot address an entity
		var single = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
			"tenant|nonce".getBytes());
		Assertions.assertThrows(
			IllegalArgumentException.class, () -> CallbackToken.decode(single));

		// empty
		Assertions.assertThrows(
			IllegalArgumentException.class, () -> CallbackToken.decode(""));
	}

}
