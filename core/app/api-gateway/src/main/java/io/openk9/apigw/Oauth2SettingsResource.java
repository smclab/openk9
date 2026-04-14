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

package io.openk9.apigw;

import java.net.URI;

import io.openk9.apigw.security.Tenant;
import io.openk9.apigw.security.TenantSecurityService;
import io.openk9.apigw.security.oauth2.OAuth2Settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/oauth2")
public class Oauth2SettingsResource {

	@Autowired
	TenantSecurityService tenantSecurityService;

	/**
	 * Returns the OAuth2/OIDC settings for the current tenant
	 * as a JSON object with generic OIDC fields.
	 *
	 * @param exchange the current server exchange
	 * @return the tenant OAuth2 settings, or empty if not configured
	 */
	@GetMapping(value = "/settings", produces = "application/json")
	public Mono<OAuth2Settings> settings(ServerWebExchange exchange) {

		return tenantSecurityService.getOAuth2Settings(exchange);
	}

	/**
	 * Returns the OAuth2 settings as JavaScript variables for
	 * legacy Keycloak and new OIDC consumption.
	 *
	 * @param request the current HTTP request
	 * @return a JavaScript snippet setting {@code window.*} variables
	 * @deprecated use {@link #settings(ServerWebExchange)} instead
	 */
	@Deprecated
	@GetMapping(value = "/settings.js", produces = "text/javascript")
	public Mono<String> settingsJs(ServerHttpRequest request) {
		URI requestURI = request.getURI();
		String requestHost = requestURI.getHost();

		return tenantSecurityService.getTenantId(requestHost)
			.flatMap(tenantId -> tenantSecurityService
				.getTenantAggregate(tenantId)
				.map(Oauth2SettingsResource::encodeJs));
	}

	private static String encodeJs(Tenant tenant) {

		if (tenant.oauth2Settings() == null 
		|| tenant.oauth2Settings().issuerUri() == null 
		|| tenant.oauth2Settings().issuerUri().isBlank()) {

			// Returns an empty set of variables
			return """
				window.KEYCLOAK_URL ='';
				window.KEYCLOAK_REALM ='';
				window.KEYCLOAK_CLIENT_ID ='';
				window.ISSUER_URI ='';
				window.CLIENT_ID ='';
				window.CLIENT_SECRET ='';
				""";
		}

		String tenantId = tenant.tenantId();
		String issuerUri = tenant.oauth2Settings().issuerUri();

		// Get keycloakUrl from issuerUri 
		int slashIndex = issuerUri.indexOf('/', 8);
		String keycloakUrl = slashIndex != -1 
			? issuerUri.substring(0, slashIndex) 
			: issuerUri;

		String clientId = tenant.oauth2Settings().clientId();
		String clientSecret = tenant.oauth2Settings().clientSecret();

		// Returns legacy and new js variables
		return String.format("""
			// legacy keycloak variables
			window.KEYCLOAK_URL ='%s';
			window.KEYCLOAK_REALM ='%s';
			window.KEYCLOAK_CLIENT_ID ='%s';
			// new oidc variables
			window.ISSUER_URI ='%s';
			window.CLIENT_ID ='%s';
			window.CLIENT_SECRET ='%s';
			""",
			keycloakUrl, tenantId, clientId,
		 	issuerUri, clientId, clientSecret);
	}

}
