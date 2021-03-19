package io.openk9.auth.keycloak.web.request;

import lombok.Data;

@Data
public class LogoutRequest {
	private String accessToken;
	private String refreshToken;
}
