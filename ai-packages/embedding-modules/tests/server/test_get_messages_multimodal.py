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

"""The v1 GetMessages path picks the embedder by the multimodal flag.

A multimodal model must go through the same direct client the v2 RPCs use: the
langchain text classes cannot serve it (cohere embed-v4 answers with
`embeddings` keyed by embedding type, so `BedrockEmbeddings.embed_query` raises
`KeyError: 0`), and every v1 caller would break at once — input and output
guardrails, domain detection, uploaded documents.
"""

from app import server as server_module
from app.external_services.grpc.embedding import embedding_pb2

MULTIMODAL_VECTOR = [0.5, 0.5, 0.5, 0.5]
TEXT_ONLY_VECTOR = [1.0, 0.0, 0.0, 0.0]


class _FakeMultimodalEmbedder:
    def __init__(self, seen):
        self.seen = seen

    def embed_texts(self, texts, input_type=None):
        self.seen["input_type"] = input_type
        return [MULTIMODAL_VECTOR for _ in texts]


class _FakeLangchainEmbeddings:
    def embed_query(self, text):
        return TEXT_ONLY_VECTOR


def _request(multimodal=None, provider=None, model_id=None):
    chunk = embedding_pb2.RequestChunk(type=1)
    chunk.jsonConfig.update({"size": 2000})

    model = (
        embedding_pb2.EmbeddingModel(multimodal=multimodal)
        if multimodal is not None
        else embedding_pb2.EmbeddingModel()
    )

    if provider is not None or model_id is not None:
        model.providerModel.provider = provider or ""
        model.providerModel.model = model_id or ""

    return embedding_pb2.EmbeddingRequest(
        chunk=chunk, embeddingModel=model, text="pikachu"
    )


def test_multimodal_model_uses_the_direct_embedder(stub, monkeypatch):
    # 1. un modello multimodale: il percorso langchain non deve essere toccato
    seen = {}
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _FakeMultimodalEmbedder(seen),
    )
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _fail_langchain(),
    )
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)

    response = stub.GetMessages(_request(multimodal=True))

    # il vettore arriva dall'embedder diretto, con l'input type del query path
    assert list(response.chunks[0].vectors) == MULTIMODAL_VECTOR
    assert seen["input_type"] == "search_query"


def test_cohere_on_bedrock_uses_the_direct_embedder_without_the_flag(stub, monkeypatch):
    # il caso reale: i chiamanti RAG non inoltrano il flag (la risposta di
    # GetEmbeddingModelConfigurations non ce l'ha), quindi decidono provider e
    # modello
    seen = {}
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _FakeMultimodalEmbedder(seen),
    )
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _fail_langchain(),
    )
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)

    response = stub.GetMessages(
        _request(provider="aws_bedrock", model_id="cohere.embed-v4:0")
    )

    assert list(response.chunks[0].vectors) == MULTIMODAL_VECTOR


def test_other_bedrock_model_keeps_the_langchain_path(stub, monkeypatch):
    # un modello Bedrock non cohere (es. titan) parla un altro protocollo:
    # il nostro client non lo serve, langchain si
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(),
    )
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _fail_multimodal(),
    )

    response = stub.GetMessages(
        _request(provider="aws_bedrock", model_id="amazon.titan-embed-text-v2:0")
    )

    assert list(response.chunks[0].vectors) == TEXT_ONLY_VECTOR


def test_text_only_model_keeps_the_langchain_path(stub, monkeypatch):
    # 2. un modello non multimodale resta su langchain, come prima
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(),
    )
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _fail_multimodal(),
    )

    response = stub.GetMessages(_request())

    assert list(response.chunks[0].vectors) == TEXT_ONLY_VECTOR


def _fail_langchain():
    raise AssertionError(
        "un modello multimodale non deve passare per initialize_embedding_model"
    )


def _fail_multimodal():
    raise AssertionError(
        "un modello di solo testo non deve passare per build_multimodal_embedder"
    )
