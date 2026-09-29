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

"""Cohere Embed on Bedrock takes at most 96 texts per request: the
embedder splits a longer list into requests of at most 96 texts and
returns the vectors in the order of the texts. A fake boto3 client that
rejects oversized requests, like Bedrock does, stands in for the
network."""

import io
import json

import pytest

from app.embedding.bedrock import BedrockMultimodalEmbedder


class _TooManyTexts(Exception):
    pass


class _FakeLimitedClient:
    """Rejects more than 96 texts per request; the vector of text "t<i>"
    is [i, 0, 0, 0], so the order of the answer is checkable."""

    def __init__(self):
        self.calls = []

    def invoke_model(self, modelId, body):
        request = json.loads(body)
        self.calls.append(request)

        if len(request["texts"]) > 96:
            raise _TooManyTexts(len(request["texts"]))

        vectors = [[float(text[1:]), 0.0, 0.0, 0.0] for text in request["texts"]]
        payload = {"embeddings": {"float": vectors}}

        return {"body": io.BytesIO(json.dumps(payload).encode())}


def _texts(count):
    return [f"t{index}" for index in range(count)]


def _embedder(client, dimension=None):
    return BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", dimension=dimension, client=client
    )


def test_long_list_is_split_in_requests_of_at_most_96_texts():
    client = _FakeLimitedClient()

    vectors = _embedder(client).embed_texts(_texts(1000))

    assert [len(call["texts"]) for call in client.calls] == [96] * 10 + [40]
    assert len(vectors) == 1000


def test_vectors_come_back_in_the_order_of_the_texts():
    client = _FakeLimitedClient()

    vectors = _embedder(client).embed_texts(_texts(250))

    assert [vector[0] for vector in vectors] == [float(index) for index in range(250)]


@pytest.mark.parametrize(
    ("count", "sizes"),
    [(1, [1]), (96, [96]), (97, [96, 1]), (192, [96, 96])],
)
def test_requests_around_the_limit(count, sizes):
    client = _FakeLimitedClient()

    _embedder(client).embed_texts(_texts(count))

    assert [len(call["texts"]) for call in client.calls] == sizes


def test_every_request_carries_input_type_and_dimension():
    client = _FakeLimitedClient()

    _embedder(client, dimension=1024).embed_texts(_texts(200), "search_query")

    assert len(client.calls) == 3
    assert all(call["input_type"] == "search_query" for call in client.calls)
    assert all(call["output_dimension"] == 1024 for call in client.calls)
