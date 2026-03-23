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

package io.openk9.apigw.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import io.openk9.common.util.ApiKeys;
import io.openk9.event.tenant.ApiGroup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Keychain")
class KeychainTest {

	@Nested
	@DisplayName("find()")
	class Find {

		@Test
		@DisplayName("returns the matched key when the API key is valid")
		void returnsKeyWhenValid() {
			String secret = ApiKeys.generateApiKeyWithChecksum("ok9");
			String hash = HexFormat.of().formatHex(ApiKeys.sha256(secret));
			String checksum = ApiKeys.checksumHex(secret);

			Keychain.Key key = Keychain.Key.of(
				hash, checksum, ApiGroup.SEARCH, null);
			Keychain keychain = Keychain.of(key);

			Optional<Keychain.Key> result = keychain.find(secret);

			assertThat(result).isPresent();
		}

		@Test
		@DisplayName("returns empty when the API key is not in the keychain")
		void returnsEmptyWhenNotFound() {
			String stored = ApiKeys.generateApiKeyWithChecksum("ok9");
			String other = ApiKeys.generateApiKeyWithChecksum("ok9");

			String hash = HexFormat.of().formatHex(ApiKeys.sha256(stored));
			String checksum = ApiKeys.checksumHex(stored);

			Keychain.Key key = Keychain.Key.of(
				hash, checksum, ApiGroup.SEARCH, null);
			Keychain keychain = Keychain.of(key);

			Optional<Keychain.Key> result = keychain.find(other);

			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("throws ChecksumValidationException for corrupted key")
		void throwsOnBadChecksum() {
			String secret = ApiKeys.generateApiKeyWithChecksum("ok9");
			String hash = HexFormat.of().formatHex(ApiKeys.sha256(secret));
			String checksum = ApiKeys.checksumHex(secret);

			Keychain.Key key = Keychain.Key.of(
				hash, checksum, ApiGroup.SEARCH, null);
			Keychain keychain = Keychain.of(key);

			// corrupt the checksum by flipping the last character
			String corrupted = secret.substring(0, secret.length() - 1) + "X";

			assertThatThrownBy(() -> keychain.find(corrupted))
				.isInstanceOf(ChecksumValidationException.class);
		}

		@Test
		@DisplayName("returns key with correct apiGroup and expirationDate")
		void returnsKeyWithMetadata() {
			String secret = ApiKeys.generateApiKeyWithChecksum("ok9");
			String hash = HexFormat.of().formatHex(ApiKeys.sha256(secret));
			String checksum = ApiKeys.checksumHex(secret);
			Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);

			Keychain.Key key = Keychain.Key.of(
				hash, checksum, ApiGroup.SEARCH, expiry);
			Keychain keychain = Keychain.of(key);

			Optional<Keychain.Key> result = keychain.find(secret);

			assertThat(result).isPresent();
			assertThat(result.get().getApiGroup()).isEqualTo(ApiGroup.SEARCH);
			assertThat(result.get().getExpirationDate()).isEqualTo(expiry);
		}
	}

	@Nested
	@DisplayName("contains()")
	class Contains {

		@Test
		@DisplayName("returns true when found")
		void returnsTrueWhenFound() {
			String secret = ApiKeys.generateApiKeyWithChecksum("ok9");
			String hash = HexFormat.of().formatHex(ApiKeys.sha256(secret));
			String checksum = ApiKeys.checksumHex(secret);

			Keychain.Key key = Keychain.Key.of(
				hash, checksum, ApiGroup.ADMINISTRATION, null);
			Keychain keychain = Keychain.of(key);

			assertThat(keychain.contains(secret)).isTrue();
		}

		@Test
		@DisplayName("returns false when not found")
		void returnsFalseWhenNotFound() {
			String stored = ApiKeys.generateApiKeyWithChecksum("ok9");
			String other = ApiKeys.generateApiKeyWithChecksum("ok9");

			String hash = HexFormat.of().formatHex(ApiKeys.sha256(stored));
			String checksum = ApiKeys.checksumHex(stored);

			Keychain.Key key = Keychain.Key.of(
				hash, checksum, ApiGroup.ADMINISTRATION, null);
			Keychain keychain = Keychain.of(key);

			assertThat(keychain.contains(other)).isFalse();
		}
	}

	@Nested
	@DisplayName("Key.isExpired()")
	class IsExpired {

		@Test
		@DisplayName("returns false when expirationDate is null")
		void notExpiredWhenNull() {
			Keychain.Key key = Keychain.Key.of(
				"a".repeat(64), "checksum",
				ApiGroup.SEARCH, null);

			assertThat(key.isExpired()).isFalse();
		}

		@Test
		@DisplayName("returns false when expirationDate is in the future")
		void notExpiredWhenFuture() {
			Instant future = Instant.now().plus(1, ChronoUnit.HOURS);
			Keychain.Key key = Keychain.Key.of(
				"a".repeat(64), "checksum",
				ApiGroup.SEARCH, future);

			assertThat(key.isExpired()).isFalse();
		}

		@Test
		@DisplayName("returns true when expirationDate is in the past")
		void expiredWhenPast() {
			Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
			Keychain.Key key = Keychain.Key.of(
				"a".repeat(64), "checksum",
				ApiGroup.SEARCH, past);

			assertThat(key.isExpired()).isTrue();
		}
	}

	@Nested
	@DisplayName("Key factory methods")
	class KeyFactory {

		@Test
		@DisplayName("of(hexDigest, checksum, apiGroup, expirationDate) preserves all fields")
		void fourArgFactory() {
			Instant expiry = Instant.now().plus(30, ChronoUnit.DAYS);
			Keychain.Key key = Keychain.Key.of(
				"b".repeat(64), "chk",
				ApiGroup.INGESTION, expiry);

			assertThat(key.getApiGroup()).isEqualTo(ApiGroup.INGESTION);
			assertThat(key.getExpirationDate()).isEqualTo(expiry);
			assertThat(key.getChecksum()).isEqualTo("chk");
		}

		@Test
		@DisplayName("rejects digest with wrong length")
		void rejectsInvalidDigestLength() {
			assertThatThrownBy(() -> Keychain.Key.of(
				"ab", "checksum", ApiGroup.SEARCH, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("expected 32 bytes");
		}

		@Test
		@DisplayName("rejects null apiGroup")
		void rejectsNullApiGroup() {
			assertThatThrownBy(() -> Keychain.Key.of(
				"a".repeat(64), "checksum", null, null))
				.isInstanceOf(NullPointerException.class);
		}
	}
}
