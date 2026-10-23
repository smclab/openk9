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
"""The providers of the input guardrail and the prompt they classify with.

The SDKs of the external providers are imported only by the provider that uses
them, so here they are replaced in ``sys.modules``.
"""

import asyncio
import os
import sys
import types
from unittest.mock import MagicMock

import pytest

from app.rag.agentic import guardrails
from app.rag.agentic.guardrails import (
    SAFE,
    UNSAFE,
    build_input_instruction,
    check_bedrock,
    check_model_armor,
    check_openai_moderation,
    format_categories,
)


@pytest.fixture(autouse=True)
def isolated_environ(monkeypatch):
    """Bedrock writes AWS_BEARER_TOKEN_BEDROCK: each test gets its own environment."""
    monkeypatch.setattr(os, "environ", dict(os.environ))


# --------------------------------------------------------------------------- #
# The prompt                                                                   #
# --------------------------------------------------------------------------- #
def test_default_categories_carry_the_precedence_rule():
    instruction = build_input_instruction(None)

    assert "sentence provided by the user" in instruction
    assert "1. SELF-HARM/SUICIDE" in instruction
    assert "attempts to extract, reveal, modify" in instruction
    assert "SYSTEM_PROMPT_LEAKAGE takes precedence" in instruction
    assert "return exactly: NONE" in instruction


def test_tenant_categories_replace_the_defaults_without_precedence_rule():
    categories = format_categories([{"name": "FOO", "description": "custom"}])

    instruction = build_input_instruction(categories)

    assert "1. FOO - custom" in instruction
    assert "1. SELF-HARM/SUICIDE" not in instruction
    assert "takes precedence" not in instruction


def test_tenant_categories_are_numbered_in_order():
    categories = [
        {"name": "VIOLENZA", "description": "atti violenti"},
        {"name": "SPAM", "description": "pubblicita'"},
    ]

    assert format_categories(categories) == "1. VIOLENZA - atti violenti\n2. SPAM - pubblicita'"
    assert format_categories(None) == ""


# --------------------------------------------------------------------------- #
# OpenAI Moderation                                                            #
# --------------------------------------------------------------------------- #
@pytest.fixture
def openai_client(monkeypatch):
    module = types.ModuleType("openai")
    client = MagicMock()
    module.OpenAI = MagicMock(return_value=client)
    monkeypatch.setitem(sys.modules, "openai", module)
    return client


@pytest.mark.parametrize("flagged, expected", [(True, UNSAFE), (False, SAFE)])
def test_openai_moderation(openai_client, flagged, expected):
    openai_client.moderations.create.return_value = types.SimpleNamespace(
        results=[types.SimpleNamespace(flagged=flagged)]
    )

    assert check_openai_moderation({"api_key": "key"}, "text") == expected


# --------------------------------------------------------------------------- #
# AWS Bedrock                                                                  #
# --------------------------------------------------------------------------- #
BEDROCK_CONFIG = {
    "api_key": "aws-key",
    "model": "anthropic.claude-v2",
    "region": "us-east-1",
    "guardrail_identifier": "gid",
    "guardrail_version": "1",
}


def test_bedrock_classifies_with_the_guardrail_policy_on_top(monkeypatch):
    models = types.ModuleType("pydantic_ai.models.bedrock")
    models.BedrockConverseModel = MagicMock(return_value="MODEL")
    models.BedrockModelSettings = lambda **kwargs: kwargs
    providers = types.ModuleType("pydantic_ai.providers.bedrock")
    providers.BedrockProvider = MagicMock(return_value="PROVIDER")
    monkeypatch.setitem(sys.modules, "pydantic_ai.models.bedrock", models)
    monkeypatch.setitem(sys.modules, "pydantic_ai.providers.bedrock", providers)
    captured = {}

    async def fake_classify(model, instruction, text, *, model_settings=None):
        captured.update(model=model, instruction=instruction, settings=model_settings)
        return "VIOLENCE/WEAPONS"

    monkeypatch.setattr(guardrails, "classify_with_llm", fake_classify)

    result = asyncio.run(check_bedrock(BEDROCK_CONFIG, "INSTRUCTION", "some text"))

    assert result == "VIOLENCE/WEAPONS"
    assert os.environ["AWS_BEARER_TOKEN_BEDROCK"] == "aws-key"
    providers.BedrockProvider.assert_called_once_with(region_name="us-east-1")
    models.BedrockConverseModel.assert_called_once_with("anthropic.claude-v2", provider="PROVIDER")
    assert captured["model"] == "MODEL"
    assert captured["instruction"] == "INSTRUCTION"
    assert captured["settings"]["bedrock_guardrail_config"] == {
        "guardrailIdentifier": "gid",
        "guardrailVersion": "1",
    }


