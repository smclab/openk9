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

import io.openk9.apigw.security.TenantSecurityService;
import io.openk9.apigw.security.oauth2.OAuth2Settings;

import org.springframework.beans.factory.annotation.Autowired;
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

}
