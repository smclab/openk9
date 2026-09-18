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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import io.openk9.common.util.ingestion.ShardingKey;

/**
 * Opaque handle handed to an HTTP_ASYNC enricher as the {@code replyTo} of
 * its request. It addresses the {@code EnrichPipeline} entity waiting for
 * the callback (its process key) and carries a nonce that identifies the
 * single call being awaited, so that a stale or duplicated callback can be
 * told apart from the expected one.
 *
 * <p>The encoded form is URL-safe Base64 without padding, since the token
 * travels as a path segment of the callback endpoint.</p>
 *
 * @param processKey the sharding key of the pipeline entity
 * @param nonce the identifier of the awaited call
 */
public record CallbackToken(ShardingKey processKey, String nonce) {

	private static final char SEPARATOR = '|';

	public CallbackToken {
		Objects.requireNonNull(processKey, "processKey");
		Objects.requireNonNull(nonce, "nonce");
	}

	/**
	 * Creates a token for the given pipeline with a fresh random nonce.
	 *
	 * @param processKey the sharding key of the pipeline entity
	 * @return a new token
	 */
	public static CallbackToken generate(ShardingKey processKey) {
		return new CallbackToken(processKey, UUID.randomUUID().toString());
	}

	/**
	 * Parses a token previously produced by {@link #encode()}.
	 *
	 * @param value the encoded token
	 * @return the decoded token
	 * @throws IllegalArgumentException when the value is not a valid token
	 */
	public static CallbackToken decode(String value) {

		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("empty callback token");
		}

		String plain;

		try {
			plain = new String(
				Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("malformed callback token", e);
		}

		int separator = plain.lastIndexOf(SEPARATOR);

		if (separator <= 0 || separator == plain.length() - 1) {
			throw new IllegalArgumentException("malformed callback token");
		}

		var key = plain.substring(0, separator);

		// a sharding key has at least tenant and schedule
		if (key.indexOf(ShardingKey.SEPARATOR) < 0) {
			throw new IllegalArgumentException("malformed callback token");
		}

		return new CallbackToken(
			ShardingKey.fromString(key), plain.substring(separator + 1));
	}

	/**
	 * Encodes this token in its URL-safe form.
	 *
	 * @return the encoded token
	 */
	public String encode() {
		var plain = processKey.asString() + SEPARATOR + nonce;

		return Base64
			.getUrlEncoder()
			.withoutPadding()
			.encodeToString(plain.getBytes(StandardCharsets.UTF_8));
	}

}
