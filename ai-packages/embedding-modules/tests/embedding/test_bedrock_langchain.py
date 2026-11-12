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

"""The Bedrock models of the registry that langchain-aws serves: what the
adapter asks BedrockEmbeddings, and the request each family gets for it.

The real BedrockEmbeddings runs over a fake bedrock-runtime client that
records the request bodies and answers in the schema of the family, so a
langchain-aws release that changes a request shows up here."""

import base64
import io
import json

import pytest
from langchain_aws import BedrockEmbeddings

from app.embedding import bedrock_langchain, multimodal
from app.embedding.bedrock_langchain import LangchainBedrockEmbedder
from app.embedding.router import SkipRef

NOVA = "amazon.nova-2-multimodal-embeddings-v1:0"
TITAN = "amazon.titan-embed-image-v1"
COHERE_EN = "cohere.embed-english-v3"
COHERE_ML = "cohere.embed-multilingual-v3"

PNG = b"\x89PNG\r\n\x1a\n" + b"\x00" * 8
JPEG = b"\xff\xd8\xff\xe0" + b"\x00" * 8
GIF = b"GIF89a" + b"\x00" * 8
WEBP = b"RIFF\x00\x00\x00\x00WEBP" + b"\x00" * 4
BMP = b"BM" + b"\x00" * 14
TIFF = b"II*\x00" + b"\x00" * 12
SVG = b'<svg xmlns="http://www.w3.org/2000/svg"></svg>'


def _b64(data):
    return base64.b64encode(data).decode()


class _FakeRuntime:
    """Records every request body; the n-th vector it hands out is
    [n, 0, 0, 0], in the response schema of the family of the model."""

    def __init__(self):
        self.bodies = []
        self.vectors = 0

    def _vector(self):
        self.vectors += 1
        return [float(self.vectors), 0.0, 0.0, 0.0]

    def invoke_model(self, body, modelId, accept, contentType):
        request = json.loads(body)
        self.bodies.append(request)

        if "cohere" in modelId:
            count = len(request.get("texts") or request.get("images"))
            payload = {"embeddings": [self._vector() for _ in range(count)]}
        elif "nova" in modelId:
            payload = {"embeddings": [{"embedding": self._vector()}]}
        else:
            payload = {"embedding": self._vector()}

        return {"body": io.BytesIO(json.dumps(payload).encode())}


@pytest.fixture
def runtime(monkeypatch):
    """bedrock_embeddings builds the real BedrockEmbeddings on the fake
    client, with the arguments the module gives it."""
    runtime = _FakeRuntime()

    monkeypatch.setattr(
        bedrock_langchain,
        "BedrockEmbeddings",
        lambda **kwargs: BedrockEmbeddings(client=runtime, **kwargs),
    )

    return runtime


def _embedder(model, aws_bedrock=None):
    configuration = {"model_type": "aws_bedrock", "model": model}
    if aws_bedrock is not None:
        configuration["aws_bedrock"] = aws_bedrock

    return multimodal.find_multimodal_embedder(configuration).factory(configuration)


def _nova_text(value):
    return {
        "taskType": "SINGLE_EMBEDDING",
        "singleEmbeddingParams": {
            "embeddingPurpose": "GENERIC_INDEX",
            "text": {"truncationMode": "END", "value": value},
        },
    }


@pytest.mark.parametrize(
    ("model", "documents", "query"),
    [
        pytest.param(
            NOVA,
            [_nova_text("uno"), _nova_text("due")],
            [_nova_text("tre")],
            id="nova 2",
        ),
        pytest.param(
            TITAN,
            [{"inputText": "uno"}, {"inputText": "due"}],
            [{"inputText": "tre"}],
            id="titan multimodal",
        ),
        pytest.param(
            COHERE_EN,
            [{"input_type": "search_document", "texts": ["uno", "due"]}],
            [{"input_type": "search_query", "texts": ["tre"]}],
            id="cohere english v3",
        ),
        pytest.param(
            COHERE_ML,
            [{"input_type": "search_document", "texts": ["uno", "due"]}],
            [{"input_type": "search_query", "texts": ["tre"]}],
            id="cohere multilingual v3",
        ),
    ],
)
def test_text_reaches_the_provider_in_the_request_of_its_family(
    runtime, model, documents, query
):
    embedder = _embedder(model)

    embedder.embed_texts(["uno", "due"], "search_document")
    assert runtime.bodies == documents

    runtime.bodies.clear()
    embedder.embed_texts(["tre"], "search_query")
    assert runtime.bodies == query


