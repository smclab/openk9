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

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * An implemenation of {@link ReactiveAuthorizationManager} that relies on the
 * {@link TenantSecurityService#isAuthorized(Mono, ServerWebExchange)} method to evaluate
 * the authorization of a request.
 *
 */
@RequiredArgsConstructor
public class TenantSecurityAuthorizationManager
	implements ReactiveAuthorizationManager<AuthorizationContext> {

	private final TenantSecurityService service;

	@Override
	public Mono<AuthorizationDecision> check(
		Mono<Authentication> authentication, AuthorizationContext context) {
		return service.isAuthorized(authentication, context.getExchange())
			.map(AuthorizationDecision::new);
	}


}
