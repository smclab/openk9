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

"""The indexing path builds one document per streamed chunk.

EmbedContent answers a stream: one message per chunk, in order, and no
message at all for a text that produces no chunk. Every message becomes an
indexable document, carrying the metadata of the source document and the
float payload of its vector.
"""

from unittest.mock import MagicMock, patch

import pytest

from app.external_services.grpc import grpc_client
from app.external_services.grpc.embedding import embedding_pb2

DOCUMENT = {
    "text": "un gatto e un topo",
    "document_id": "doc-1",
    "user_id": "user-1",
    "chat_id": "chat-1",
    "filename": "gatti",
    "file_extension": ".md",
}


def _generate_documents_embeddings(embedded_chunks):
    stub = MagicMock()
    stub.EmbedContent = MagicMock(return_value=iter(embedded_chunks))

    with patch.object(grpc_client.grpc, "insecure_channel"), patch.object(
        grpc_client.embedding_pb2_grpc, "EmbeddingStub", return_value=stub
    ):
        return grpc_client.generate_documents_embeddings(
            "localhost:50053",
            "mew",
            {"type": 1},
            {},
            DOCUMENT,
        )


def _chunk(number, text, values):
    return embedding_pb2.EmbeddedChunk(
        number=number,
        total=2,
        text=text,
        f32=embedding_pb2.FloatVector(values=values),
    )


def test_every_streamed_chunk_becomes_a_document():
    documents = _generate_documents_embeddings(
        [_chunk(1, "un gatto", [1.0, 0.0]), _chunk(2, "un topo", [0.0, 1.0])]
    )

    assert [document["chunkText"] for document in documents] == [
        "un gatto",
        "un topo",
    ]
    assert [document["chunk_number"] for document in documents] == [1, 2]
    assert documents[0]["vector"] == pytest.approx([1.0, 0.0])
    assert documents[1]["total_chunks"] == 2
    # the metadata of the source document travels with every chunk
    assert documents[0]["filename"] == "gatti"
    assert documents[0]["chat_id"] == "chat-1"


def test_an_empty_stream_yields_no_document():
    # a text that produces no chunk: nothing to index, and no error
    assert _generate_documents_embeddings([]) == []
