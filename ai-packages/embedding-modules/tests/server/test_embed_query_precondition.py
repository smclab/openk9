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

"""A query-time request cannot degrade: a capability the model lacks is
FAILED_PRECONDITION, not a fallback. Two cases: an image on a text-only
model, and text+image on a model without native mixed input."""

import grpc
import pytest

from app.embedding.query import QueryCapabilities
from app.external_services.grpc.embedding import embedding_pb2

TEXT_VECTOR = [3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0]
IMAGE_VECTOR = [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]


def _text_only_model(configuration):
    return QueryCapabilities(embed_text=lambda text: TEXT_VECTOR)


def _no_mixed_model(configuration):
    return QueryCapabilities(
        embed_text=lambda text: TEXT_VECTOR,
        embed_image=lambda data, content_type: IMAGE_VECTOR,
        embed_mixed=None,
    )


def test_inline_image_on_text_only_model_is_precondition(make_stub):
    stub = make_stub(build_query_capabilities=_text_only_model)
    request = embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        inline=embedding_pb2.InlineMedia(data=b"png", contentType="image/png"),
    )

    with pytest.raises(grpc.RpcError) as error:
        stub.EmbedQuery(request)

    assert error.value.code() == grpc.StatusCode.FAILED_PRECONDITION


def test_text_and_inline_without_native_mixed_is_precondition(make_stub):
    stub = make_stub(build_query_capabilities=_no_mixed_model)
    request = embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        text="un gatto",
        inline=embedding_pb2.InlineMedia(data=b"png", contentType="image/png"),
    )

    with pytest.raises(grpc.RpcError) as error:
        stub.EmbedQuery(request)

    assert error.value.code() == grpc.StatusCode.FAILED_PRECONDITION
