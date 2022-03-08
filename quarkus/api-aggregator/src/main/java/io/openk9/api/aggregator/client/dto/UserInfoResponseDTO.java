package io.openk9.api.aggregator.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class UserInfoResponseDTO {
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