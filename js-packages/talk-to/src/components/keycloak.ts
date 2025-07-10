import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
	url: window.KEYCLOAK_URL,
	realm: window.KEYCLOAK_REALM,
	clientId: window.KEYCLOAK_CLIENT_ID,
});
