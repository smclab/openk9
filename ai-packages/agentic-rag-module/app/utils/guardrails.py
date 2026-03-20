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
from enum import Enum

from langchain_aws import ChatBedrockConverse
from langchain_classic.chains import OpenAIModerationChain
from langchain_google_community.model_armor import ModelArmorSanitizePromptRunnable

from app.utils.llm import save_google_application_credentials

DEFAULT_MODEL_TYPE = "aws_bedrock"
DEFAULT_MODEL = "qwen.qwen3-32b-v1:0"


class GuardrailType(Enum):
    AWS_BEDROCK = "aws_bedrock"
    GOOGLE_MODEL_ARMOR = "google_model_armor"
    OPENAI_MODERATION = "openai_moderation"


def initialize_guardrail(configuration):
    """
    Initialize and return a guardrail mechanism based on the specified model type
    and configuration settings.

    This function sets up content moderation and safety guardrails for language model
    interactions. It supports various guardrail providers including AWS Bedrock,
    Google Model Armor, and OpenAI Moderation.

    Parameters:
    ----------
    configuration : dict
        A dictionary containing configuration settings required for guardrail initialization.
        Expected keys vary based on the guardrail type:

        Common keys:
            - "model_type": str
                The type of guardrail to instantiate. Should match one of the values defined
                in the GuardrailType enumeration (e.g., 'AWS_BEDROCK', 'GOOGLE_MODEL_ARMOR',
                'OPENAI_MODERATION').
            - "api_key": str
                API key for authentication (required for AWS_BEDROCK and OPENAI_MODERATION).
            - "region": str
                Region for the guardrail service (required for AWS_BEDROCK and GOOGLE_MODEL_ARMOR).

        For AWS_BEDROCK:
            - "aws_bedrock": dict
                AWS Bedrock configuration containing:
                    - "guardrail_identifier": str
                        Identifier for the guardrail to use.
                    - "guardrail_version": str
                        Version of the guardrail to use.
            - "model": str
                Name of the model to use with the guardrail.

        For GOOGLE_MODEL_ARMOR:
            - "google_model_armor": dict
                Google Model Armor credentials containing:
                    - "quota_project_id": str
                        Project ID for quota management.
            - "template_id": str
                Identifier for the Model Armor template to use.

        For OPENAI_MODERATION:
            - No additional configuration required beyond api_key.

    Returns:
    -------
    guardrail : object
        An instance of a guardrail mechanism corresponding to the specified type:
            - For AWS_BEDROCK: Returns a ChatBedrockConverse instance with guardrail config
            - For GOOGLE_MODEL_ARMOR: Returns a ModelArmorSanitizePromptRunnable instance
            - For OPENAI_MODERATION: Returns an OpenAIModerationChain instance
            - Default: Returns an OpenAIModerationChain instance

        The returned object can be used to apply content moderation and safety checks
        to language model inputs and outputs.

    Examples:
    --------
    >>> # Initialize AWS Bedrock guardrail
    >>> config = {
            "model_type": "aws_bedrock",
            "api_key": "your-aws-api-key",
            "region": "us-east-1",
            "model": "anthropic.claude-v2",
            "aws_bedrock": {
                "guardrail_identifier": "my-guardrail",
                "guardrail_version": "1"
            }
        }
    >>> guardrail = initialize_guardrail(config)

    >>> # Initialize Google Model Armor
    >>> config = {
            "model_type": "google_model_armor",
            "region": "region",
            "project_id": "project_id",
            "template_id": "template_id",
            "google_credentials": {
                "account": "",
                "client_id": "client_id",
                "client_secret": "client_secret,
                "refresh_token": "refresh_token",
                "type": "type",
                "universe_domain": "universe_domain",
            },
        }
    >>> guardrail = initialize_guardrail(config)

    >>> # Initialize OpenAI Moderation
    >>> config = {
            "model_type": "openai_moderation",
            "api_key": "your-openai-api-key"
        }
    >>> guardrail = initialize_guardrail(config)

    Notes:
    -----
    - For AWS_BEDROCK, the API key is set in the AWS_BEARER_TOKEN_BEDROCK environment variable.
    - For GOOGLE_MODEL_ARMOR, credentials are saved using save_google_application_credentials().
    - For OPENAI_MODERATION, the API key is set in the OPENAI_API_KEY environment variable.
    - If no matching model_type is provided, the function defaults to OPENAI_MODERATION.
    """

    model_type = (
        configuration.get("model_type")
        if configuration.get("model_type")
        else DEFAULT_MODEL_TYPE
    )
    model = configuration.get("model") if configuration.get("model") else DEFAULT_MODEL
    api_key = configuration.get("api_key")
    region = configuration.get("region")

    match model_type:
        case GuardrailType.AWS_BEDROCK.value:
            os.environ["AWS_BEARER_TOKEN_BEDROCK"] = api_key
            aws_bedrock = configuration.get("aws_bedrock")
            guardrail_identifier = aws_bedrock.get("guardrail_identifier")
            guardrail_version = aws_bedrock.get("guardrail_version")

            guardrail = ChatBedrockConverse(
                model=model,
                region_name=region,
                guardrail_config={
                    "guardrailIdentifier": guardrail_identifier,
                    "guardrailVersion": guardrail_version,
                },
            )
        case GuardrailType.GOOGLE_MODEL_ARMOR.value:
            google_model_armor_credentials = configuration.get("google_credentials")
            save_google_application_credentials(google_model_armor_credentials)
            project_id = configuration.get("project_id")
            template_id = configuration.get("template_id")

            guardrail = ModelArmorSanitizePromptRunnable(
                project=project_id,
                location=region,
                template_id=template_id,
                fail_open=False,
            )
        case GuardrailType.OPENAI_MODERATION.value:
            os.environ["OPENAI_API_KEY"] = api_key

            guardrail = OpenAIModerationChain()
        case _:
            os.environ["OPENAI_API_KEY"] = api_key

            guardrail = OpenAIModerationChain()

    return guardrail
