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

"""build_pipelines wires the indexing capabilities of a request. A
multimodal model embeds the chunks and the images through the same direct
client, with the document input type and its credentials applied; any other
model goes through its langchain class and has no image input. Both chunk
with the chunker of the request and fetch the refs over HTTP."""

from types import SimpleNamespace

import pytest

from app import server as server_module

MULTIMODAL_CONFIGURATION = {
    "model_type": "aws_bedrock",
    "model": "cohere.embed-v4",
    "multimodal": True,
}
TEXT_ONLY_CONFIGURATION = {"model_type": "openai", "model": "m", "multimodal": False}


class _RecordingChunker:
    """A chunker that records the text and splits it on whitespace."""

    def __init__(self):
        self.texts = []

    def chunk(self, text):
        self.texts.append(text)
        return [SimpleNamespace(text=word) for word in text.split()]


class _FakeMultimodalEmbedder:
    def __init__(self, calls):
        self.calls = calls

    def embed_texts(self, texts, input_type=None):
        self.calls.append(("embed_texts", list(texts), input_type))
        return [[float(len(text))] for text in texts]

    def embed_image(self, data, content_type):
        self.calls.append(("embed_image", data, content_type))
        return [0.0, 1.0]


class _FakeLangchainEmbeddings:
    def __init__(self, calls):
        self.calls = calls

    def embed_documents(self, texts):
        self.calls.append(("embed_documents", list(texts)))
        return [[float(len(text))] for text in texts]


@pytest.fixture
def calls(monkeypatch):
    """Records every builder the pipelines are made from, and every call to
    the embedders they return."""
    calls = []

    def apply_credentials(configuration):
        calls.append(("_apply_credentials", configuration))

    def build_multimodal_embedder(configuration):
        calls.append(("build_multimodal_embedder", configuration))
        return _FakeMultimodalEmbedder(calls)

    def initialize_embedding_model(configuration):
        calls.append(("initialize_embedding_model", configuration))
        return _FakeLangchainEmbeddings(calls)

    monkeypatch.setattr(server_module, "_apply_credentials", apply_credentials)
    monkeypatch.setattr(
        server_module, "build_multimodal_embedder", build_multimodal_embedder
    )
    monkeypatch.setattr(
        server_module, "initialize_embedding_model", initialize_embedding_model
    )

    return calls


def test_a_multimodal_model_indexes_through_the_direct_client(calls):
    chunker = _RecordingChunker()
    pipelines = server_module.build_pipelines(MULTIMODAL_CONFIGURATION, chunker)

    # credentials first, then the embedder of the configured provider;
    # langchain is not built at all
    assert calls == [
        ("_apply_credentials", MULTIMODAL_CONFIGURATION),
        ("build_multimodal_embedder", MULTIMODAL_CONFIGURATION),
    ]
    calls.clear()

    assert pipelines.embed_texts(["uno", "quattro"]) == [[3.0], [7.0]]
    assert pipelines.embed_image(b"png", "image/png") == [0.0, 1.0]
    # the chunks of a document are embedded as documents, never as queries
    assert calls == [
        ("embed_texts", ["uno", "quattro"], "search_document"),
        ("embed_image", b"png", "image/png"),
    ]

    assert pipelines.chunk("uno due") == ["uno", "due"]
    assert chunker.texts == ["uno due"]
    assert pipelines.fetch is server_module.fetch_url


def test_a_text_only_model_indexes_through_langchain_without_images(calls):
    chunker = _RecordingChunker()
    pipelines = server_module.build_pipelines(TEXT_ONLY_CONFIGURATION, chunker)

    assert calls == [("initialize_embedding_model", TEXT_ONLY_CONFIGURATION)]
    calls.clear()

    assert pipelines.embed_texts(["uno"]) == [[3.0]]
    assert calls == [("embed_documents", ["uno"])]
    # no image input: the router skips the image refs
    assert pipelines.embed_image is None

    assert pipelines.chunk("uno due") == ["uno", "due"]
    assert chunker.texts == ["uno due"]
    assert pipelines.fetch is server_module.fetch_url
