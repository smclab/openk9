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
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/oauth2")
public class Oauth2SettingsResource {

	@Autowired
	TenantSecurityService tenantSecurityService;

    @GetMapping("/settings")
	public Mono<OAuth2Settings> settings(ServerHttpRequest request) {
		URI requestURI = request.getURI();
		String requestHost = requestURI.getHost();

		return tenantSecurityService.getTenantId(requestHost)
			.flatMap(tenantId -> tenantSecurityService
				.getTenantAggregate(tenantId)
				.map(Tenant::oauth2Settings)
			);
	}

    @GetMapping(value = "/settings.js", produces = "text/javascript")
    public Mono<String> settingsJs(ServerHttpRequest request) {
		URI requestURI = request.getURI();
		String requestHost = requestURI.getHost();

		return tenantSecurityService.getTenantId(requestHost)
			.flatMap(tenantId -> tenantSecurityService
				.getTenantAggregate(tenantId)
				.map(Oauth2SettingsResource::encodeOldSettingsJs)
			);
	}

	private static String encodeOldSettingsJs(Tenant tenant) {

		String issuerUri = tenant.oauth2Settings().issuerUri();
		var keycloakUrl = issuerUri.substring(0, issuerUri.indexOf('/', 8));


		return String.format(
			"""
				window.KEYCLOAK_URL ='%s';
				window.KEYCLOAK_REALM ='%s';
				window.KEYCLOAK_CLIENT_ID ='%s';
			""",
			keycloakUrl,
			tenant.tenantId(),
			tenant.oauth2Settings().clientId()
		);
	}

	private static String encodeNewSettingsJs(OAuth2Settings oauth2Settings) {

		return String.format(
			"""
				window.ISSUER_URI ='%s';
				window.CLIENT_ID ='%s';
			""",
			oauth2Settings.issuerUri(),
			oauth2Settings.clientId()
		);

	}

}