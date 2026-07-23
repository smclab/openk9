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

import java.util.Base64;
import java.util.List;

import io.openk9.searcher.client.dto.ParserSearchToken;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchResourceMediaValidationTest {

	private static final long MAX_SIZE = 1024;
	private static final String IMAGE_BASE64 =
		Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});

	@Test
	void should_reject_media_on_a_non_knn_token() {

		// 1. a TEXT token carrying media
		var token = ParserSearchToken.builder()
			.tokenType("TEXT")
			.media(new ParserSearchToken.Media(IMAGE_BASE64, "image/png"))
			.build();

		// 2. validation fails with HTTP 400
		assertBadRequest(List.of(token));
	}

	@Test
	void should_reject_media_with_a_non_image_content_type() {

		// 1. a KNN token whose media is not an image
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media(IMAGE_BASE64, "application/pdf"))
			.build();

		// 2. validation fails with HTTP 400
		assertBadRequest(List.of(token));
	}

	@Test
	void should_reject_media_above_the_size_limit() {

		// 1. a KNN token whose decoded media exceeds the limit
		var oversized = Base64.getEncoder()
			.encodeToString(new byte[] {1, 2, 3, 4, 5});
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media(oversized, "image/png"))
			.build();

		// 2. validation fails with HTTP 400 against a tiny limit
		var exception = Assertions.assertThrows(
			WebApplicationException.class,
			() -> SearchResource.validateMediaTokens(List.of(token), 2));

		Assertions.assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void should_reject_media_with_invalid_base64() {

		// 1. a KNN token whose media data is not valid base64
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media("not+valid+base64!!", "image/png"))
			.build();

		// 2. validation fails with HTTP 400
		assertBadRequest(List.of(token));
	}

	@Test
	void should_accept_a_valid_knn_image_token() {

		// 1. a well-formed KNN image token
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.values(List.of("a cat"))
			.media(new ParserSearchToken.Media(IMAGE_BASE64, "image/png"))
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

	private void assertBadRequest(List<ParserSearchToken> tokens) {
		var exception = Assertions.assertThrows(
			WebApplicationException.class,
			() -> SearchResource.validateMediaTokens(tokens, MAX_SIZE));

		Assertions.assertEquals(400, exception.getResponse().getStatus());
	}

}
