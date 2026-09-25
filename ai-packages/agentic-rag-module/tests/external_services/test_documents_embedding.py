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

import grpc
import pytest
from fastapi import HTTPException
from google.protobuf.struct_pb2 import Struct

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

CHUNK_JSON_CONFIG = Struct()
CHUNK_JSON_CONFIG.update({"size": 2000})
CHUNK = {"type": 1, "jsonConfig": CHUNK_JSON_CONFIG}

EMBEDDING_MODEL = {
    "apiKey": "sk-embed",
    "providerModel": {"provider": "openai", "model": "text-embedding-3-small"},
    "apiUrl": "http://embedder:8080",
    "multimodal": False,
}

# time.time() at the moment the chunks are read: a timestamp in milliseconds
NOW = 1700000000.123
TIMESTAMP = 1700000000123


def _generate_documents_embeddings(embedded_chunks, stub=None):
    if stub is None:
        stub = MagicMock()
        stub.EmbedContent = MagicMock(return_value=iter(embedded_chunks))

    with patch.object(grpc_client.grpc, "insecure_channel") as channel, patch.object(
        grpc_client.embedding_pb2_grpc, "EmbeddingStub", return_value=stub
    ) as stub_class, patch.object(grpc_client.time, "time", return_value=NOW):
        documents = grpc_client.generate_documents_embeddings(
            "localhost:50053",
            "mew",
            CHUNK,
            EMBEDDING_MODEL,
            DOCUMENT,
        )

    channel.assert_called_once_with("localhost:50053")
    stub_class.assert_called_once_with(channel.return_value.__enter__.return_value)
    return documents


def _chunk(number, text, values):
    return embedding_pb2.EmbeddedChunk(
        number=number,
        total=2,
        text=text,
        f32=embedding_pb2.FloatVector(values=values),
    )


def test_the_request_carries_the_text_and_the_embedding_setup():
    stub = MagicMock()
    stub.EmbedContent = MagicMock(return_value=iter([]))

    _generate_documents_embeddings([], stub=stub)

    stub.EmbedContent.assert_called_once_with(
        embedding_pb2.EmbedContentRequest(
            tenantId="mew",
            chunk=embedding_pb2.RequestChunk(type=1, jsonConfig=CHUNK_JSON_CONFIG),
            embeddingModel=embedding_pb2.EmbeddingModel(
                apiKey="sk-embed",
                providerModel=embedding_pb2.ProviderModel(
                    provider="openai", model="text-embedding-3-small"
                ),
                apiUrl="http://embedder:8080",
                multimodal=False,
            ),
            vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_FLOAT32,
            text="un gatto e un topo",
        )
    )


def test_every_streamed_chunk_becomes_a_document():
    documents = _generate_documents_embeddings(
        [_chunk(1, "un gatto", [1.0, 0.0]), _chunk(2, "un topo", [0.0, 1.0])]
    )

    # the metadata of the source document travels with every chunk
    metadata = {
        "timestamp": TIMESTAMP,
        "document_id": "doc-1",
        "user_id": "user-1",
        "chat_id": "chat-1",
        "filename": "gatti",
        "file_extension": ".md",
        "total_chunks": 2,
    }
    assert documents == [
        {**metadata, "chunk_number": 1, "chunkText": "un gatto", "vector": [1.0, 0.0]},
        {**metadata, "chunk_number": 2, "chunkText": "un topo", "vector": [0.0, 1.0]},
    ]


def test_an_empty_stream_yields_no_document():
    # a text that produces no chunk: nothing to index, and no error
    assert _generate_documents_embeddings([]) == []


class _RpcError(grpc.RpcError):
    def details(self):
        return "unavailable"


@pytest.mark.parametrize("failure", [_RpcError(), ValueError("broken stream")])
def test_any_failure_is_an_opaque_internal_error(failure):
    stub = MagicMock()
    stub.EmbedContent = MagicMock(side_effect=failure)

    with pytest.raises(HTTPException) as error:
        _generate_documents_embeddings([], stub=stub)

    assert error.value.status_code == 500
    assert error.value.detail == grpc_client.UNEXPECTED_ERROR_MESSAGE
