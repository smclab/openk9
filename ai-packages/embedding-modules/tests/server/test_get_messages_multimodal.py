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

"""The v1 GetMessages path serves a model the langchain classes cannot read.

A multimodal model has to go through the same direct client the v2 RPCs use, or
every v1 caller breaks at once: input and output guardrails, domain detection,
uploaded documents. The `multimodal` flag says so when it arrives, but the RAG
callers cannot forward it, so the fallback keys on the failure itself: whatever
the model, an answer langchain cannot read moves the request to the direct
client. `cohere.embed-v4` on Bedrock is the case that motivated this — it
answers with `embeddings` keyed by embedding type, which
`BedrockEmbeddings.embed_documents` returns the keys of instead of raising —
but nothing here names it.
"""

import pytest

from app import server as server_module
from app.external_services.grpc.embedding import embedding_pb2

MULTIMODAL_VECTOR = [0.5, 0.5, 0.5, 0.5]
TEXT_ONLY_VECTOR = [1.0, 0.0, 0.0, 0.0]

# what BedrockEmbeddings raises on the answer of an embedding-types model
UNREADABLE_ANSWER = KeyError(0)


class _FakeMultimodalEmbedder:
    def __init__(self, seen):
        self.seen = seen

    def embed_texts(self, texts, input_type=None):
        self.seen["input_type"] = input_type
        return [MULTIMODAL_VECTOR for _ in texts]


class _FakeLangchainEmbeddings:
    """A langchain class that either answers the batch or fails on it."""

    def __init__(self, error=None, calls=None):
        self.error = error
        self.calls = calls if calls is not None else []

    def embed_documents(self, texts):
        self.calls.append(list(texts))
        if self.error is not None:
            raise self.error
        return [TEXT_ONLY_VECTOR for _ in texts]


class _FakeUnreadableBatch:
    """A langchain class that answers with the keys of the answer instead of
    the vectors, as `BedrockEmbeddings.embed_documents` does on
    `cohere.embed-v4`."""

    def __init__(self, calls):
        self.calls = calls

    def embed_documents(self, texts):
        self.calls.append(list(texts))
        return ["float"]


def _request(multimodal=None, text="pikachu", chunk_size=None):
    chunk = embedding_pb2.RequestChunk(type=1)
    # chunk_size is the DerivedTextSplitter argument: without it the default
    # (2048) applies and any text of this size stays a single chunk
    chunk.jsonConfig.update(
        {"size": 2000} if chunk_size is None else {"chunk_size": chunk_size}
    )

    model = (
        embedding_pb2.EmbeddingModel(multimodal=multimodal)
        if multimodal is not None
        else embedding_pb2.EmbeddingModel()
    )

    return embedding_pb2.EmbeddingRequest(
        chunk=chunk, embeddingModel=model, text=text
    )


def _use_direct(monkeypatch, seen):
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _FakeMultimodalEmbedder(seen),
    )
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)


def test_multimodal_flag_uses_the_direct_embedder(stub, monkeypatch):
    # the flag is enough on its own: langchain must not be built at all
    seen = {}
    _use_direct(monkeypatch, seen)
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _unexpected("initialize_embedding_model"),
    )

    response = stub.GetMessages(_request(multimodal=True))

    # the vector comes from the direct embedder, with the document input type
    assert list(response.chunks[0].vectors) == MULTIMODAL_VECTOR
    assert seen["input_type"] == "search_document"


def test_unreadable_answer_falls_back_to_the_direct_embedder(stub, monkeypatch):
    # the real case: the flag never arrives, so the failure decides. No model
    # name and no provider is involved.
    seen = {}
    _use_direct(monkeypatch, seen)
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(error=UNREADABLE_ANSWER),
    )

    response = stub.GetMessages(_request())

    assert list(response.chunks[0].vectors) == MULTIMODAL_VECTOR
    assert seen["input_type"] == "search_document"


def test_the_failed_langchain_call_is_paid_once_per_request(stub, monkeypatch):
    # the whole document goes in one call, so there is one failure to pay
    seen = {}
    calls = []
    _use_direct(monkeypatch, seen)
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(
            error=UNREADABLE_ANSWER, calls=calls
        ),
    )

    response = stub.GetMessages(
        _request(text="pikachu bulbasaur charmander squirtle", chunk_size=10)
    )

    assert len(response.chunks) > 1, "the text must split, or this proves nothing"
    assert all(
        list(chunk.vectors) == MULTIMODAL_VECTOR for chunk in response.chunks
    )
    assert len(calls) == 1


@pytest.mark.parametrize(
    "document",
    [
        pytest.param({}, id="one chunk"),
        pytest.param(
            {"text": "pikachu bulbasaur charmander squirtle", "chunk_size": 10},
            id="many chunks",
        ),
    ],
)
def test_an_answer_that_is_not_vectors_falls_back_too(stub, monkeypatch, document):
    # the batch call fails to read the answer without raising: what comes
    # back is the keys of the answer, not the vectors. A single-chunk
    # document makes the count match, so the count alone cannot decide it.
    seen = {}
    calls = []
    _use_direct(monkeypatch, seen)
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeUnreadableBatch(calls),
    )

    response = stub.GetMessages(_request(**document))

    assert all(
        list(chunk.vectors) == MULTIMODAL_VECTOR for chunk in response.chunks
    )
    assert len(calls) == 1


def test_text_only_model_keeps_the_langchain_path(stub, monkeypatch):
    # a model langchain can read stays on langchain: the direct client is never
    # built, so a provider without one is unaffected
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(),
    )
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _unexpected("build_multimodal_embedder"),
    )

    response = stub.GetMessages(_request())

    assert list(response.chunks[0].vectors) == TEXT_ONLY_VECTOR


def test_a_failure_that_is_not_about_the_answer_is_not_worked_around(monkeypatch):
    # missing credentials, a throttled provider, an unreachable endpoint: the
    # direct client would fail the same way, so the error must surface as it is
    credentials_error = RuntimeError("no credentials")
    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(error=credentials_error),
    )
    monkeypatch.setattr(
        server_module,
        "build_multimodal_embedder",
        lambda configuration: _unexpected("build_multimodal_embedder"),
    )

    embed_texts = server_module.build_text_embed_texts({"model_type": "aws_bedrock"})

    with pytest.raises(RuntimeError, match="no credentials"):
        embed_texts(["pikachu"])


def test_provider_without_a_direct_client_reports_the_original_failure(monkeypatch):
    # the provider has no direct embedder registered: the langchain failure
    # describes the problem, the missing registration only describes the failed
    # workaround
    def no_embedder(configuration):
        raise ValueError("no multimodal embedder registered for provider 'openai'")

    monkeypatch.setattr(
        server_module,
        "initialize_embedding_model",
        lambda configuration: _FakeLangchainEmbeddings(error=UNREADABLE_ANSWER),
    )
    monkeypatch.setattr(server_module, "build_multimodal_embedder", no_embedder)
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)

    embed_texts = server_module.build_text_embed_texts({"model_type": "openai"})

    with pytest.raises(KeyError):
        embed_texts(["pikachu"])


def _unexpected(name):
    raise AssertionError(f"{name} must not be reached on this path")
