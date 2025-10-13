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

package io.openk9.experimental.spring_apigw_sample.security;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * This is an {@link Authentication} not authenticated that is used as a fallback
 * when a route is permitted to all.
 */
public final class NoAuthenticationToken extends AbstractAuthenticationToken {

	public final static Authentication INSTANCE = new NoAuthenticationToken();

	private static final String NO_AUTH = "No-Auth";

	private NoAuthenticationToken() {
		super(Collections.emptyList());
	}

	@Override
	public Object getCredentials() {
		return NO_AUTH;
	}

	@Override
	public Object getPrincipal() {
		return NO_AUTH;
	}
}
