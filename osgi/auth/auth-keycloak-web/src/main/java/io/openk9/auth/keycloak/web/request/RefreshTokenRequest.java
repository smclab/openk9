package io.openk9.auth.keycloak.web.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class RefreshTokenRequest {
	private String refreshToken;
}
