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

"""The model picks the client, not the multimodal flag.

A model in the registry is served by its own embedder for text, images and
mixed queries; any other model goes to langchain and embeds text only. The
three RPCs choose the same way, and a provider error surfaces as it is, with
no second attempt on another client.

The real builders run here; only the factories of the registry and the
langchain classes are faked, so the predicates on provider and model are the
real ones.
"""

import logging

import grpc
import pytest

from app import server as server_module
from app.embedding import multimodal
from app.external_services.grpc.embedding import embedding_pb2

# the real builders: the stub fixture replaces them with fakes by default
BUILD_PIPELINES = server_module.build_pipelines
BUILD_QUERY_CAPABILITIES = server_module.build_query_capabilities

DIRECT_TEXT_VECTOR = [3.0, 4.0, 0.0, 0.0]
DIRECT_IMAGE_VECTOR = [0.0, 3.0, 4.0, 0.0]
DIRECT_MIXED_VECTOR = [0.0, 0.0, 3.0, 4.0]
LANGCHAIN_VECTOR = [4.0, 0.0, 0.0, 3.0]

# the object store behind the pre-signed URLs of the refs
STORAGE = {"https://signed/img-1": (b"png-1", "image/png")}

COHERE_V4 = ("aws_bedrock", "cohere.embed-v4:0")
TITAN_V2 = ("aws_bedrock", "amazon.titan-embed-text-v2:0")


class _FakeDirectEmbedder:
    def __init__(self, calls, error=None):
        self.calls = calls
        self.error = error

    def embed_texts(self, texts, input_type=None):
        self.calls.append(("embed_texts", list(texts), input_type))
        if self.error is not None:
            raise self.error
        return [DIRECT_TEXT_VECTOR for _ in texts]

    def embed_image(self, data, content_type):
        self.calls.append(("embed_image", data, content_type))
        return DIRECT_IMAGE_VECTOR

    def embed_mixed(self, text, data, content_type, input_type="search_query"):
        self.calls.append(("embed_mixed", text, data, content_type))
        return DIRECT_MIXED_VECTOR


class _FakeLangchainEmbeddings:
    def __init__(self, calls):
        self.calls = calls

    def embed_documents(self, texts):
        self.calls.append(("embed_documents", list(texts)))
        return [LANGCHAIN_VECTOR for _ in texts]

    def embed_query(self, text):
        self.calls.append(("embed_query", text))
        return LANGCHAIN_VECTOR


@pytest.fixture
def calls(monkeypatch):
    """Fakes the registry factories and the langchain classes, and records
    what each of them is asked to embed, in order."""
    calls = []

    def direct(configuration):
        calls.append(("direct", configuration["model"]))
        return _FakeDirectEmbedder(calls)

    monkeypatch.setattr(
        multimodal,
        "MULTIMODAL_EMBEDDERS",
        [entry._replace(factory=direct) for entry in multimodal.MULTIMODAL_EMBEDDERS],
    )
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)

    def langchain(configuration):
        calls.append(("langchain", configuration["model"]))
        return _FakeLangchainEmbeddings(calls)

    monkeypatch.setattr(server_module, "initialize_embedding_model", langchain)

    return calls


@pytest.fixture
def stub(make_stub, monkeypatch):
    """A server on the real builders, with whitespace chunking and a fake
    object store."""
    monkeypatch.setattr(
        server_module.chunking, "chunk_text", lambda chunker, text: text.split()
    )
    monkeypatch.setattr(server_module, "fetch_url", lambda url: STORAGE[url])

    return make_stub(
        build_pipelines=BUILD_PIPELINES,
        build_query_capabilities=BUILD_QUERY_CAPABILITIES,
    )


def _model(provider_model, multimodal_flag=False):
    provider, model = provider_model
    return embedding_pb2.EmbeddingModel(
        providerModel=embedding_pb2.ProviderModel(provider=provider, model=model),
        multimodal=multimodal_flag,
    )


