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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.zip.CRC32;
import jakarta.annotation.Nullable;

/**
 * Utility class for generating and validating API keys with optional
 * CRC-32 checksum suffixes.
 * <p>
 * Features:
 * <ul>
 *   <li>Random API key generation with a prefix and random body bytes</li>
 *   <li>Optional CRC-32 checksum suffix for detecting accidental corruption</li>
 *   <li>Utility helpers for conversion and checksum validation</li>
 * </ul>
 * <p>
 * API keys are ASCII strings composed of:
 * <ul>
 *   <li>a prefix (e.g., {@code "sk"})</li>
 *   <li>hex-encoded random bytes (the key body), separated by an underscore</li>
 *   <li>an optional CRC-32 checksum suffix, separated by another underscore</li>
 * </ul>
 * <p>
 * Example formats:
 * <ul>
 *   <li>{@code sk_dead020019934beef} — plain API key</li>
 *   <li>{@code sk_dead020019934beef_039ab823} — with checksum suffix</li>
 * </ul>
 * <p>
 * All random values are generated with {@link SecureRandom}.
 */
public class ApiKeys {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final HexFormat HEX = HexFormat.of();
	public static final String DEFAULT_KEY_PREFIX = "sk";
	public static final int DEFAULT_RANDOM_BYTES = 32;
	public static final int DEFAULT_SALT_BYTES = 16;
	public static final char KEY_PART_SEPARATOR = '_';

	public static void main(String[] args) {
		var apiKey = ApiKeys.generateApiKeyWithChecksum();

		// print api key in clear
		System.out.println(apiKey);

		// print api key's digest
		System.out.println(HEX.formatHex(sha256(apiKey)));
	}

	// ========================================================================
	// BASIC
	// ========================================================================

	/**
	 * Produces an API Key with a defined prefix and an arbitrary number of
	 * random bytes for the body of this key.
	 * <p>
	 * Format: {@code prefix_randomBytesHexString}
	 * <br>
	 * Example: {@code sk_dead020019934beef}
	 *
	 * @param prefix the prefix applied to the API Key
	 * @param randomBytesSize the number of random bytes in the key body
	 * @return the generated API key string
	 */
	public static String generateApiKey(@Nullable String prefix, int randomBytesSize) {
		var result = new StringBuilder();

		if (prefix != null && !prefix.isBlank()) {
			result.append(prefix);
		}
		else {
			result.append(DEFAULT_KEY_PREFIX);
		}

		result.append(KEY_PART_SEPARATOR);

		// adds an arbitrary number of random bytes that will add entropy
		byte[] randomBytes = new byte[randomBytesSize > 0 ? randomBytesSize : DEFAULT_RANDOM_BYTES];
		RANDOM.nextBytes(randomBytes);

		result.append(HEX.formatHex(randomBytes));

		return result.toString();
	}

