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

"""This module provides JWT token decode.

It includes functions to decode JWT tokens and handle unauthorized responses.
"""

from fastapi import HTTPException, status
from simple_jwt import jwt

from app.utils.logger import logger


def decode_token(token: str) -> dict:
    """Decode a JWT token and return its payload.

    Args:
        token (str): The JWT token string to decode.

    Returns:
        dict: The decoded token payload as a dictionary.

    Raises:
        Exception: If the token is malformed or cannot be decoded.

    Example:
        >>> token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        >>> decode_token(token)
        {'user_id': 123, 'exp': 1672531200, 'iat': 1672527600}
    """

    try:
        decoded_token = jwt.decode(token)
        return decoded_token
    except Exception as e:
        logger.error(f"Error decoding token: {str(e)}")
        unauthorized_response()


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
