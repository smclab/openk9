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

package io.openk9.apigw.security.oauth2;

/**
 * The OAuth2 settings of a tenant as returned to the browser.
 * <p>
 * The frontends log the user in with the Authorization Code flow and PKCE
 * on a public client, so they only need the issuer URI and the client id:
 * the client secret is left out on purpose and must never be added.
 *
 * @param issuerUri the OpenID Connect issuer URI
 * @param clientId the OAuth2 client identifier
 */
public record OAuth2SettingsResponse(
	String issuerUri,
	String clientId
) {

	/**
	 * Builds the browser view of the given settings, without the client
	 * secret.
	 *
	 * @param settings the OAuth2 settings of the tenant
	 * @return the issuer URI and the client id of the settings
	 */
	public static OAuth2SettingsResponse from(OAuth2Settings settings) {
		return new OAuth2SettingsResponse(
			settings.issuerUri(), settings.clientId());
	}

}
