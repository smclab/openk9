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

"""Malformed queries are INVALID_ARGUMENT: an empty request, or an inline
that is not an image. The malformed check precedes the model-capability
check (so a non-image inline is INVALID_ARGUMENT, not FAILED_PRECONDITION)."""

import grpc
import pytest

from app.external_services.grpc.embedding import embedding_pb2


def test_empty_request_is_invalid_argument(stub):
    request = embedding_pb2.EmbedQueryRequest(tenantId="mew")

    with pytest.raises(grpc.RpcError) as error:
        stub.EmbedQuery(request)

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT


def test_inline_non_image_is_invalid_argument(stub):
    request = embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        inline=embedding_pb2.InlineMedia(data=b"wav", contentType="audio/wav"),
    )

    with pytest.raises(grpc.RpcError) as error:
        stub.EmbedQuery(request)

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
