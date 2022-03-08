package io.openk9.api.aggregator.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class LoginResponseDTO {
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private int expiresIn;
	@JsonProperty("refresh_expires_in")
	private int refreshExpiresIn;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("not-before-policy")
	private int notBeforePolicy;
	@JsonProperty("session_state")
	private String sessionState;
	@JsonProperty("scope")
	private String scope;
}