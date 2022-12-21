package io.openk9.datasource.searcher.util;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
		return jwt == null || jwt.isBlank()
			? EMPTY
			: new JsonObject(jwt).mapTo(JWT.class);
	}

	private static final JWT EMPTY = new JWT();

}
