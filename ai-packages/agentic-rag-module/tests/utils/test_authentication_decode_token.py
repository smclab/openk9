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

"""A bearer token is decoded into its claims, or refused with a 401.

Signature and expiry are verified upstream by the gateway, so the module only
reads the claims: a token signed with any key, or already expired, still
yields them. A token that is not a JWT at all is refused with the same 401
the endpoints raise when the token is missing.
"""

import time

import jwt
import pytest
from fastapi import HTTPException

from app.utils.authentication import decode_token, unauthorized_response

KEY = "a-key-the-module-never-sees-00000000"
CLAIMS = {"sub": "user-1", "realm_name": "tenant-1"}


def _token(claims, key=KEY):
    return jwt.encode(claims, key, algorithm="HS256")


def _assert_unauthorized(error):
    assert error.status_code == 401
    assert error.detail == "Invalid token."
    assert error.headers == {"WWW-Authenticate": "Bearer"}


def test_well_formed_token_yields_its_claims():
    assert decode_token(_token(CLAIMS)) == CLAIMS


def test_signature_is_not_verified_by_the_module():
    token = _token(CLAIMS, key="another-key-the-gateway-checks-00000")

    assert decode_token(token) == CLAIMS


def test_expired_token_still_yields_its_claims():
    claims = {**CLAIMS, "exp": int(time.time()) - 3600}

    assert decode_token(_token(claims)) == claims


def test_token_without_the_expected_claims_is_decoded_as_is():
    assert decode_token(_token({"iss": "gateway"})) == {"iss": "gateway"}


@pytest.mark.parametrize(
    "token", ["not-a-jwt", "", "a.b.c", "eyJhbGciOiJIUzI1NiJ9.garbage.sig"]
)
def test_malformed_token_is_refused_with_401(token):
    with pytest.raises(HTTPException) as raised:
        decode_token(token)

    _assert_unauthorized(raised.value)


def test_unauthorized_response_raises_401():
    with pytest.raises(HTTPException) as raised:
        unauthorized_response()

    _assert_unauthorized(raised.value)