@pytest.mark.parametrize(
    ("model", "request_body"),
    [
        pytest.param(
            NOVA,
            {
                "taskType": "SINGLE_EMBEDDING",
                "singleEmbeddingParams": {
                    "embeddingPurpose": "GENERIC_INDEX",
                    "image": {"format": "png", "source": {"bytes": _b64(PNG)}},
                },
            },
            id="nova 2",
        ),
        pytest.param(TITAN, {"inputImage": _b64(PNG)}, id="titan multimodal"),
        pytest.param(
            COHERE_ML,
            {
                "input_type": "image",
                "images": [f"data:image/png;base64,{_b64(PNG)}"],
                "embedding_types": ["float"],
            },
            id="cohere multilingual v3",
        ),
    ],
)
def test_an_image_reaches_the_provider_in_the_request_of_its_family(
    runtime, model, request_body
):
    vector = _embedder(model).embed_image(PNG, "image/png")

    assert runtime.bodies == [request_body]
    assert vector == [1.0, 0.0, 0.0, 0.0]


@pytest.mark.parametrize("model", [NOVA, TITAN, COHERE_EN])
def test_the_vectors_come_back_one_per_text_in_order(runtime, model):
    vectors = _embedder(model).embed_texts(["uno", "due", "tre"])

    assert [vector[0] for vector in vectors] == [1.0, 2.0, 3.0]


@pytest.mark.parametrize("model", [NOVA, TITAN, COHERE_ML])
def test_the_output_dimension_of_the_configuration_is_not_sent(runtime, model):
    embedder = _embedder(model, {"region_name": "us-east-1", "output_dimension": 384.0})

    embedder.embed_texts(["uno"])
    embedder.embed_texts(["due"], "search_query")
    embedder.embed_image(PNG, "image/png")

    sent = json.dumps(runtime.bodies)
    for key in ("dimensions", "outputEmbeddingLength", "embeddingDimension"):
        assert key not in sent


@pytest.mark.parametrize(
    "data",
    [
        pytest.param(BMP, id="bmp"),
        pytest.param(TIFF, id="tiff"),
        pytest.param(SVG, id="svg"),
        pytest.param(b"not an image", id="unknown bytes"),
    ],
)
def test_an_image_in_another_format_is_skipped_without_a_request(runtime, data):
    with pytest.raises(SkipRef):
        _embedder(NOVA).embed_image(data, "image/png")

    assert runtime.bodies == []


@pytest.mark.parametrize(
    ("data", "format"),
    [
        pytest.param(JPEG, "jpeg", id="jpeg"),
        pytest.param(GIF, "gif", id="gif"),
        pytest.param(WEBP, "webp", id="webp"),
    ],
)
def test_png_jpeg_gif_and_webp_images_are_embedded(runtime, data, format):
    # the declared content type does not matter: the format is read from
    # the bytes
    _embedder(NOVA).embed_image(data, "application/octet-stream")

    image = runtime.bodies[0]["singleEmbeddingParams"]["image"]
    assert image["format"] == format


def test_the_langchain_embedder_has_no_mixed_input():
    assert not hasattr(LangchainBedrockEmbedder, "embed_mixed")


@pytest.mark.parametrize("model", [NOVA, TITAN, COHERE_EN, COHERE_ML])
def test_langchain_aws_still_embeds_images_with_every_registered_model(model):
    # a canary on the private capability check of langchain-aws: an upgrade
    # that drops a registered family fails here, before the provider does
    assert BedrockEmbeddings(model_id=model, client=object())._supports_image()
