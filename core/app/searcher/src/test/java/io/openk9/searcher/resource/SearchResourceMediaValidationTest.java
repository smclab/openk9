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

package io.openk9.searcher.resource;

import java.util.List;

import io.openk9.searcher.client.dto.ParserSearchToken;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchResourceMediaValidationTest {

	private static final long MAX_SIZE = 1024;
	private static final byte[] IMAGE_BYTES = {1, 2, 3};

	@Test
	void should_reject_media_on_a_non_knn_token() {

		// 1. a TEXT token carrying media
		var token = ParserSearchToken.builder()
			.tokenType("TEXT")
			.media(new ParserSearchToken.Media(IMAGE_BYTES, "image/png"))
			.build();

		// 2. validation fails with HTTP 400 naming the violation
		assertBadRequest(
			List.of(token),
			"media is only supported on KNN tokens, got tokenType=TEXT",
			MAX_SIZE);
	}

	@Test
	void should_reject_media_with_a_non_image_content_type() {

		// 1. a KNN token whose media is not an image
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media(IMAGE_BYTES, "application/pdf"))
			.build();

		// 2. validation fails with HTTP 400 naming the violation
		assertBadRequest(
			List.of(token),
			"media.contentType must be image/*, got application/pdf",
			MAX_SIZE);
	}

	@Test
	void should_reject_empty_media_data() {

		// 1. a KNN token whose media carries no bytes
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media(new byte[0], "image/png"))
			.build();

		// 2. validation fails with HTTP 400 naming the violation
		assertBadRequest(
			List.of(token), "media.data must not be empty", MAX_SIZE);
	}

	@Test
	void should_reject_media_above_the_size_limit() {

		// 1. a KNN token whose media exceeds the limit
		var oversized = new byte[] {1, 2, 3, 4, 5};
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media(oversized, "image/png"))
			.build();

		// 2. validation fails with HTTP 400 against a tiny limit
		assertBadRequest(
			List.of(token),
			"media exceeds the maximum allowed size of 2 bytes",
			2);
	}

	@Test
	void should_accept_a_valid_knn_image_token() {

		// 1. a well-formed KNN image token
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.values(List.of("a cat"))
			.media(new ParserSearchToken.Media(IMAGE_BYTES, "image/png"))
			.build();

		// 2. validation passes
		Assertions.assertDoesNotThrow(
			() -> SearchResource.validateMediaTokens(List.of(token), MAX_SIZE));
	}

	@Test
	void should_be_a_no_op_without_media_or_tokens() {

		// 1. a token without media and a null list are both accepted
		var token = ParserSearchToken.builder()
			.tokenType("TEXT")
			.values(List.of("plain text"))
			.build();

		// 2. neither triggers validation
		Assertions.assertDoesNotThrow(
			() -> SearchResource.validateMediaTokens(List.of(token), MAX_SIZE));
		Assertions.assertDoesNotThrow(
			() -> SearchResource.validateMediaTokens(null, MAX_SIZE));
	}

	/**
	 * Asserts the violation answers {@code 400} and that the reason reaches the
	 * caller in the response body, not just the exception.
	 */
	private void assertBadRequest(
		List<ParserSearchToken> tokens, String expectedDetails, long maxSize) {

		var exception = Assertions.assertThrows(
			WebApplicationException.class,
			() -> SearchResource.validateMediaTokens(tokens, maxSize));

		var response = exception.getResponse();

		Assertions.assertEquals(400, response.getStatus());

		Assertions.assertInstanceOf(JsonObject.class, response.getEntity());

		Assertions.assertEquals(
			expectedDetails,
			((JsonObject) response.getEntity()).getString("details"));
	}

}
