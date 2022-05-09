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

package io.openk9.auth.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class UserInfo {
	@JsonProperty("exp")
	private long exp;
	@JsonProperty("iat")
	private long iat;
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
	@JsonProperty("name")
	private String name;
	@JsonProperty("given_name")
	private String givenName;
	@JsonProperty("family_name")
	private String familyName;
	@JsonProperty("preferred_username")
	private String preferredUsername;
	@JsonProperty("email")
	private String email;
	@JsonProperty("email_verified")
	private boolean emailVerified;
	@JsonProperty("acr")
	private String acr;
	@JsonProperty("realm_access")
	private Map<String, List<String>> realmAccess;
	@JsonProperty("resource_access")
	private Map<String, Map<String, List<String>>> resourceAccess;
	@JsonProperty("scope")
	private String scope;
	@JsonProperty("client_id")
	private String clientId;
	@JsonProperty("username")
	private String username;
	@JsonProperty("active")
	private boolean active;
}