def _index(stub, model, text="uno due", refs=("img-1",)):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        embeddingModel=model,
        text=text,
        refs=[
            embedding_pb2.MediaRef(
                url=f"https://signed/{file_id}", fileId=file_id, contentType="image/png"
            )
            for file_id in refs
        ],
    )
    return [
        (chunk.text, chunk.fileId if chunk.HasField("fileId") else None, list(chunk.f32.values))
        for chunk in stub.EmbedContent(request)
    ]


def _query(stub, model, text=None, image=None):
    request = embedding_pb2.EmbedQueryRequest(tenantId="mew", embeddingModel=model)
    if text is not None:
        request.text = text
    if image is not None:
        request.inline.CopyFrom(embedding_pb2.InlineMedia(data=image, contentType="image/png"))
    return stub.EmbedQuery(request)


def _normalized(vector):
    norm = sum(component * component for component in vector) ** 0.5
    return pytest.approx([component / norm for component in vector])


def test_cohere_v4_embeds_images_without_the_flag(stub, calls):
    chunks = _index(stub, _model(COHERE_V4, multimodal_flag=False))

    assert [(text, file_id) for text, file_id, _ in chunks] == [
        ("uno", None),
        ("due", None),
        ("", "img-1"),
    ]
    assert chunks[2][2] == _normalized(DIRECT_IMAGE_VECTOR)
    assert ("embed_texts", ["uno", "due"], "search_document") in calls
    assert ("embed_image", b"png-1", "image/png") in calls
    assert not any(call[0] == "langchain" for call in calls)


def test_the_flag_does_not_change_the_client(stub, calls):
    without_flag = _index(stub, _model(COHERE_V4, multimodal_flag=False))
    with_flag = _index(stub, _model(COHERE_V4, multimodal_flag=True))

    assert with_flag == without_flag


def test_cohere_v4_answers_a_mixed_query_with_one_vector(stub, calls):
    vector = _query(stub, _model(COHERE_V4), text="un gatto", image=b"png-1")

    assert list(vector.f32.values) == _normalized(DIRECT_MIXED_VECTOR)
    assert ("embed_mixed", "un gatto", b"png-1", "image/png") in calls


def test_a_model_outside_the_registry_with_the_flag_embeds_text_only(
    stub, calls, caplog
):
    with caplog.at_level(logging.WARNING, logger=server_module.logger.name):
        chunks = _index(stub, _model(TITAN_V2, multimodal_flag=True))

    assert [(text, file_id) for text, file_id, _ in chunks] == [("uno", None), ("due", None)]
    assert calls == [
        ("langchain", "amazon.titan-embed-text-v2:0"),
        ("embed_documents", ["uno", "due"]),
    ]
    assert any(
        "EmbedContent skip" in record.getMessage() and "img-1" in record.getMessage()
        for record in caplog.records
    )


def test_a_custom_provider_with_the_flag_does_not_fail(stub, calls, caplog):
    with caplog.at_level(logging.INFO, logger=server_module.logger.name):
        chunks = _index(
            stub,
            _model(("my-gateway", "my-embedder"), multimodal_flag=True),
            refs=(),
        )

    assert [text for text, _, _ in chunks] == ["uno", "due"]
    assert calls[0] == ("langchain", "my-embedder")
    assert not any(
        "no multimodal embedder registered" in record.getMessage()
        for record in caplog.records
    )


def test_a_query_with_an_image_on_a_text_only_model_is_a_precondition(stub, calls):
    with pytest.raises(grpc.RpcError) as error:
        _query(stub, _model(TITAN_V2), image=b"png-1")

    assert error.value.code() == grpc.StatusCode.FAILED_PRECONDITION


