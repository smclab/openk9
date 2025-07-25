#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

"""This module provides authentication utilities using Keycloak for token verification.

It includes functions to verify JWT tokens against a Keycloak server, handle unauthorized responses,
and communicate with gRPC services to retrieve tenant-specific configurations.
"""

import os

from dotenv import load_dotenv
from fastapi import HTTPException, status
from keycloak import KeycloakOpenID
from keycloak.exceptions import KeycloakError, KeycloakInvalidTokenError

from app.external_services.grpc.grpc_client import get_tenant_manager_configuration
from app.utils.logger import logger

load_dotenv()

KEYCLOAK_URL = os.getenv("KEYCLOAK_URL")


def verify_token(grpc_host: str, virtual_host: str, token: str) -> dict:
    """Verify the validity of a Keycloak JWT token using tenant-specific configuration.

    Retrieves the tenant's Keycloak configuration (client ID and realm) via gRPC, then
    verifies the provided token with the Keycloak server. Returns user information if valid.

    Args:
        grpc_host (str): Hostname or IP address of the gRPC server for tenant configuration.
        virtual_host (str): Virtual host identifier for the tenant.
        token (str): The JWT token to be verified.

    Returns:
        dict: User information dictionary from Keycloak if token is valid.
            Returns an empty dictionary on errors (invalid token, Keycloak issues, etc.).

    Example:
        >>> user_info = verify_token("grpc.example.com", "tenant_vhost", "bearer_token")
        >>> if not user_info:
        ...     unauthorized_response()

    Note:
        Depends on environment variable `KEYCLOAK_URL` for Keycloak server URL.
        All exceptions are caught and logged internally.
    """
    try:
        keycloak_info = get_tenant_manager_configuration(grpc_host, virtual_host)

        keycloak_client_id = keycloak_info["client_id"]
        keycloak_realm = keycloak_info["realm_name"]

        keycloak_openid = KeycloakOpenID(
            server_url=KEYCLOAK_URL,
            client_id=keycloak_client_id,
            realm_name=keycloak_realm,
        )

        userinfo = keycloak_openid.userinfo(token=token)
        return userinfo

    except KeycloakInvalidTokenError as e:
        logger.error(f"Invalid token: {e}")
    except KeycloakError as e:
        logger.error(f"Keycloak error: {e}")
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
    return {}


def unauthorized_response() -> None:
    """Raise an HTTP 401 Unauthorized exception indicating invalid token.

    Raises:
        HTTPException: Always raises a 401 error with detail and authentication header.

    Example:
        >>> unauthorized_response()
        Traceback (most recent call last):
        ...
        fastapi.exceptions.HTTPException: 401 Unauthorized: Invalid token.
    """
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid token.",
        headers={"WWW-Authenticate": "Bearer"},
    )
