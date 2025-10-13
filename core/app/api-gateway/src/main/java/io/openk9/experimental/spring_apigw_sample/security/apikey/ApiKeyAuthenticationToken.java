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

package io.openk9.experimental.spring_apigw_sample.security.apikey;

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * An {@link Authentication} that contains an Api Key.
 * <p>
 * Used by {@link ApiKeyAuthenticationFilter} to prepare an authentication attempt
 * and supported by {@link ApiKeyAuthenticationManager}.
 *
 */
@Getter
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

	private final String apiKey;
	private final String tenantId;

	/**
	 * Create a <strong>not authenticated</strong> {@code ApiKeyAuthenticationToken}
	 * <p>
	 *  using the provided parameter(s)
	 *
	 * @param tenantId - the tenant id
	 * @param apiKey - the api key
	 */
	public ApiKeyAuthenticationToken(
		String tenantId, String apiKey) {

		super(Collections.emptyList());

		this.tenantId = tenantId;
		this.apiKey = apiKey;
		setAuthenticated(false);
	}

	/**
	 * Create an <strong>authenticated</strong> {@code ApiKeyAuthenticationToken}
	 * <p>
	 * using the provided parameter(s)
	 *
	 * @param tenantId - the tenant id
	 * @param apiKey - the API Key
	 * @param authorities the collection of the authorities associated to this API Key
	 */
	public ApiKeyAuthenticationToken(
		String tenantId, String apiKey,
		Collection<? extends GrantedAuthority> authorities) {

		super(authorities);

		this.tenantId = tenantId;
		this.apiKey = apiKey;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return apiKey;
	}

	@Override
	public Object getPrincipal() {
		return apiKey;
	}

}
