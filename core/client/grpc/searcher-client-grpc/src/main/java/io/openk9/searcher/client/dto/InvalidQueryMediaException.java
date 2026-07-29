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

/**
 * Raised by {@link QueryMediaValidator} when a search token carries an
 * unacceptable inline media.
 *
 * <p>The exception carries only the reason: it is up to each caller to turn it
 * into the error its protocol expects, an HTTP {@code 400} on the REST surface
 * and an {@code INVALID_ARGUMENT} on the gRPC one. A dedicated type keeps the
 * violation distinguishable from the other {@link IllegalArgumentException}s
 * raised while a request is being parsed.
 */
public class InvalidQueryMediaException extends RuntimeException {

	/**
	 * Creates the exception describing a single media violation.
	 *
	 * @param message the reason to report to the caller
	 */
	public InvalidQueryMediaException(String message) {
		super(message);
	}

}
