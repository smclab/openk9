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

"""Each multimodal embedder sends text and images through the same
injected client and model, so both land in one shared vector space. Fake
clients record the exact requests and stand in for boto3 / the Vertex
SDK."""

import base64
import io
import json

from app.embedding.bedrock import BedrockMultimodalEmbedder
from app.embedding.vertex import VertexMultimodalEmbedder


class _FakeBedrockClient:
    def __init__(self, dimension=8):
        self.dimension = dimension
        self.calls = []

    def invoke_model(self, modelId, body):
        request = json.loads(body)
        self.calls.append((modelId, request))
        count = len(request.get("texts") or request.get("images"))
        # a distinct vector per entry, so the pairing with the input shows
        vectors = [[float(index)] * self.dimension for index in range(count)]
        payload = {"embeddings": {"float": vectors}}

        return {"body": io.BytesIO(json.dumps(payload).encode())}


class _FakeVertexResult:
    def __init__(self, text_embedding=None, image_embedding=None):
        self.text_embedding = text_embedding
        self.image_embedding = image_embedding


class _FakeVertexClient:
    def __init__(self, dimension=8):
        self.dimension = dimension
        self.calls = []

    def get_embeddings(self, **kwargs):
        self.calls.append(kwargs)
        vector = [0.2] * self.dimension

        if "image" in kwargs:
            return _FakeVertexResult(image_embedding=vector)

        return _FakeVertexResult(text_embedding=vector)


def test_bedrock_text_and_image_share_model_and_space():
    client = _FakeBedrockClient(dimension=8)
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", client=client
    )

    text_vectors = embedder.embed_texts(["alfa", "beta"])
    image_vector = embedder.embed_image(b"jpeg-bytes", "image/jpeg")

    assert text_vectors == [[0.0] * 8, [1.0] * 8]
    assert image_vector == [0.0] * 8
    # same model for text and image: one vector space; the data URI carries
    # the image's own content type
    data_uri = "data:image/jpeg;base64," + base64.b64encode(b"jpeg-bytes").decode()
    assert client.calls == [
        (
            "cohere.embed-v4:0",
            {
                "texts": ["alfa", "beta"],
                "input_type": "search_document",
                "embedding_types": ["float"],
            },
        ),
        (
            "cohere.embed-v4:0",
            {
                "images": [data_uri],
                "input_type": "image",
                "embedding_types": ["float"],
            },
        ),
    ]


def test_bedrock_embed_texts_forwards_the_input_type():
    client = _FakeBedrockClient(dimension=8)
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", client=client
    )

    embedder.embed_texts(["alfa"], input_type="search_query")

    assert client.calls[-1][1]["input_type"] == "search_query"


def test_bedrock_coerces_float_output_dimension_to_int():
    # a gRPC Struct decodes numbers to float; Cohere rejects a float dimension
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", dimension=1024.0, client=object()
    )

    assert embedder.dimension == 1024
    assert type(embedder.dimension) is int


def test_vertex_text_and_image_share_model_and_space():
    client = _FakeVertexClient(dimension=8)
    # a float dimension, as decoded from the gRPC Struct
    embedder = VertexMultimodalEmbedder(
        "multimodalembedding@001",
        dimension=8.0,
        client=client,
        image_factory=lambda data: ("wrapped", data),
    )

    text_vectors = embedder.embed_texts(["alfa", "beta"])
    image_vector = embedder.embed_image(b"png-bytes", "image/png")

    assert text_vectors == [[0.2] * 8, [0.2] * 8]
    assert image_vector == [0.2] * 8
    # one call per text, then the wrapped image; the configured dimension
    # reaches every call, as an int
    assert client.calls == [
        {"contextual_text": "alfa", "dimension": 8},
        {"contextual_text": "beta", "dimension": 8},
        {"image": ("wrapped", b"png-bytes"), "dimension": 8},
    ]
    assert all(type(call["dimension"]) is int for call in client.calls)
