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

"""An unsupported chunk type -> INVALID_ARGUMENT, not INTERNAL."""

import grpc
import pytest

from app import server as server_module
from app.external_services.grpc.embedding import embedding_pb2


def test_unsupported_chunk_type_is_invalid_argument(stub, monkeypatch):
    monkeypatch.setattr(
        server_module, "build_text_embed_texts", lambda configuration: None
    )
    request = embedding_pb2.EmbeddingRequest(
        chunk=embedding_pb2.RequestChunk(type=99),
        embeddingModel=embedding_pb2.EmbeddingModel(),
        text="pikachu",
    )

    with pytest.raises(grpc.RpcError) as error:
        stub.GetMessages(request)

    assert error.value.code() == grpc.StatusCode.INVALID_ARGUMENT
    assert error.value.details() == "Unsupported chunk type: 99"
