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

package io.openk9.apigw.security.configuration;

import io.openk9.apigw.security.TenantIdResolverFilter;
import io.openk9.apigw.security.TenantSecurityAuthorizationManager;
import io.openk9.apigw.security.TenantSecurityService;
import io.openk9.apigw.security.apikey.ApiKeyAuthenticationFilter;
import io.openk9.apigw.security.apikey.ApiKeyAuthenticationManager;
import io.openk9.apigw.security.oauth2.TenantJwtAuthenticationManagerResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebFiltersConfiguration {

	@Bean
	WebFilter tenantResolverFilter(TenantSecurityService tenantSecurityService) {

		return new TenantIdResolverFilter(tenantSecurityService);
	}

	@Bean
	AuthenticationWebFilter apiKeyAuthFilter(TenantSecurityService tenantSecurityService) {

		var apiKeyAuthenticationManager = new ApiKeyAuthenticationManager(tenantSecurityService);

		return new ApiKeyAuthenticationFilter(apiKeyAuthenticationManager);
	}

	@Bean
	@Profile("!test")
	ReactiveAuthenticationManagerResolver<ServerWebExchange> jwtAuthManagerResolver(
		TenantSecurityService tenantSecurityService) {

		return new TenantJwtAuthenticationManagerResolver(tenantSecurityService);
	}

	@Bean
	ReactiveAuthorizationManager<AuthorizationContext> authzManager(
		TenantSecurityService tenantSecurityService) {

		return new TenantSecurityAuthorizationManager(tenantSecurityService);
	}

}
