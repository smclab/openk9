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

import io.openk9.apigw.security.ApiRoute;
import io.openk9.apigw.security.AuthorizationHeaderFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class WebFilterChainConfiguration {

	@Autowired
	WebFilter tenantResolverFilter;
	@Autowired
	WebFilter apiKeyAuthFilter;
	@Autowired
	ReactiveAuthenticationManagerResolver<ServerWebExchange> jwtAuthManagerResolver;
	@Autowired
	ReactiveAuthorizationManager<AuthorizationContext> authzManager;
	@Value("${io.openk9.apigw.reject-basic-auth:true}")
	boolean rejectBasicAuth;

	@Bean
	@Order(0)
	SecurityWebFilterChain actuatorSecurityFilterChain(
		ServerHttpSecurity http
	) {

		return http
			.securityMatcher(
				EndpointRequest.toAnyEndpoint())
			.authorizeExchange(
				auth -> auth.anyExchange().permitAll())
			.build();
	}

	@Bean
	@Order(1)
	@ConditionalOnProperty(
		name = "io.openk9.apigw.reject-basic-auth",
		havingValue = "true",
		matchIfMissing = true)
	SecurityWebFilterChain basicAuthFilterChain(
		ServerHttpSecurity http
	) {

		return http
			.securityMatcher(exchange -> {
				var authHeaders = exchange.getRequest()
					.getHeaders()
					.get(HttpHeaders.AUTHORIZATION);
				if (authHeaders == null) {
					return MatchResult.notMatch();
				}
				if (authHeaders.size() != 1) {
					return MatchResult.match();
				}
				String value = authHeaders.get(0);
				if (value != null
					&& value.regionMatches(
						true, 0, "basic ", 0, 6)) {
					return MatchResult.match();
				}
				return MatchResult.notMatch();
			})
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.addFilterAt(
				new AuthorizationHeaderFilter(),
				SecurityWebFiltersOrder.AUTHENTICATION)
			.authorizeExchange(auth ->
				auth.anyExchange().denyAll())
			.build();
	}

	@Bean
	@Order(2)
	SecurityWebFilterChain defaultFilterChain(
		ServerHttpSecurity http
	) {

		http
			.addFilterBefore(
				tenantResolverFilter,
				SecurityWebFiltersOrder.CORS)
			.csrf(ServerHttpSecurity.CsrfSpec::disable);

		if (rejectBasicAuth) {
			http
				.addFilterAt(
					apiKeyAuthFilter,
					SecurityWebFiltersOrder.AUTHENTICATION)
				.oauth2ResourceServer(oauth2 -> oauth2
					.authenticationManagerResolver(
						jwtAuthManagerResolver))
				.authorizeExchange(authorize -> authorize
					.pathMatchers(ApiRoute.antPatterns())
					.access(authzManager));
		}
		else {
			http.authorizeExchange(authorize -> authorize
				.anyExchange().permitAll());
		}

		return http.build();
	}

}