	/**
	 * Computes a SHA-256 digest of the given secret.
	 *
	 * @param secret the input secret
	 * @return a {@code byte[]} containing the digest
	 */
	public static byte[] sha256(String secret) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = toBytes(secret);
			return md.digest(bytes);
		}
		catch (NoSuchAlgorithmException e) {
			throw new AssertionError("SHA-256 not available", e);
		}
	}

	// ========================================================================
	// CHECKSUM
	// ========================================================================

	/**
	 * Generates an API key with the default prefix and default number
	 * of random body bytes, and appends a CRC-32 checksum suffix.
	 * <p>
	 * Format: {@code prefix_randomBytesHexString_crc32HexString}
	 * <br>
	 * Example: {@code sk_dead020019934beef_039ab823}
	 *
	 * @return the API key string including the checksum suffix
	 */
	public static String generateApiKeyWithChecksum() {
		return appendChecksum(generateApiKey(null, 0));
	}

	/**
	 * Generates an API key with the default prefix, a custom number of
	 * random body bytes, and appends a CRC-32 checksum suffix.
	 * <p>
	 * Format: {@code prefix_randomBytesHexString_crc32HexString}
	 * <br>
	 * Example: {@code sk_dead020019934beef_039ab823}
	 *
	 * @param randomBytesSize the number of random bytes in the key body
	 * @return the API key string including the checksum suffix
	 */
	public static String generateApiKeyWithChecksum(int randomBytesSize) {
		return appendChecksum(generateApiKey(null, randomBytesSize));
	}

	/**
	 * Generates an API key with the given prefix, the default number
	 * of random body bytes, and appends a CRC-32 checksum suffix.
	 * <p>
	 * Format: {@code prefix_randomBytesHexString_crc32HexString}
	 * <br>
	 * Example: {@code sk_dead020019934beef_039ab823}
	 *
	 * @param prefix the prefix applied to the API key
	 * @return the API key string including the checksum suffix
	 */
	public static String generateApiKeyWithChecksum(String prefix) {
		return appendChecksum(generateApiKey(prefix, 0));
	}

	/**
	 * Generates an API key with the given prefix, a custom number of
	 * random body bytes, and appends a CRC-32 checksum suffix.
	 * <p>
	 * Format: {@code prefix_randomBytesHexString_crc32HexString}
	 * <br>
	 * Example: {@code sk_dead020019934beef_039ab823}
	 *
	 * @param prefix the prefix applied to the API key
	 * @param randomBytesSize the number of random bytes in the key body
	 * @return the API key string including the checksum suffix
	 */
	public static String generateApiKeyWithChecksum(String prefix, int randomBytesSize) {
		return appendChecksum(generateApiKey(prefix, randomBytesSize));
	}

	/**
	 * Appends a CRC-32 checksum to the given API key string.
	 * <p>
	 * The checksum is computed over the provided string and appended
	 * using the standard separator.
	 * <p>
	 * Format: {@code input_crc32HexString}
	 *
	 * @param secret the API key string without a checksum
	 * @return the API key string with the appended checksum
	 */
	public static String appendChecksum(String secret) {
		String checksum = checksumHex(secret);
		return secret + KEY_PART_SEPARATOR + checksum;
	}

	/**
	 * Verifies that the given API key string ends with a valid CRC-32 checksum suffix.
	 * <p>
	 * The key is split at the last underscore into the "prefix+body" portion
	 * and the checksum suffix. A CRC-32 checksum is recomputed from the prefix+body
	 * and compared against the suffix.
	 * <p>
	 * This method returns {@code false} if:
	 * <ul>
	 *   <li>the checksum suffix is missing or empty</li>
	 *   <li>the checksum does not match</li>
	 * </ul>
	 * Example:
	 * <pre>{@code
	 * String raw = ApiKeys.generateApiKey("sk", 16);
	 * String withChecksum = ApiKeys.appendChecksum(raw);
	 * assertTrue(ApiKeys.verifyChecksum(withChecksum));
	 * }</pre>
	 *
	 * @param apiKey the API key string in format {@code prefix_body_checksum}
	 * @return {@code true} if the checksum suffix is valid, {@code false} otherwise
	 */
	public static boolean verifyChecksum(String apiKey) {
		int idx = apiKey.lastIndexOf(KEY_PART_SEPARATOR);
		if (idx == -1 || idx == apiKey.length() - 1) {
			return false;
		}
		String withoutChecksum = apiKey.substring(0, idx);
		String checksum = apiKey.substring(idx + 1);

		return checksumHex(withoutChecksum).equals(checksum);
	}

	/**
	 * Create a checksum for the input string using CRC-32 algorithm.
	 *
	 * @param input the input string
	 * @return the hexString representation of the checksum
	 */
	public static String checksumHex(String input) {
		return Long.toHexString(checksum(input));
	}

	/**
	 * Create a checksum for the input string using CRC-32 algorithm.
	 *
	 * @param input the input string
	 * @return the value of the checksum
	 */
	public static long checksum(String  input) {
		CRC32 crc32 = new CRC32();

		byte[] bytes = toBytes(input);
		crc32.update(bytes);

		return crc32.getValue();
	}

	// ====================================================================
	// UTILS
	// ====================================================================

	/**
	 * Convert the given API key string into a byte array using the US-ASCII charset.
	 * <p>
	 * API keys in this system consist only of ASCII characters (hex digits and
	 * underscores), so US-ASCII is sufficient and guarantees a 1:1 mapping
	 * between characters and bytes. This makes the conversion deterministic and
	 * safe for hashing or checksum operations.
	 *
	 * @param secret the API key string
	 * @return the US-ASCII encoded byte representation of the string
	 */
	private static byte[] toBytes(String secret) {
		return secret.getBytes(StandardCharsets.US_ASCII);
	}

	private ApiKeys() {}

}
