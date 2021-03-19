package io.openk9.auth.keycloak;

import lombok.Data;

import java.util.Map;

@Data
public class KeycloakEvent {

	private long time;
	private String type;
	private String realmId;
	private String clientId;
	private String userId;
	private String sessionId;
	private String ipAddress;
	private String error;
	private Map<String, Object> details;

}
