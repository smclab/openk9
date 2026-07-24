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

"""Neither text nor refs -> INVALID_ARGUMENT."""

import grpc
import pytest

from app.external_services.grpc.embedding import embedding_pb2


def test_empty_source_is_invalid_argument(stub):
    request = embedding_pb2.EmbedContentRequest(tenantId="mew")

    with pytest.raises(grpc.RpcError) as error:
        list(stub.EmbedContent(request))

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