def test_a_provider_error_is_not_retried_on_another_client(stub, calls, monkeypatch):
    provider_error = RuntimeError("UnrecognizedClientException: invalid token")
    monkeypatch.setattr(
        multimodal,
        "MULTIMODAL_EMBEDDERS",
        [
            entry._replace(factory=lambda configuration: _FakeDirectEmbedder(calls, provider_error))
            for entry in multimodal.MULTIMODAL_EMBEDDERS
        ],
    )

    with pytest.raises(grpc.RpcError) as error:
        _index(stub, _model(COHERE_V4), refs=())

    assert error.value.code() == grpc.StatusCode.INTERNAL
    assert error.value.details() == "UnrecognizedClientException: invalid token"
    assert [call[0] for call in calls] == ["embed_texts"]


@pytest.mark.parametrize(
    ("provider_model", "client"),
    [
        pytest.param(COHERE_V4, "direct", id="cohere v4"),
        pytest.param(("aws_bedrock", "eu.cohere.embed-v4:0"), "direct", id="cohere v4 eu profile"),
        pytest.param(("chat_vertex_ai", "multimodalembedding@001"), "direct", id="vertex multimodal"),
        pytest.param(TITAN_V2, "langchain", id="titan"),
        pytest.param(("openai", "text-embedding-3-small"), "langchain", id="openai"),
    ],
)
def test_get_messages_picks_the_client_from_the_model(stub, calls, provider_model, client):
    chunk = embedding_pb2.RequestChunk(type=1)
    chunk.jsonConfig.update({"size": 2000})

    stub.GetMessages(
        embedding_pb2.EmbeddingRequest(
            chunk=chunk, embeddingModel=_model(provider_model), text="pikachu"
        )
    )

    assert calls[0] == (client, provider_model[1])
    if client == "direct":
        assert calls[1] == ("embed_texts", ["pikachu"], "search_document")
    else:
        assert calls[1] == ("embed_documents", ["pikachu"])


@pytest.mark.parametrize(
    ("provider_model", "expected"),
    [
        pytest.param(COHERE_V4, ("embed_texts", ["pikachu"], "search_query"), id="cohere v4"),
        pytest.param(TITAN_V2, ("embed_query", "pikachu"), id="titan"),
    ],
)
def test_a_text_query_keeps_its_input_type(stub, calls, provider_model, expected):
    _query(stub, _model(provider_model), text="pikachu")

    assert calls[1] == expected


def _served_lines(caplog, rpc):
    return [
        record.getMessage()
        for record in caplog.records
        if record.levelno == logging.INFO
        and record.getMessage().startswith(f"{rpc} served:")
    ]


def test_a_query_logs_the_client_that_served_it(stub, calls, caplog):
    model = _model(COHERE_V4)
    model.apiKey = "secret-key"

    with caplog.at_level(logging.INFO, logger=server_module.logger.name):
        _query(stub, model, text="pikachu")

    [line] = _served_lines(caplog, "EmbedQuery")
    assert "tenantId=mew" in line
    assert "provider=aws_bedrock model=cohere.embed-v4:0" in line
    assert "client=bedrock-cohere-v4" in line
    assert "inputType=search_query" in line
    assert "vectorDataType=VECTOR_DATA_TYPE_FLOAT32 vectors=1" in line
    assert "secret-key" not in line


def test_an_indexing_logs_the_client_and_the_chunks(stub, calls, caplog):
    with caplog.at_level(logging.INFO, logger=server_module.logger.name):
        _index(stub, _model(TITAN_V2), refs=())

    [line] = _served_lines(caplog, "EmbedContent")
    assert "client=langchain" in line
    assert "inputType=search_document" in line
    assert "vectors=2" in line


def test_a_failed_query_logs_no_served_line(stub, calls, caplog):
    with caplog.at_level(logging.INFO, logger=server_module.logger.name):
        with pytest.raises(grpc.RpcError):
            _query(stub, _model(TITAN_V2), image=b"png-1")

    assert _served_lines(caplog, "EmbedQuery") == []
