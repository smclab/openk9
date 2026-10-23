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

"""The common headers are all optional at the model level.

Whether a token or a tenant is required is decided by each endpoint, which
answers 401 or 400 itself: the models only give them a shape. The full set
adds the ACL list to the minimal one.
"""

import pytest
from pydantic import ValidationError

from app.models.models import CommonHeaders, CommonHeadersMinimal


@pytest.mark.parametrize("model", [CommonHeadersMinimal, CommonHeaders])
def test_no_header_is_required(model):
    headers = model.model_validate({})

    assert headers.authorization is None
    assert headers.x_tenant_id is None


def test_minimal_headers_carry_token_and_tenant():
    headers = CommonHeadersMinimal.model_validate(
        {"authorization": "Bearer abc", "x_tenant_id": "tenant-1"}
    )

    assert headers.authorization == "Bearer abc"
    assert headers.x_tenant_id == "tenant-1"


def test_full_headers_add_the_acl_list():
    headers = CommonHeaders.model_validate(
        {"x_tenant_id": "tenant-1", "openk9_acl": ["group:admins", "project:openk9"]}
    )

    assert isinstance(headers, CommonHeadersMinimal)
    assert headers.openk9_acl == ["group:admins", "project:openk9"]
    assert CommonHeaders.model_validate({}).openk9_acl is None


def test_acl_must_be_a_list_of_strings():
    with pytest.raises(ValidationError) as raised:
        CommonHeaders.model_validate({"openk9_acl": "group:admins"})

    assert {error["loc"] for error in raised.value.errors()} == {("openk9_acl",)}
