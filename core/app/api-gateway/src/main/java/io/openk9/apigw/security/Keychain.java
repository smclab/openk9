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

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.openk9.common.util.ApiKeys;
import io.openk9.event.tenant.ApiGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * A store for trusted API key digests.
 * <p>
 * A {@link  Keychain} maintains a collection of SHA-256 {@code digests} and
 * {@code checksums} representing valid API keys.
 * API keys can be added by hashing raw secrets or by supplying precomputed digests.
 * <p>
 * On validation, the provided API key is first verified against its checksum
 * to detect accidental corruption or tampering. If the checksum passes,
 * the key is hashed and compared to the stored digests in constant time.
 */
@Slf4j
public class Keychain {

	private static final HexFormat HEX = HexFormat.of();
	private final Key[] keys;

	/**
	 * Creates a {@link Keychain} from the given keys.
	 *
	 * @param keys the trusted API key entries
	 * @return a new {@link Keychain}
	 */
	public static Keychain of(Key... keys) {
		return new Keychain(keys);
	}

	/**
	 * Creates a {@link Keychain} from the given list of keys.
	 *
	 * @param keys the trusted API key entries
	 * @return a new {@link Keychain}
	 */
	public static Keychain of(List<Key> keys) {
		return new Keychain(keys.toArray(Key[]::new));
	}

	private Keychain(Key[] keys) {
		this.keys = keys;
	}

	/**
	 * Checks whether the provided API key exists in this {@code Keychain}.
	 * <p>
	 * Validation consists of two steps:
	 * <ol>
	 *   <li>Verify the checksum suffix of the API key. If the checksum is invalid,
	 *       a {@link ChecksumValidationException} is thrown.</li>
	 *   <li>Compute the SHA-256 digest of the API key and compare it in constant time
	 *       against all stored digests.</li>
	 * </ol>
	 *
	 * @param apiKey the API key to validate
	 * @return {@code true} if the key is present in the {@code Keychain}, {@code false} otherwise
	 * @throws ChecksumValidationException if the API key fails checksum verification
	 */
	public boolean contains(String apiKey)
		throws ChecksumValidationException {

		return find(apiKey).isPresent();
	}

	/**
	 * Finds the {@link Key} matching the provided API key in this {@code Keychain}.
	 * <p>
	 * Validation consists of two steps:
	 * <ol>
	 *   <li>Verify the checksum suffix of the API key. If the checksum is invalid,
	 *       a {@link ChecksumValidationException} is thrown.</li>
	 *   <li>Compute the SHA-256 digest of the API key and compare it in constant time
	 *       against all stored digests.</li>
	 * </ol>
	 *
	 * @param apiKey the API key to look up
	 * @return an {@link Optional} containing the matched {@link Key}, or empty if not found
	 * @throws ChecksumValidationException if the API key fails checksum verification
	 */
	public Optional<Key> find(String apiKey)
		throws ChecksumValidationException {

		if (!ApiKeys.verifyChecksum(apiKey)) {
			throw new ChecksumValidationException(
				"Invalid API key: checksum verification failed");
		}

		byte[] hash = ApiKeys.sha256(apiKey);

		for (Key key : keys) {
			if (key.verify(hash)) {
				return Optional.of(key);
			}
		}

		return Optional.empty();
	}

	/**
	 * Returns all keys stored in this {@link Keychain}.
	 *
	 * @return unmodifiable list of keys
	 */
	public List<Key> getKeys() {
		return Arrays.stream(this.keys).toList();
	}

	/**
	 * Return all digests in their hexadecimal {@link String} form.
	 *
	 * @return unmodifiable {@link List} of hex digests
	 */
	public List<String> getDigests() {
		return Arrays.stream(this.keys)
			.map(Key::getDigest)
			.map(HEX::formatHex)
			.toList();
	}

