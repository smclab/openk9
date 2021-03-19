package io.openk9.auth.keycloak.web.request;

import lombok.Data;

@Data
public class LoginRequest {
	private String username;
	private String password;
}
