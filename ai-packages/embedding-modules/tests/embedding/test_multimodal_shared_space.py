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
injected client, so both come back with the same dimension: one shared
vector space. Fake clients stand in for boto3 / the Vertex SDK."""

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
        self.calls.append(request)
        count = len(request.get("texts") or request.get("images"))
        vectors = [[0.1] * self.dimension for _ in range(count)]
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
    image_vector = embedder.embed_image(b"png-bytes", "image/png")

    assert len(text_vectors) == 2
    # same model -> same dimension for text and image: one vector space
    assert len(text_vectors[0]) == len(image_vector) == 8
    assert client.calls[0]["input_type"] == "search_document"
    assert client.calls[-1]["input_type"] == "image"


def test_bedrock_coerces_float_output_dimension_to_int():
    # a gRPC Struct decodes numbers to float; Cohere rejects a float dimension
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", dimension=1024.0, client=object()
    )

    assert embedder.dimension == 1024
    assert type(embedder.dimension) is int


def test_vertex_text_and_image_share_model_and_space():
    client = _FakeVertexClient(dimension=8)
    embedder = VertexMultimodalEmbedder(
        "multimodalembedding@001", client=client, image_factory=lambda data: data
    )

    text_vectors = embedder.embed_texts(["alfa", "beta"])
    image_vector = embedder.embed_image(b"png-bytes", "image/png")

    assert len(text_vectors) == 2
    assert len(text_vectors[0]) == len(image_vector) == 8
    assert "contextual_text" in client.calls[0]
    assert "image" in client.calls[-1]
