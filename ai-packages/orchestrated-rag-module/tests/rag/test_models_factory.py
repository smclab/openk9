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

"""build_model turns the tenant LLM configuration into the PydanticAI model of
its provider, without reaching the network."""

import builtins
import sys
from unittest.mock import MagicMock

import pytest
from pydantic_ai.models.bedrock import BedrockConverseModel
from pydantic_ai.models.google import GoogleModel
from pydantic_ai.models.openai import OpenAIChatModel

from app.rag.agentic import models_factory
from app.rag.agentic.models_factory import (
    DEFAULT_MODEL,
    build_model,
    build_model_settings,
)

GOOGLE_CREDENTIALS = {"quota_project_id": "project-1", "type": "authorized_user"}


@pytest.fixture
def saved_credentials(monkeypatch):
    save = MagicMock()
    monkeypatch.setattr(models_factory, "save_google_application_credentials", save)
    return save


def test_openai_uses_the_configured_endpoint_and_key():
    model = build_model(
        {
            "model_type": "openai",
            "api_url": "https://llm.example/v1",
            "api_key": "sk-1",
            "model": "gpt-4.1",
        }
    )

    assert isinstance(model, OpenAIChatModel)
    assert model.model_name == "gpt-4.1"
    assert str(model.client.base_url) == "https://llm.example/v1/"
    assert model.client.api_key == "sk-1"


def test_openai_without_api_url_uses_the_default_endpoint():
    model = build_model({"model_type": "openai", "api_url": "", "api_key": "sk-1"})

    assert str(model.client.base_url) == "https://api.openai.com/v1/"


def test_missing_model_type_and_model_fall_back_to_the_defaults():
    model = build_model({"model_type": None, "api_key": "sk-1", "model": None})

    assert isinstance(model, OpenAIChatModel)
    assert model.model_name == DEFAULT_MODEL


def test_unknown_model_type_falls_back_to_openai():
    model = build_model({"model_type": "some-future-provider", "api_key": "sk-1"})

    assert isinstance(model, OpenAIChatModel)


def test_vertex_ai_saves_the_credentials_and_uses_their_project(saved_credentials):
    model = build_model(
        {
            "model_type": "chat_vertex_ai",
            "model": "gemini-2.5-flash",
            "chat_vertex_ai_credentials": GOOGLE_CREDENTIALS,
        }
    )

    assert isinstance(model, GoogleModel)
    assert model.model_name == "gemini-2.5-flash"
    assert model.client._api_client.vertexai is True
    assert model.client._api_client.project == "project-1"
    saved_credentials.assert_called_once_with(GOOGLE_CREDENTIALS)


def test_vertex_model_garden_calls_the_endpoint_with_the_oauth_token(
    monkeypatch, saved_credentials
):
    credentials = MagicMock(token="oauth-token")
    google_auth = MagicMock()
    google_auth.default.return_value = (credentials, "project-1")
    monkeypatch.setitem(sys.modules, "google.auth", google_auth)

    model = build_model(
        {
            "model_type": "chat_vertex_ai_model_garden",
            "model": "llama-4",
            "chat_vertex_ai_model_garden": {
                "credentials": GOOGLE_CREDENTIALS,
                "endpoint_id": "endpoint.example",
                "location": "europe-west4",
            },
        }
    )

    assert isinstance(model, OpenAIChatModel)
    assert str(model.client.base_url) == (
        "https://endpoint.example/v1/projects/project-1"
        "/locations/europe-west4/endpoints/openapi/"
    )
    assert model.client.api_key == "oauth-token"
    credentials.refresh.assert_called_once()
    saved_credentials.assert_called_once_with(GOOGLE_CREDENTIALS)


def test_bedrock_uses_the_configured_region_and_api_key():
    model = build_model(
        {
            "model_type": "aws_bedrock",
            "api_key": "bedrock-key",
            "model": "eu.anthropic.claude-sonnet",
            "aws_bedrock": {"region_name": "eu-central-1"},
        }
    )

    assert isinstance(model, BedrockConverseModel)
    assert model.client.meta.region_name == "eu-central-1"


@pytest.mark.parametrize("model_type", ["ollama", "watsonx", "hugging-face-custom"])
def test_custom_providers_are_refused_until_they_have_a_model(model_type):
    with pytest.raises(ValueError, match=model_type):
        build_model({"model_type": model_type, "api_url": "http://llm:8080"})


def test_a_provider_without_its_sdk_says_which_extra_is_missing(monkeypatch):
    real_import = builtins.__import__

    def import_without_google(name, *args, **kwargs):
        if name == "pydantic_ai.models.google":
            raise ImportError(
                'Please install `google-genai` to use the Google model, you can use '
                'the `google` optional group — `pip install "pydantic-ai-slim[google]"`'
            )
        return real_import(name, *args, **kwargs)

    monkeypatch.setattr(builtins, "__import__", import_without_google)

    with pytest.raises(ImportError, match=r"pydantic-ai-slim\[google\]"):
        build_model(
            {
                "model_type": "chat_vertex_ai",
                "chat_vertex_ai_credentials": GOOGLE_CREDENTIALS,
            }
        )


def test_model_settings_carry_the_temperature():
    assert build_model_settings(0) == {"temperature": 0}


def test_no_temperature_keeps_the_provider_default():
    assert build_model_settings() is None
