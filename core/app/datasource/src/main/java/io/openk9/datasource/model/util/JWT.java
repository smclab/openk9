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

package io.openk9.datasource.model.util;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JWT {
	@JsonProperty("exp")
	private long exp;
	@JsonProperty("iat")
	private long iat;
	@JsonProperty("auth_time")
	private long authTime;
	@JsonProperty("jti")
	private String jti;
	@JsonProperty("iss")
	private String iss;
	@JsonProperty("aud")
	private String aud;
	@JsonProperty("sub")
	private String sub;
	@JsonProperty("typ")
	private String typ;
	@JsonProperty("azp")
	private String azp;
	@JsonProperty("session_state")
	private String sessionState;
	@JsonProperty("acr")
	private String acr;
	@JsonProperty("allowed-origins")
	private List<String> allowedOrigins;
	@JsonProperty("realm_access")
	private Map<String, List<String>> realmAccess;
	@JsonProperty("resource_access")
	private Map<String, Map<String, List<String>>> resourceAccess;
	@JsonProperty("scope")
	private String scope;
	@JsonProperty("sid")
	private String sid;
	@JsonProperty("email_verified")
	private boolean emailVerified;
	@JsonProperty("preferred_username")
	private String preferredUsername;
	@JsonProperty("given_name")
	private String givenName;
	@JsonProperty("family_name")
	private String familyName;
	@JsonProperty("email")
	private String email;

	public boolean isEmpty() {
		return this == EMPTY;
	}

	public static JWT of(String jwt) {

		if (jwt == null || jwt.isBlank()) {
			return EMPTY;
		}

		if (jwt.contains(".")) {
			String[] split = jwt.split("\\.");
			String payload = split[1];
			return new JsonObject(
				new String(Base64.getDecoder().decode(payload))
			)
				.mapTo(JWT.class);
		}
		else {
			return new JsonObject(jwt).mapTo(JWT.class);
		}

	}

	private static final JWT EMPTY = new JWT();

}
