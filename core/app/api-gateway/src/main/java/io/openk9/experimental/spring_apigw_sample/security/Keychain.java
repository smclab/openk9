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

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;

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
	 * From an array of secrets, apply {@code SHA-256} hash function
	 * and create a new {@link Keychain} from the digests.
	 *
	 * @param secrets the secrets that will be hashed
	 * @return the {@link Keychain} with digests
	 */
	public static Keychain of(String... secrets) {
		int len = secrets.length;
		Key[] keys = new Key[len];

		for (int i = 0; i < len; i++) {
			String checksum = ApiKeys.checksumHex(secrets[i]);
			byte[] digest = ApiKeys.sha256(secrets[i]);

			keys[i] = new Key(digest, checksum);
		}

		return new Keychain(keys);
	}

	/**
	 * From a collection of secrets, apply {@code SHA-256} hash function
	 * and create a new {@link Keychain} from the digests.
	 *
	 * @param secrets the secrets that will be hashed
	 * @return the {@link Keychain} with digests
	 */
	public static Keychain of(Collection<String> secrets) {
		return of(secrets.toArray(String[]::new));
	}

	public Keychain(List<Key> keys) {
		this.keys = keys.toArray(Key[]::new);
	}

	public Keychain(Key[] keys) {
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
	public boolean contains(String apiKey) throws ChecksumValidationException {
		if (!ApiKeys.verifyChecksum(apiKey)) {
			throw new ChecksumValidationException("Invalid API key: checksum verification failed");
		}

		byte[] hash = ApiKeys.sha256(apiKey);

		for (Key key : keys) {
			if (key.verify(hash)) {
				return true;
			}
		}

		return false;
	}

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
	 * </ul>
	 */
	public static class Key {

		private final byte[] digest;
		private final String checksum;

		public static Key of(String hexDigest, String checksum) {
			byte[] digest = HEX.parseHex(hexDigest);
			return new Key(digest, checksum);
		}

		public static Key of(byte[] digest, String checksum) {
			return new Key(digest, checksum);
		}

		private Key(byte[] digest, String checksum) {
			if (digest.length != 32) {
				throw new IllegalArgumentException("Invalid digest length: expected 32 bytes");
			}
			this.digest =  Arrays.copyOf(digest, 32);
			this.checksum = checksum;
		}

		public byte[] getDigest() {
			return Arrays.copyOf(this.digest, 32);
		}

		public String getChecksum() {
			return this.checksum;
		}

		public boolean verify(byte[] hash) {
			return MessageDigest.isEqual(digest, hash);
		}
	}
}
