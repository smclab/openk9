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

"""Bedrock (Cohere Embed v4) embeds text + image together in one call and
returns a single fused vector, produced by the model (no module-side
fusion). A fake boto3 client stands in for the network."""

import base64
import io
import json

from app.embedding.bedrock import BedrockMultimodalEmbedder


class _FakeMixedClient:
    def __init__(self, dimension=8):
        self.dimension = dimension
        self.calls = []

    def invoke_model(self, modelId, body):
        request = json.loads(body)
        self.calls.append((modelId, request))
        # one vector per input: the mixed call sends exactly one input
        vectors = [[0.3] * self.dimension for _ in request["inputs"]]
        payload = {"embeddings": {"float": vectors}}

        return {"body": io.BytesIO(json.dumps(payload).encode())}


def test_embed_mixed_returns_single_vector_from_one_input():
    client = _FakeMixedClient(dimension=8)
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", client=client
    )

    vector = embedder.embed_mixed("un gatto", b"png-bytes", "image/png")

    # a single vector (the first and only one), not the list of vectors
    assert vector == [0.3] * 8


def test_embed_mixed_packs_text_and_image_in_one_input():
    client = _FakeMixedClient(dimension=8)
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", client=client
    )

    embedder.embed_mixed("un gatto", b"png-bytes", "image/png")

    model_id, request = client.calls[-1]
    assert model_id == "cohere.embed-v4:0"
    # text and image travel together in a single input's content array
    data_uri = "data:image/png;base64," + base64.b64encode(b"png-bytes").decode()
    assert request["inputs"] == [
        {
            "content": [
                {"type": "text", "text": "un gatto"},
                {"type": "image_url", "image_url": {"url": data_uri}},
            ]
        }
    ]
    # query-time input_type (asymmetric models such as Cohere Embed v4)
    assert request["input_type"] == "search_query"


def test_embed_mixed_applies_output_dimension():
    client = _FakeMixedClient(dimension=8)
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", dimension=8, client=client
    )

    embedder.embed_mixed("un gatto", b"png-bytes", "image/png")

    assert client.calls[-1][1]["output_dimension"] == 8
