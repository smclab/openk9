package io.openk9.auth.keycloak.api;

import java.util.Map;

public class UserInfoResponse
	extends EitherResponse<UserInfo, Map<String, Object>> {

	public UserInfoResponse(
		UserInfo userInfo, Map<String, Object> rest) {
		super(userInfo, rest);
	}
	
}
