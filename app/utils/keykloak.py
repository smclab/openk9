import requests
from keycloak import KeycloakOpenID

KEYKLOAK_INFO_API_URL = "https://k9-backend.openk9.io/api/datasource/oauth2/settings"


class Keycloak:

    def verify_token(token) -> dict:
        try:
            keykloak_request = requests.get(KEYKLOAK_INFO_API_URL)
            keykloak_info = keykloak_request.json()

            keykloak_url = keykloak_info["url"]
            keykloak_client_id = keykloak_info["clientId"]
            keykloak_realm = keykloak_info["realm"]

            keycloak_openid = KeycloakOpenID(
                server_url=keykloak_url,
                client_id=keykloak_client_id,
                realm_name=keykloak_realm,
            )

            userinfo = keycloak_openid.userinfo(token=token)
            return userinfo
        except:
            return {}
