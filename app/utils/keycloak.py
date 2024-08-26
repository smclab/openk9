import requests
from keycloak import KeycloakOpenID

from app.external_services.grpc.grpc_client import get_tenant_manager_configuration


class Keycloak:

    def verify_token(grpc_host, virtualHost, token) -> dict:
        try:
            keycloak_info = get_tenant_manager_configuration(grpc_host, virtualHost)

            keycloak_url = keycloak_info["server_url"]
            keycloak_client_id = keycloak_info["client_id"]
            keycloak_realm = keycloak_info["realm_name"]

            keycloak_openid = KeycloakOpenID(
                server_url=keycloak_url,
                client_id=keycloak_client_id,
                realm_name=keycloak_realm,
            )

            userinfo = keycloak_openid.userinfo(token=token)

            return userinfo
        except:
            return {}
