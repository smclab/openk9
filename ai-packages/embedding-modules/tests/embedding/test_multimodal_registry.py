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

"""The registry selects the concrete embedder from the provider and maps
the jsonConfig onto its constructor. An unregistered provider (e.g. a
text-only one marked multimodal by mistake) raises. Concrete classes are
faked so the test needs no cloud SDK."""

import pytest

from app.embedding import bedrock, multimodal, vertex


def test_unknown_provider_has_no_multimodal_embedder():
    with pytest.raises(ValueError):
        multimodal.build_multimodal_embedder({"model_type": "openai", "model": "x"})


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

    embedder = multimodal.build_multimodal_embedder(configuration)

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

    embedder = multimodal.build_multimodal_embedder(configuration)

    assert isinstance(embedder, _Fake)
    # explicit project wins over the ADC quota_project_id; location flows through
    assert captured == {
        "model_id": "multimodalembedding@001",
        "project": "explicit-proj",
        "location": "europe-west1",
        "dimension": 1408,
    }
