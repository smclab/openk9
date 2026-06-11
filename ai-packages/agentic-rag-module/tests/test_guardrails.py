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


import os
from unittest.mock import patch

import pytest

from app.utils.guardrails import GuardrailType, initialize_guardrail

AWS_BEDROCK_CONFIGURATION = {
    "api_key": "aws-api-key",
    "model": "anthropic.claude-v2",
    "region": "us-east-1",
    "guardrail_identifier": "my-guardrail",
    "guardrail_version": "1",
}

GOOGLE_MODEL_ARMOR_CONFIGURATION = {
    "google_credentials": {"client_id": "client-id"},
    "project_id": "project-id",
    "template_id": "template-id",
    "region": "europe-west1",
}

OPENAI_MODERATION_CONFIGURATION = {
    "api_key": "openai-api-key",
}


@pytest.fixture(autouse=True)
def isolated_environ(monkeypatch):
    monkeypatch.setattr(os, "environ", dict(os.environ))


def test_openai_moderation_without_guardrail_type_key():
    with patch("app.utils.guardrails.OpenAIModerationChain") as moderation_chain:
        guardrail = initialize_guardrail(
            OPENAI_MODERATION_CONFIGURATION,
            guardrail_type=GuardrailType.OPENAI_MODERATION.value,
        )

    moderation_chain.assert_called_once_with()
    assert guardrail is moderation_chain.return_value
    assert os.environ["OPENAI_API_KEY"] == "openai-api-key"


def test_google_model_armor_without_guardrail_type_key():
    with (
        patch(
            "app.utils.guardrails.ModelArmorSanitizePromptRunnable"
        ) as sanitize_prompt,
        patch(
            "app.utils.guardrails.save_google_application_credentials"
        ) as save_credentials,
    ):
        guardrail = initialize_guardrail(
            GOOGLE_MODEL_ARMOR_CONFIGURATION,
            guardrail_type=GuardrailType.GOOGLE_MODEL_ARMOR.value,
        )

    save_credentials.assert_called_once_with({"client_id": "client-id"})
    sanitize_prompt.assert_called_once_with(
        project="project-id",
        location="europe-west1",
        template_id="template-id",
        fail_open=False,
    )
    assert guardrail is sanitize_prompt.return_value


def test_google_model_armor_response_without_guardrail_type_key():
    with (
        patch(
            "app.utils.guardrails.ModelArmorSanitizeResponseRunnable"
        ) as sanitize_response,
        patch("app.utils.guardrails.save_google_application_credentials"),
    ):
        guardrail = initialize_guardrail(
            GOOGLE_MODEL_ARMOR_CONFIGURATION,
            guardrail_type=GuardrailType.GOOGLE_MODEL_ARMOR_RESPONSE.value,
        )

    sanitize_response.assert_called_once_with(
        project="project-id",
        location="europe-west1",
        template_id="template-id",
        fail_open=False,
    )
    assert guardrail is sanitize_response.return_value


def test_aws_bedrock_without_guardrail_type_key():
    with patch("app.utils.guardrails.ChatBedrockConverse") as bedrock_converse:
        guardrail = initialize_guardrail(
            AWS_BEDROCK_CONFIGURATION,
            guardrail_type=GuardrailType.AWS_BEDROCK.value,
        )

    bedrock_converse.assert_called_once_with(
        model="anthropic.claude-v2",
        region_name="us-east-1",
        guardrail_config={
            "guardrailIdentifier": "my-guardrail",
            "guardrailVersion": "1",
        },
    )
    assert guardrail is bedrock_converse.return_value
    assert os.environ["AWS_BEARER_TOKEN_BEDROCK"] == "aws-api-key"


def test_guardrail_type_resolved_from_configuration_key():
    configuration = {
        **OPENAI_MODERATION_CONFIGURATION,
        "guardrail_type": GuardrailType.OPENAI_MODERATION.value,
    }

    with patch("app.utils.guardrails.OpenAIModerationChain") as moderation_chain:
        guardrail = initialize_guardrail(configuration)

    moderation_chain.assert_called_once_with()
    assert guardrail is moderation_chain.return_value


def test_guardrail_type_parameter_takes_precedence_over_configuration_key():
    configuration = {
        **OPENAI_MODERATION_CONFIGURATION,
        "guardrail_type": GuardrailType.AWS_BEDROCK.value,
    }

    with patch("app.utils.guardrails.OpenAIModerationChain") as moderation_chain:
        guardrail = initialize_guardrail(
            configuration,
            guardrail_type=GuardrailType.OPENAI_MODERATION.value,
        )

    moderation_chain.assert_called_once_with()
    assert guardrail is moderation_chain.return_value


def test_missing_guardrail_type_raises_value_error():
    with pytest.raises(ValueError, match="guardrail_type is missing"):
        initialize_guardrail(OPENAI_MODERATION_CONFIGURATION)


def test_invalid_guardrail_type_parameter_raises_value_error():
    with pytest.raises(ValueError, match="invalid guardrail_type 'unknown_provider'"):
        initialize_guardrail(
            OPENAI_MODERATION_CONFIGURATION,
            guardrail_type="unknown_provider",
        )


def test_invalid_guardrail_type_configuration_key_raises_value_error():
    configuration = {
        **OPENAI_MODERATION_CONFIGURATION,
        "guardrail_type": "unknown_provider",
    }

    with pytest.raises(ValueError, match="invalid guardrail_type 'unknown_provider'"):
        initialize_guardrail(configuration)
