package io.openk9.auth.keycloak.api;

import java.util.Collections;
import java.util.Map;

public class LoginResponse
	extends EitherResponse<LoginInfo, Map<String, Object>> {

	public LoginResponse(LoginInfo loginInfo) {
		super(loginInfo, Collections.emptyMap());
	}

	public LoginResponse(Map<String, Object> rest) {
		super(null, rest);
	}

	public LoginResponse(
		LoginInfo loginInfo, Map<String, Object> rest) {
		super(loginInfo, rest);
	}
	
}