	/**
	 * Return all checksums in their hexadecimal {@link String} form.
	 *
	 * @return unmodifiable {@link List} of hex digests
	 */
	public List<String> getChecksums() {
		return Arrays.stream(this.keys)
			.map(Key::getChecksum)
			.toList();
	}

	/**
	 * Represents a stored API key entry in the {@link Keychain}.
	 * <p>
	 * Each entry contains:
	 * <ul>
	 *   <li>{@code digest} – the 32-byte SHA-256 digest of the API key</li>
	 *   <li>{@code checksum} – the checksum string (e.g., CRC-32) of the API key,
	 *       typically used for UI feedback or copy/paste error detection</li>
	 *   <li>{@code apiGroup} – the API group this key belongs to</li>
	 *   <li>{@code expirationDate} – the expiration instant (nullable means
	 *       the key never expires)</li>
	 * </ul>
	 */
	public static class Key {

		private final byte[] digest;
		private final String checksum;
		private final ApiGroup apiGroup;
		private final Instant expirationDate;

		/**
		 * Creates a {@link Key} from a hex-encoded digest.
		 *
		 * @param hexDigest the SHA-256 digest as a hex string
		 * @param checksum the CRC-32 checksum string
		 * @param apiGroup the API group this key belongs to
		 * @param expirationDate the expiration instant, or
		 *        {@code null} if the key never expires
		 * @return a new {@link Key}
		 */
		public static Key of(
			String hexDigest, String checksum,
			ApiGroup apiGroup, Instant expirationDate) {

			byte[] digest = HEX.parseHex(hexDigest);
			return new Key(digest, checksum, apiGroup, expirationDate);
		}

		/**
		 * Creates a {@link Key} from a raw byte digest.
		 *
		 * @param digest the 32-byte SHA-256 digest
		 * @param checksum the CRC-32 checksum string
		 * @param apiGroup the API group this key belongs to
		 * @param expirationDate the expiration instant, or
		 *        {@code null} if the key never expires
		 * @return a new {@link Key}
		 */
		public static Key of(
			byte[] digest, String checksum,
			ApiGroup apiGroup, Instant expirationDate) {

			return new Key(digest, checksum, apiGroup, expirationDate);
		}

		private Key(
			byte[] digest, String checksum,
			ApiGroup apiGroup, Instant expirationDate) {

			Objects.requireNonNull(digest);
			Objects.requireNonNull(checksum);
			Objects.requireNonNull(apiGroup);
			if (digest.length != 32) {
				throw new IllegalArgumentException(
					"Invalid digest length: expected 32 bytes");
			}
			this.digest = Arrays.copyOf(digest, 32);
			this.checksum = checksum;
			this.apiGroup = apiGroup;
			this.expirationDate = expirationDate;
		}

		/** Returns a copy of the 32-byte SHA-256 digest. */
		public byte[] getDigest() {
			return Arrays.copyOf(this.digest, 32);
		}

		/** Returns the CRC-32 checksum string. */
		public String getChecksum() {
			return this.checksum;
		}

		/** Returns the {@link ApiGroup} this key belongs to. */
		public ApiGroup getApiGroup() {
			return this.apiGroup;
		}

		/**
		 * Returns the expiration instant, or {@code null}
		 * if the key never expires.
		 */
		public Instant getExpirationDate() {
			return this.expirationDate;
		}

		/**
		 * Returns {@code true} if this key has an expiration date
		 * and the current time is past it.
		 *
		 * @return {@code true} if expired, {@code false} otherwise
		 */
		public boolean isExpired() {
			return expirationDate != null
				&& Instant.now().isAfter(expirationDate);
		}

		/**
		 * Compares the given hash against this key's digest
		 * in constant time.
		 *
		 * @param hash the SHA-256 hash to compare
		 * @return {@code true} if the hashes match
		 */
		public boolean verify(byte[] hash) {
			return MessageDigest.isEqual(digest, hash);
		}
	}
}
