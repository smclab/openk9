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

"""The v1 GetMessages path builds its chunker through chunking.py, like
the v2 RPCs, from the jsonConfig of the request, and chunks the text
cleaned of its markup."""

from types import SimpleNamespace

from app import server as server_module
from app.embedding import chunking
from app.external_services.grpc.embedding import embedding_pb2


def _recording_splitter(instances):
    """A chunker class whose instances record their arguments and the text,
    and split it on whitespace."""

    class RecordingSplitter:
        def __init__(self, chunk_size: int = 2048):
            self.arguments = {"chunk_size": chunk_size}
            self.texts = []
            instances.append(self)

        def chunk(self, text):
            self.texts.append(text)
            return [SimpleNamespace(text=word) for word in text.split()]

    return RecordingSplitter


def test_the_request_builds_the_chunker_and_the_text_is_cleaned(stub, monkeypatch):
    embedded = []
    instances = []

    def build_text_embed_texts(configuration):
        def embed_texts(texts):
            embedded.append(list(texts))
            return [[float(len(text))] for text in texts]

        return embed_texts

    monkeypatch.setattr(server_module, "build_text_embed_texts", build_text_embed_texts)
    recording_splitter = _recording_splitter(instances)
    monkeypatch.setattr(chunking, "_chunker_class", lambda chunk_type: recording_splitter)
    chunk = embedding_pb2.RequestChunk(type=embedding_pb2.CHUNK_TYPE_SENTENCE_SPLITTER)
    chunk.jsonConfig.update({"chunk_size": 10, "not_an_argument": True})

    response = stub.GetMessages(
        embedding_pb2.EmbeddingRequest(
            chunk=chunk,
            embeddingModel=embedding_pb2.EmbeddingModel(),
            text="<p>pikachu</p> &amp; bulbasaur",
        )
    )

    [splitter] = instances
    # the Struct float is handed over as the int the signature asks for
    assert splitter.arguments == {"chunk_size": 10}
    assert type(splitter.arguments["chunk_size"]) is int
    assert splitter.texts == ["pikachu & bulbasaur"]
    assert embedded == [["pikachu", "&", "bulbasaur"]]
    assert [chunk.text for chunk in response.chunks] == ["pikachu", "&", "bulbasaur"]
