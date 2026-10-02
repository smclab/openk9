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

"""initialize_embedding_model picks the langchain class of the configured
provider and hands it the model and its settings; an unknown or missing
provider falls back to OpenAI. The Ollama keys are covered by
test_ollama_parameters.py."""

import pytest

from app import server as server_module

LANGCHAIN_CLASSES = (
    "OpenAIEmbeddings",
    "WatsonxEmbeddings",
    "BatchedVertexAIEmbeddings",
    "HuggingFaceEmbeddings",
    "BedrockEmbeddings",
)


@pytest.fixture(autouse=True)
def _fake_langchain_classes(monkeypatch):
    """Every class answers (its name, the keyword arguments it received)."""
    for name in LANGCHAIN_CLASSES:
        monkeypatch.setattr(
            server_module, name, lambda name=name, **kwargs: (name, kwargs)
        )
    monkeypatch.setattr(server_module, "_apply_credentials", lambda configuration: None)


def test_openai_uses_the_api_url_when_given():
    assert server_module.initialize_embedding_model(
        {"model_type": "openai", "model": "m", "api_url": "http://proxy"}
    ) == ("OpenAIEmbeddings", {"model": "m", "base_url": "http://proxy"})


def test_openai_without_api_url_uses_the_default_endpoint():
    assert server_module.initialize_embedding_model(
        {"model_type": "openai", "model": "m", "api_url": ""}
    ) == ("OpenAIEmbeddings", {"model": "m"})


def test_watsonx_gets_the_project_and_the_provider_defaults():
    # no embed params: the chunks reach the model whole, with the defaults of
    # the provider
    assert server_module.initialize_embedding_model(
        {
            "model_type": "watsonx",
            "model": "m",
            "api_url": "http://watsonx",
            "watsonx_project_id": "project-id",
        }
    ) == (
        "WatsonxEmbeddings",
        {"model_id": "m", "url": "http://watsonx", "project_id": "project-id"},
    )


def test_vertex_takes_the_project_from_the_credentials():
    assert server_module.initialize_embedding_model(
        {
            "model_type": "chat_vertex_ai",
            "model": "m",
            "chat_vertex_ai_model_garden": {
                "credentials": {"quota_project_id": "project-id"}
            },
        }
    ) == ("BatchedVertexAIEmbeddings", {"model_name": "m", "project": "project-id"})


def test_hugging_face_loads_the_model_by_name():
    assert server_module.initialize_embedding_model(
        {"model_type": "hugging_face", "model": "m"}
    ) == ("HuggingFaceEmbeddings", {"model_name": "m"})


def test_bedrock_gets_the_region():
    assert server_module.initialize_embedding_model(
        {
            "model_type": "aws_bedrock",
            "model": "m",
            "aws_bedrock": {"region_name": "eu-west-1"},
        }
    ) == ("BedrockEmbeddings", {"model_id": "m", "region_name": "eu-west-1"})


def test_an_unknown_provider_falls_back_to_openai():
    assert server_module.initialize_embedding_model(
        {"model_type": "unknown", "model": "m", "api_url": ""}
    ) == ("OpenAIEmbeddings", {"model": "m"})


def test_an_unknown_provider_keeps_the_api_url():
    assert server_module.initialize_embedding_model(
        {"model_type": "unknown", "model": "m", "api_url": "http://proxy"}
    ) == ("OpenAIEmbeddings", {"model": "m", "base_url": "http://proxy"})


def test_a_missing_provider_and_model_use_the_openai_default():
    assert server_module.initialize_embedding_model({}) == (
        "OpenAIEmbeddings",
        {"model": server_module.DEFAULT_MODEL},
    )
