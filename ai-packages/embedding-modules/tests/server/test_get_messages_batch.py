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

"""The v1 GetMessages path embeds a document in one request.

The chunks reach the provider as a single list and the vectors come back
matched to their chunk by position, so the cost of embedding a document
depends on the text and not on the number of chunks it was split into.
The response is unchanged: progressive `number`, `total`, text, vectors.
"""

from app import server as server_module
from app.external_services.grpc.embedding import embedding_pb2


class _FakeLangchainEmbeddings:
    """Records every batch and answers with a vector derived from the text,
    so a vector attached to the wrong chunk is recognizable."""

    def __init__(self, calls):
        self.calls = calls

    def embed_documents(self, texts):
        self.calls.append(list(texts))
        return [[float(len(text))] for text in texts]

    def embed_query(self, text):
        raise AssertionError("the chunks must not be embedded one at a time")


def _request(text, chunk_size):
    chunk = embedding_pb2.RequestChunk(type=1)
    chunk.jsonConfig.update({"chunk_size": chunk_size})

    return embedding_pb2.EmbeddingRequest(
        chunk=chunk,
        embeddingModel=embedding_pb2.EmbeddingModel(),
        text=text,
    )


def _use_langchain(monkeypatch, calls):
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(calls),
    )
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _unexpected("build_multimodal_embedder"),
    )


def _unexpected(name):
    raise AssertionError(f"{name} must not be reached on this path")


def test_a_multi_chunk_document_is_one_request(stub, monkeypatch):
    calls = []
    _use_langchain(monkeypatch, calls)

    response = stub.GetMessages(
        _request("pikachu bulbasaur charmander squirtle", chunk_size=10)
    )

    chunks = response.chunks
    assert len(chunks) > 1, "the text must split, or this proves nothing"

    # one request, carrying every chunk in order
    assert calls == [[chunk.text for chunk in chunks]]

    # unchanged response: progressive number, the total, and the vector of
    # the chunk it belongs to
    assert [chunk.number for chunk in chunks] == list(range(1, len(chunks) + 1))
    assert all(chunk.total == len(chunks) for chunk in chunks)
    assert all(list(chunk.vectors) == [len(chunk.text)] for chunk in chunks)


def test_a_single_chunk_document_is_unchanged(stub, monkeypatch):
    calls = []
    _use_langchain(monkeypatch, calls)

    response = stub.GetMessages(_request("pikachu", chunk_size=2048))

    assert len(response.chunks) == 1
    assert calls == [["pikachu"]]

    chunk = response.chunks[0]
    assert chunk.number == 1
    assert chunk.total == 1
    assert chunk.text == "pikachu"
    assert list(chunk.vectors) == [len("pikachu")]


def test_a_document_with_no_chunk_is_not_embedded(stub, monkeypatch):
    # an empty text splits into nothing: there is no batch to send, and an
    # empty one is what several providers reject
    calls = []
    _use_langchain(monkeypatch, calls)

    response = stub.GetMessages(_request("", chunk_size=2048))

    assert list(response.chunks) == []
    assert calls == []
