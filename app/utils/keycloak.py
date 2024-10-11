import os

from fastapi import HTTPException, status
from keycloak import KeycloakOpenID

from app.external_services.grpc.grpc_client import get_tenant_manager_configuration

KEYCLOAK_URL = os.getenv("KEYCLOAK_URL")


def verify_token(grpc_host, virtual_host, token) -> dict:
    """Utility function that verify keycloak token validity."""
    try:
        keycloak_info = get_tenant_manager_configuration(grpc_host, virtual_host)

        keycloak_url = KEYCLOAK_URL
        keycloak_client_id = keycloak_info["client_id"]
        keycloak_realm = keycloak_info["realm_name"]

        keycloak_openid = KeycloakOpenID(
            server_url=keycloak_url,
            client_id=keycloak_client_id,
            realm_name=keycloak_realm,
        )

        userinfo = keycloak_openid.userinfo(token=token)

        return userinfo
    except Exception:
        return {}


def unauthorized_response():
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid token.",
        headers={"WWW-Authenticate": "Bearer"},
    )
