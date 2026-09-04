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

"""The query path reads one vector, or none at all.

A query maps onto a single vector: the float payload of EmbeddedVector. A
query with nothing to embed (empty once cleaned by the module) is
INVALID_ARGUMENT and must become "no vector", so the retriever skips the
search; every other failure has to keep surfacing as it did.
"""

from unittest.mock import MagicMock, patch

import grpc
import pytest
from fastapi import HTTPException

from app.external_services.grpc import grpc_client
from app.external_services.grpc.embedding import embedding_pb2


class _RpcError(grpc.RpcError):
    """A gRPC failure carrying a status code, like the ones a stub raises."""

    def __init__(self, code):
        self._code = code

    def code(self):
        return self._code

    def details(self):
        return "boom"


def _generate_query_embedding(embed_query):
    stub = MagicMock()
    stub.EmbedQuery = embed_query

    with patch.object(grpc_client.grpc, "insecure_channel"), patch.object(
        grpc_client.embedding_pb2_grpc, "EmbeddingStub", return_value=stub
    ):
        return grpc_client.generate_query_embedding(
            grpc_host="localhost:50053",
            tenant_id="mew",
            embedding_model={},
            text="un gatto",
        )


def test_vector_comes_from_the_float_payload():
    embedded_vector = embedding_pb2.EmbeddedVector(
        dimension=2, f32=embedding_pb2.FloatVector(values=[0.6, 0.8])
    )

    vector = _generate_query_embedding(MagicMock(return_value=embedded_vector))

    assert vector == pytest.approx([0.6, 0.8])


def test_nothing_to_embed_is_no_vector():
    embed_query = MagicMock(side_effect=_RpcError(grpc.StatusCode.INVALID_ARGUMENT))

    assert _generate_query_embedding(embed_query) is None


def test_any_other_failure_still_surfaces():
    embed_query = MagicMock(side_effect=_RpcError(grpc.StatusCode.INTERNAL))

    with pytest.raises(HTTPException) as error:
        _generate_query_embedding(embed_query)

    assert error.value.status_code == 500