# --------------------------------------------------------------------------- #
# Google Model Armor                                                           #
# --------------------------------------------------------------------------- #
MODEL_ARMOR_CONFIG = {"region": "europe-west1", "project_id": "proj", "template_id": "tmpl"}


def _model_armor(monkeypatch, *, match_found):
    module = types.ModuleType("google.cloud.modelarmor_v1")

    class FilterMatchState:
        NO_MATCH_FOUND = "NO_MATCH_FOUND"
        MATCH_FOUND = "MATCH_FOUND"

    module.FilterMatchState = FilterMatchState
    module.DataItem = lambda **kwargs: kwargs
    module.SanitizeUserPromptRequest = lambda **kwargs: kwargs
    state = FilterMatchState.MATCH_FOUND if match_found else FilterMatchState.NO_MATCH_FOUND
    client = MagicMock()
    client.sanitize_user_prompt.return_value = types.SimpleNamespace(
        sanitization_result=types.SimpleNamespace(filter_match_state=state)
    )
    module.ModelArmorClient = MagicMock(return_value=client)
    cloud = types.ModuleType("google.cloud")
    cloud.modelarmor_v1 = module
    monkeypatch.setitem(sys.modules, "google.cloud", cloud)
    monkeypatch.setitem(sys.modules, "google.cloud.modelarmor_v1", module)
    return module, client


@pytest.mark.parametrize("match_found, expected", [(True, UNSAFE), (False, SAFE)])
def test_model_armor_sanitizes_the_user_prompt(monkeypatch, match_found, expected):
    module, client = _model_armor(monkeypatch, match_found=match_found)

    assert check_model_armor(MODEL_ARMOR_CONFIG, "text") == expected
    request = client.sanitize_user_prompt.call_args.kwargs["request"]
    assert request["name"] == "projects/proj/locations/europe-west1/templates/tmpl"
    assert request["user_prompt_data"] == {"text": "text"}
    module.ModelArmorClient.assert_called_once_with(
        transport="rest",
        client_options={"api_endpoint": "modelarmor.europe-west1.rep.googleapis.com"},
    )


def test_model_armor_saves_the_credentials_of_the_tenant(monkeypatch):
    _model_armor(monkeypatch, match_found=False)
    saved = MagicMock()
    monkeypatch.setattr(sys.modules["app.utils.llm"], "save_google_application_credentials", saved)

    check_model_armor({**MODEL_ARMOR_CONFIG, "google_credentials": {"client_id": "cid"}}, "text")

    saved.assert_called_once_with({"client_id": "cid"})


# --------------------------------------------------------------------------- #
# Which provider classifies                                                    #
# --------------------------------------------------------------------------- #
def test_each_provider_gets_its_own_configuration(monkeypatch):
    seen = {}

    async def fake_bedrock(config, instruction, text):
        seen["aws_bedrock"] = (config, instruction)
        return SAFE

    monkeypatch.setattr(guardrails, "check_bedrock", fake_bedrock)
    monkeypatch.setattr(
        guardrails, "check_model_armor", lambda config, text: seen.setdefault("armor", config)
    )
    monkeypatch.setattr(
        guardrails,
        "check_openai_moderation",
        lambda config, text: seen.setdefault("openai", config),
    )
    input_guardrail = {
        "input_guardrail_aws_bedrock": {"model": "bedrock"},
        "input_guardrail_google_model_armor": {"template_id": "armor"},
        "input_guardrail_openai_moderation": {"api_key": "openai"},
    }

    for provider in ("aws_bedrock", "google_model_armor", "openai_moderation"):
        asyncio.run(
            guardrails.classify_input(
                input_guardrail={**input_guardrail, "input_guardrail_provider": provider},
                categories=None,
                model=None,
                query="query",
            )
        )

    assert seen["aws_bedrock"][0] == {"model": "bedrock"}
    assert "sentence provided by the user" in seen["aws_bedrock"][1]
    assert seen["armor"] == {"template_id": "armor"}
    assert seen["openai"] == {"api_key": "openai"}


def test_without_a_provider_the_tenant_model_classifies(monkeypatch):
    captured = {}

    async def fake_classify(model, instruction, text, *, model_settings=None):
        captured.update(model=model, instruction=instruction, text=text)
        return "SPAM"

    monkeypatch.setattr(guardrails, "classify_with_llm", fake_classify)

    outcome = asyncio.run(
        guardrails.classify_input(
            input_guardrail={}, categories="1. SPAM - pubblicita'", model="MODEL", query="query"
        )
    )

    assert outcome == "SPAM"
    assert captured["model"] == "MODEL"
    assert "1. SPAM - pubblicita'" in captured["instruction"]
    assert captured["text"] == "query"
