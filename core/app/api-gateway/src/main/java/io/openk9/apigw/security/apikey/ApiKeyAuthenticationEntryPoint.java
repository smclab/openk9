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

package io.openk9.apigw.security.apikey;

import io.openk9.apigw.security.ChecksumValidationException;
import io.openk9.apigw.security.ExpiredApiKeyException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * {@link ServerAuthenticationEntryPoint} for the {@code ApiKey} credential
 * scheme.
 * <p>
 * Every API key authentication failure is answered with {@code 401} and an
 * RFC 7235 §4.1 compliant {@code WWW-Authenticate} challenge that carries a
 * stable, enumerated {@code error} attribute (modelled on RFC 6750 §3), so
 * callers can distinguish malformed, unknown, expired and checksum failures:
 * <pre>
 * WWW-Authenticate: ApiKey realm="openk9", error="&lt;code&gt;",
 *                   error_description="&lt;human readable&gt;"
 * </pre>
 * The response body is intentionally empty, mirroring the OAuth2/Bearer branch.
 */
public class ApiKeyAuthenticationEntryPoint
	implements ServerAuthenticationEntryPoint {

	private static final String REALM = "openk9";

	/**
	 * Writes a {@code 401 Unauthorized} response with the {@code ApiKey}
	 * {@code WWW-Authenticate} challenge derived from the failure type.
	 *
	 * @param exchange the current exchange
	 * @param ex the authentication failure raised on the API key branch
	 * @return a {@link Mono} that completes once the response is committed
	 */
	@Override
	public Mono<Void> commence(
		ServerWebExchange exchange, AuthenticationException ex) {

		Error error = Error.from(ex);

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().set(
			HttpHeaders.WWW_AUTHENTICATE, error.toChallenge());

		return response.setComplete();
	}

	/**
	 * Closed set of {@code error} codes returned on the API key branch.
	 */
	private enum Error {

		MALFORMED_API_KEY(
			"malformed_api_key",
			"API key does not match the expected format"),
		INVALID_CHECKSUM(
			"invalid_checksum",
			"API key checksum validation failed"),
		INVALID_API_KEY(
			"invalid_api_key",
			"API key not registered for this tenant"),
		EXPIRED_API_KEY(
			"expired_api_key",
			"API key has expired"),
		INVALID_REQUEST(
			"invalid_request",
			"API key authentication failed");

		private final String code;
		private final String description;

		Error(String code, String description) {
			this.code = code;
			this.description = description;
		}

		/**
		 * Maps an API key authentication failure to its {@code error} code,
		 * falling back to {@link #INVALID_REQUEST} for any unrecognised
		 * {@link AuthenticationException}.
		 *
		 * @param ex the authentication failure raised on the API key branch
		 * @return the matching {@code error} code
		 */
		static Error from(AuthenticationException ex) {
			if (ex instanceof ApiKeyMalformedException) {
				return MALFORMED_API_KEY;
			}
			if (ex instanceof ChecksumValidationException) {
				return INVALID_CHECKSUM;
			}
			if (ex instanceof ApiKeyNotFoundException) {
				return INVALID_API_KEY;
			}
			if (ex instanceof ExpiredApiKeyException) {
				return EXPIRED_API_KEY;
			}
			return INVALID_REQUEST;
		}

		String toChallenge() {
			return String.format(
				"ApiKey realm=\"%s\", error=\"%s\", error_description=\"%s\"",
				REALM, code, description);
		}
	}
}
