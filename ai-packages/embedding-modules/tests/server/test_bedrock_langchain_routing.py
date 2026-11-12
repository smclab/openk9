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

"""A Bedrock model served by langchain-aws, end to end over gRPC: it indexes
text and images, answers a query with only an image, and refuses a query
with text and image together, which langchain-aws cannot fuse.

The real builders and the real BedrockEmbeddings run here; only the
bedrock-runtime client is faked, answering Nova vectors."""

import io
import json

import grpc
import pytest
from langchain_aws import BedrockEmbeddings

from app import server as server_module
from app.embedding import bedrock_langchain
from app.external_services.grpc.embedding import embedding_pb2

BUILD_PIPELINES = server_module.build_pipelines
BUILD_QUERY_CAPABILITIES = server_module.build_query_capabilities

NOVA = "amazon.nova-2-multimodal-embeddings-v1:0"

PNG = b"\x89PNG\r\n\x1a\n" + b"\x00" * 8
BMP = b"BM" + b"\x00" * 14

TEXT_VECTOR = [3.0, 4.0, 0.0, 0.0]
IMAGE_VECTOR = [0.0, 0.0, 3.0, 4.0]

STORAGE = {
    "https://signed/img-1": (PNG, "image/png"),
    "https://signed/img-bmp": (BMP, "image/bmp"),
}


class _FakeNovaRuntime:
    def __init__(self):
        self.bodies = []

    def invoke_model(self, body, modelId, accept, contentType):
        request = json.loads(body)
        self.bodies.append(request)

        params = request["singleEmbeddingParams"]
        vector = IMAGE_VECTOR if "image" in params else TEXT_VECTOR
        payload = {"embeddings": [{"embedding": vector}]}

        return {"body": io.BytesIO(json.dumps(payload).encode())}


@pytest.fixture
def runtime(monkeypatch):
    runtime = _FakeNovaRuntime()

    monkeypatch.setattr(
        bedrock_langchain,
        "BedrockEmbeddings",
        lambda **kwargs: BedrockEmbeddings(client=runtime, **kwargs),
    )
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)

    return runtime


@pytest.fixture
def stub(make_stub, monkeypatch):
    monkeypatch.setattr(
        server_module.chunking, "chunk_text", lambda chunker, text: text.split()
    )
    monkeypatch.setattr(server_module, "fetch_url", lambda url: STORAGE[url])

    return make_stub(
        build_pipelines=BUILD_PIPELINES,
        build_query_capabilities=BUILD_QUERY_CAPABILITIES,
    )


def _model():
    return embedding_pb2.EmbeddingModel(
        providerModel=embedding_pb2.ProviderModel(provider="aws_bedrock", model=NOVA),
    )


def _index(stub, refs):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        embeddingModel=_model(),
        text="uno due",
        refs=[
            embedding_pb2.MediaRef(
                url=f"https://signed/{file_id}",
                fileId=file_id,
                contentType=STORAGE[f"https://signed/{file_id}"][1],
            )
            for file_id in refs
        ],
    )
    return [
        (
            chunk.text,
            chunk.fileId if chunk.HasField("fileId") else None,
            list(chunk.f32.values),
        )
        for chunk in stub.EmbedContent(request)
    ]


def _query(stub, text=None, image=None):
    request = embedding_pb2.EmbedQueryRequest(tenantId="mew", embeddingModel=_model())
    if text is not None:
        request.text = text
    if image is not None:
        request.inline.CopyFrom(
            embedding_pb2.InlineMedia(data=image, contentType="image/png")
        )
    return stub.EmbedQuery(request)


def _normalized(vector):
    norm = sum(component * component for component in vector) ** 0.5
    return pytest.approx([component / norm for component in vector])


def test_a_document_with_text_and_an_image_indexes_both(stub, runtime):
    chunks = _index(stub, refs=("img-1",))

    assert [(text, file_id) for text, file_id, _ in chunks] == [
        ("uno", None),
        ("due", None),
        ("", "img-1"),
    ]
    assert chunks[0][2] == _normalized(TEXT_VECTOR)
    assert chunks[2][2] == _normalized(IMAGE_VECTOR)
    assert ["image" in body["singleEmbeddingParams"] for body in runtime.bodies] == [
        False,
        False,
        True,
    ]


def test_a_query_with_only_an_image_gets_a_vector(stub, runtime):
    vector = _query(stub, image=PNG)

    assert list(vector.f32.values) == _normalized(IMAGE_VECTOR)


def test_a_query_with_text_and_an_image_is_a_precondition(stub, runtime):
    with pytest.raises(grpc.RpcError) as error:
        _query(stub, text="un gatto", image=PNG)

    assert error.value.code() == grpc.StatusCode.FAILED_PRECONDITION
    assert runtime.bodies == []


def test_an_image_in_another_format_is_skipped_and_the_text_indexed(stub, runtime):
    chunks = _index(stub, refs=("img-bmp",))

    assert [(text, file_id) for text, file_id, _ in chunks] == [
        ("uno", None),
        ("due", None),
    ]
    assert all("image" not in body["singleEmbeddingParams"] for body in runtime.bodies)
