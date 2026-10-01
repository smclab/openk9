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

"""The registry selects the concrete embedder from provider and model and
maps the jsonConfig onto its constructor. A model that is not registered is
text-only: the registry has nothing for it. Concrete classes are faked so the
test needs no cloud SDK."""

import pytest

from app.embedding import bedrock, multimodal, vertex


@pytest.mark.parametrize(
    ("provider", "model"),
    [
        pytest.param("openai", "text-embedding-3-small", id="text-only provider"),
        pytest.param("aws_bedrock", "amazon.titan-embed-text-v2:0", id="bedrock titan"),
        pytest.param("aws_bedrock", "cohere.embed-english-v3", id="bedrock cohere v3"),
        pytest.param("chat_vertex_ai", "text-embedding-005", id="vertex text"),
        pytest.param("my-gateway", "cohere.embed-v4:0", id="custom provider"),
    ],
)
def test_a_model_outside_the_registry_has_no_embedder(provider, model):
    assert (
        multimodal.find_multimodal_embedder({"model_type": provider, "model": model})
        is None
    )


@pytest.mark.parametrize(
    ("provider", "model", "name"),
    [
        pytest.param("aws_bedrock", "cohere.embed-v4:0", "bedrock-cohere-v4", id="model id"),
        pytest.param("aws_bedrock", "eu.cohere.embed-v4:0", "bedrock-cohere-v4", id="eu profile"),
        pytest.param("aws_bedrock", "global.cohere.embed-v4:0", "bedrock-cohere-v4", id="global profile"),
        pytest.param(
            "chat_vertex_ai",
            "multimodalembedding@001",
            "vertex-multimodalembedding",
            id="vertex",
        ),
    ],
)
def test_a_registered_model_is_found_by_provider_and_model(provider, model, name):
    entry = multimodal.find_multimodal_embedder({"model_type": provider, "model": model})

    assert entry.name == name


def test_bedrock_provider_maps_to_bedrock_embedder(monkeypatch):
    captured = {}

    class _Fake:
        def __init__(self, model_id, region_name, dimension=None, client=None):
            captured.update(
                model_id=model_id, region_name=region_name, dimension=dimension
            )

    monkeypatch.setattr(bedrock, "BedrockMultimodalEmbedder", _Fake)

    configuration = {
        "model_type": "aws_bedrock",
        "model": "cohere.embed-v4:0",
        "aws_bedrock": {"region_name": "us-east-1", "output_dimension": 1024},
    }

    embedder = multimodal.find_multimodal_embedder(configuration).factory(configuration)

    assert isinstance(embedder, _Fake)
    assert captured == {
        "model_id": "cohere.embed-v4:0",
        "region_name": "us-east-1",
        "dimension": 1024,
    }


def test_vertex_provider_maps_to_vertex_embedder(monkeypatch):
    captured = {}

    class _Fake:
        def __init__(
            self,
            model_id,
            project=None,
            location=None,
            dimension=None,
            client=None,
            image_factory=None,
        ):
            captured.update(
                model_id=model_id,
                project=project,
                location=location,
                dimension=dimension,
            )

    monkeypatch.setattr(vertex, "VertexMultimodalEmbedder", _Fake)

    configuration = {
        "model_type": "chat_vertex_ai",
        "model": "multimodalembedding@001",
        "chat_vertex_ai_model_garden": {
            "credentials": {"quota_project_id": "adc-proj"},
            "project": "explicit-proj",
            "location": "europe-west1",
            "dimension": 1408,
        },
    }

    embedder = multimodal.find_multimodal_embedder(configuration).factory(configuration)

    assert isinstance(embedder, _Fake)
    # explicit project wins over the ADC quota_project_id; location flows through
    assert captured == {
        "model_id": "multimodalembedding@001",
        "project": "explicit-proj",
        "location": "europe-west1",
        "dimension": 1408,
    }


def test_bedrock_without_its_block_gets_no_region_nor_dimension(monkeypatch):
    captured = {}

    class _Fake:
        def __init__(self, model_id, region_name, dimension=None, client=None):
            captured.update(
                model_id=model_id, region_name=region_name, dimension=dimension
            )

    monkeypatch.setattr(bedrock, "BedrockMultimodalEmbedder", _Fake)

    configuration = {"model_type": "aws_bedrock", "model": "cohere.embed-v4:0"}

    multimodal.find_multimodal_embedder(configuration).factory(configuration)

    assert captured == {
        "model_id": "cohere.embed-v4:0",
        "region_name": None,
        "dimension": None,
    }


def test_vertex_project_falls_back_to_the_adc_quota_project(monkeypatch):
    captured = {}

    class _Fake:
        def __init__(
            self,
            model_id,
            project=None,
            location=None,
            dimension=None,
            client=None,
            image_factory=None,
        ):
            captured.update(project=project)

    monkeypatch.setattr(vertex, "VertexMultimodalEmbedder", _Fake)

    configuration = {
        "model_type": "chat_vertex_ai",
        "model": "multimodalembedding@001",
        "chat_vertex_ai_model_garden": {
            "credentials": {"quota_project_id": "adc-proj"},
        },
    }

    multimodal.find_multimodal_embedder(configuration).factory(configuration)

    assert captured == {"project": "adc-proj"}
