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

"""build_query_capabilities wires the query-time capabilities. A multimodal
model embeds the query text with the query input type, the image through
the same client, and the text+image pair only when the concrete embedder
has native mixed input; any other model embeds query text only, through
its langchain class."""

import pytest

from app import server as server_module

MULTIMODAL_CONFIGURATION = {
    "model_type": "aws_bedrock",
    "model": "cohere.embed-v4",
    "multimodal": True,
}
TEXT_ONLY_CONFIGURATION = {"model_type": "openai", "model": "m", "multimodal": False}


class _FakeMultimodalEmbedder:
    def __init__(self, calls):
        self.calls = calls

    def embed_texts(self, texts, input_type=None):
        self.calls.append(("embed_texts", list(texts), input_type))
        return [[1.0, 0.0] for _ in texts]

    def embed_image(self, data, content_type):
        self.calls.append(("embed_image", data, content_type))
        return [0.0, 1.0]


class _FakeMixedEmbedder(_FakeMultimodalEmbedder):
    def embed_mixed(self, text, data, content_type):
        self.calls.append(("embed_mixed", text, data, content_type))
        return [0.5, 0.5]


class _FakeLangchainEmbeddings:
    def __init__(self, calls):
        self.calls = calls

    def embed_query(self, text):
        self.calls.append(("embed_query", text))
        return [1.0, 0.0]


def _record_builders(monkeypatch, calls, embedder_class):
    def apply_credentials(configuration):
        calls.append(("_apply_credentials", configuration))

    def build_multimodal_embedder(configuration):
        calls.append(("build_multimodal_embedder", configuration))
        return embedder_class(calls)

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


@pytest.mark.parametrize(
    "embedder_class", [_FakeMultimodalEmbedder, _FakeMixedEmbedder]
)
def test_a_multimodal_model_embeds_the_query_through_the_direct_client(
    monkeypatch, embedder_class
):
    calls = []
    _record_builders(monkeypatch, calls, embedder_class)

    capabilities = server_module.build_query_capabilities(MULTIMODAL_CONFIGURATION)

    assert calls == [
        ("_apply_credentials", MULTIMODAL_CONFIGURATION),
        ("build_multimodal_embedder", MULTIMODAL_CONFIGURATION),
    ]
    calls.clear()

    # one text in, its single vector out, embedded as a query
    assert capabilities.embed_text("un gatto") == [1.0, 0.0]
    assert capabilities.embed_image(b"png", "image/png") == [0.0, 1.0]
    assert calls == [
        ("embed_texts", ["un gatto"], "search_query"),
        ("embed_image", b"png", "image/png"),
    ]


def test_native_mixed_input_is_taken_from_the_embedder(monkeypatch):
    calls = []
    _record_builders(monkeypatch, calls, _FakeMixedEmbedder)

    capabilities = server_module.build_query_capabilities(MULTIMODAL_CONFIGURATION)
    calls.clear()

    assert capabilities.embed_mixed("un gatto", b"png", "image/png") == [0.5, 0.5]
    assert calls == [("embed_mixed", "un gatto", b"png", "image/png")]


def test_an_embedder_without_mixed_input_declares_none(monkeypatch):
    _record_builders(monkeypatch, [], _FakeMultimodalEmbedder)

    capabilities = server_module.build_query_capabilities(MULTIMODAL_CONFIGURATION)

    assert capabilities.embed_mixed is None


def test_a_text_only_model_embeds_query_text_only(monkeypatch):
    calls = []
    _record_builders(monkeypatch, calls, _FakeMixedEmbedder)

    capabilities = server_module.build_query_capabilities(TEXT_ONLY_CONFIGURATION)

    assert calls == [("initialize_embedding_model", TEXT_ONLY_CONFIGURATION)]
    calls.clear()

    assert capabilities.embed_text("un gatto") == [1.0, 0.0]
    assert calls == [("embed_query", "un gatto")]
    assert capabilities.embed_image is None
    assert capabilities.embed_mixed is None
