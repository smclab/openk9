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

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;

/**
 * A specialized version of the {@link AuthenticationWebFilter} for the
 * {@code ApiKey} credential scheme, that use the same logic.
 * <p>
 * Authentication failures are delegated to
 * {@link ApiKeyAuthenticationEntryPoint} so that every {@code 401} on the API
 * key branch carries an {@code ApiKey} {@code WWW-Authenticate} challenge.
 */
public class ApiKeyAuthenticationFilter extends AuthenticationWebFilter {

	private static final ApiKeyAuthenticationConverter AUTHENTICATION_CONVERTER =
		new ApiKeyAuthenticationConverter();

	private static final ServerAuthenticationEntryPointFailureHandler
		FAILURE_HANDLER = new ServerAuthenticationEntryPointFailureHandler(
			new ApiKeyAuthenticationEntryPoint());

	public ApiKeyAuthenticationFilter(
		ReactiveAuthenticationManager authenticationManager) {

		super(authenticationManager);
		setServerAuthenticationConverter(AUTHENTICATION_CONVERTER);
		setAuthenticationFailureHandler(FAILURE_HANDLER);
	}
}
