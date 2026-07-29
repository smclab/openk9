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

package io.openk9.searcher.client.dto;

import java.util.List;
import java.util.Locale;

/**
 * Validates the optional inline {@code media} carried by search tokens.
 *
 * <p>The rules live here, in the module both the REST surface and the gRPC
 * surface depend on, because the searcher is not the only caller that can put a
 * media on a token: the RAG modules talk to the {@code QueryParser} gRPC
 * service directly, without passing through the REST resource. Each caller maps
 * {@link InvalidQueryMediaException} to the error its protocol expects, but
 * neither restates the rules.
 */
public class QueryMediaValidator {

	/**
	 * Rejects any token whose media violates the query media rules, using the
	 * default size limit.
	 *
	 * @param tokens the search tokens to validate; {@code null} is a no-op
	 * @throws InvalidQueryMediaException on the first violation found
	 */
	public static void validate(List<ParserSearchToken> tokens) {
		validate(tokens, MEDIA_MAX_SIZE_BYTES);
	}

	/**
	 * Rejects any token whose media violates the query media rules.
	 *
	 * <p>A media is accepted only on a KNN token, with an {@code image/*}
	 * content type, non-empty bytes and a size within the given limit.
	 *
	 * @param tokens the search tokens to validate; {@code null} is a no-op
	 * @param maxSizeBytes the maximum size of the media bytes; a value of zero
	 * or less disables the size check
	 * @throws InvalidQueryMediaException on the first violation found
	 */
	public static void validate(
		List<ParserSearchToken> tokens, long maxSizeBytes) {

		if (tokens == null) {
			return;
		}

		for (ParserSearchToken token : tokens) {

			var media = token.getMedia();

			if (media == null) {
				continue;
			}

			if (!KNN_TOKEN_TYPE.equals(token.getTokenType())) {
				throw new InvalidQueryMediaException(
					"media is only supported on KNN tokens, got tokenType="
						+ token.getTokenType());
			}

			var contentType = media.getContentType();

			if (contentType == null || !contentType
				.toLowerCase(Locale.ROOT)
				.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {

				throw new InvalidQueryMediaException(
					"media.contentType must be image/*, got " + contentType);
			}

			var data = media.getData();

			if (data == null || data.length == 0) {
				throw new InvalidQueryMediaException(
					"media.data must not be empty");
			}

			if (maxSizeBytes > 0 && data.length > maxSizeBytes) {
				throw new InvalidQueryMediaException(
					"media exceeds the maximum allowed size of "
						+ maxSizeBytes
						+ " bytes");
			}
		}
	}

	public static final long MEDIA_MAX_SIZE_BYTES = 2 * 1024 * 1024;

	private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";
	private static final String KNN_TOKEN_TYPE = "KNN";

}
