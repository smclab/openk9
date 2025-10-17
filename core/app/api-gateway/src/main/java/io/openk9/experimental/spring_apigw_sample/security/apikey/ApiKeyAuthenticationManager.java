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

import io.openk9.experimental.spring_apigw_sample.security.TenantSecurityService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApiKeyAuthenticationManager
	implements ReactiveAuthenticationManager {

	private final TenantSecurityService service;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {

		if (authentication instanceof ApiKeyAuthenticationToken apiKeyToken) {
			var tenantId = apiKeyToken.getTenantId();
			var apiKey = apiKeyToken.getApiKey();

			return service.getApiKeyPermission(apiKeyToken)
				.map(AuthorityUtils::createAuthorityList)
				.map(grantedAuthorities -> new ApiKeyAuthenticationToken(
					tenantId, apiKey, grantedAuthorities)
				)
				.cast(Authentication.class)
				.switchIfEmpty(Mono.error(() -> new ApiKeyNotFoundException(
					String.format("No registered tenantId: %s API Key: %s", tenantId, apiKey))));
		}

		return Mono.empty();
	}


}
