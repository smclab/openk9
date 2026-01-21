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

import io.openk9.apigw.security.apikey.ApiKeyAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * An enumeration of the authentication methods that the Api Gateway supports.
 * <p>
 * There is an {@link Authentication} associated to each of that.
 */
public enum AuthorizationSchemeToken {

	API_KEY(ApiKeyAuthenticationToken.class),
	OAUTH2(JwtAuthenticationToken.class),
	NO_AUTH(NoAuthenticationToken.class);

	private final Class<? extends Authentication> authenticationTokenClass;

	<T extends Authentication> AuthorizationSchemeToken(Class<T> authenticationTokenClass) {
		this.authenticationTokenClass = authenticationTokenClass;
	}

	public <T extends Authentication> boolean match(Class<T> other) {
		return authenticationTokenClass.isAssignableFrom(other);
	}
}
