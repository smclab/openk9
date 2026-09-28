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

"""A chunk type outside the ChunkType enum -> INVALID_ARGUMENT, on the v1
GetMessages path and on EmbedContent alike."""

import grpc
import pytest

from app.external_services.grpc.embedding import embedding_pb2

UNKNOWN_CHUNK_TYPE = 99


def test_get_messages_refuses_an_unknown_chunk_type(stub):
    request = embedding_pb2.EmbeddingRequest(
        chunk=embedding_pb2.RequestChunk(type=UNKNOWN_CHUNK_TYPE),
        embeddingModel=embedding_pb2.EmbeddingModel(),
        text="alfa beta",
    )

    with pytest.raises(grpc.RpcError) as error:
        stub.GetMessages(request)

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT


def test_embed_content_refuses_an_unknown_chunk_type(stub):
    request = embedding_pb2.EmbedContentRequest(
        chunk=embedding_pb2.RequestChunk(type=UNKNOWN_CHUNK_TYPE),
        tenantId="mew",
        text="alfa beta",
    )

    with pytest.raises(grpc.RpcError) as error:
        list(stub.EmbedContent(request))

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
