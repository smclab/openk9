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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.openk9.experimental.spring_apigw_sample.security.TenantIdResolverFilter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A strategy for resolving {@link ApiKeyAuthenticationToken}s
 * from the {@link ServerWebExchange}.
 */
public class ApiKeyAuthenticationConverter implements ServerAuthenticationConverter {

	// TODO: parser of apikeys
	private static final Pattern authorizationPattern = Pattern.compile(
		"^ApiKey (?<apikey>[a-zA-Z0-9-._~+/]+=*)$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {

		return Mono.create(sink -> {

			// get the required objects to evaluate the request
			ServerHttpRequest request = exchange.getRequest();

			// tenant has to be recognized
			String tenantId = TenantIdResolverFilter.getTenantId(exchange);
			if (tenantId == null) {
				sink.success();
			}

			HttpHeaders headers = request.getHeaders();
			var authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);

			// authorization scheme has to be ApiKey
			if (!StringUtils.startsWithIgnoreCase(authorization, "apikey")) {
				sink.success();
			}

			Matcher matcher = authorizationPattern.matcher(authorization);
			if (!matcher.matches()) {
				sink.error(new ApiKeyMalformedException("ApiKey is malformed"));
			}

			var apiKey = matcher.group("apikey");

			sink.success(new ApiKeyAuthenticationToken(tenantId, apiKey));

		});
	}

}
