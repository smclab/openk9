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

"""
Build the PydanticAI model of a tenant from its LLM configuration.

It takes the same ``llm_configuration`` that agentic-rag-module hands to
``initialize_language_model`` and returns a ``pydantic_ai.models.Model``. The
SDK of each provider is imported only when that provider is built.

The temperature is not a property of the model in PydanticAI: it travels in
the model settings of an agent or of a run, see :func:`build_model_settings`.
"""

from enum import Enum
from typing import TYPE_CHECKING, Optional

from app.utils.llm import save_google_application_credentials
from app.utils.logger import get_logger

if TYPE_CHECKING:
    from pydantic_ai.models import Model
    from pydantic_ai.settings import ModelSettings

logger = get_logger(__name__)

DEFAULT_MODEL_TYPE = "openai"
DEFAULT_MODEL = "gpt-4o-mini"


class ModelType(Enum):
    OPENAI = "openai"
    OLLAMA = "ollama"
    HUGGING_FACE_CUSTOM = "hugging-face-custom"
    IBM_WATSONX = "watsonx"
    CHAT_VERTEX_AI = "chat_vertex_ai"
    CHAT_VERTEX_AI_MODEL_GARDEN = "chat_vertex_ai_model_garden"
    AWS_BEDROCK = "aws_bedrock"


#: Providers without a PydanticAI model: they get one of their own in a later
#: issue. Until then they are refused rather than sent to OpenAI by default.
CUSTOM_MODEL_TYPES = {
    ModelType.OLLAMA.value,
    ModelType.HUGGING_FACE_CUSTOM.value,
    ModelType.IBM_WATSONX.value,
}


def build_model_settings(temperature: Optional[float] = None) -> Optional["ModelSettings"]:
    """
    Return the model settings for ``temperature``.

    :param temperature: The sampling temperature; ``None`` keeps the default of
        the provider, ``0`` makes the utility steps deterministic.
    :type temperature: float or None

    :return: The settings to pass to an agent, or ``None`` with no temperature.
    :rtype: ModelSettings or None
    """
    if temperature is None:
        return None
    from pydantic_ai.settings import ModelSettings

    return ModelSettings(temperature=temperature)


def build_model(configuration: dict) -> "Model":
    """
    Build the PydanticAI model of the tenant LLM configuration.

    :param configuration: The ``llm_configuration`` of the tenant: ``model_type``,
        ``api_url``, ``api_key``, ``model`` and the provider keys
        (``chat_vertex_ai_credentials``, ``chat_vertex_ai_model_garden``,
        ``aws_bedrock``).
    :type configuration: dict

    :return: The model of the provider. An unknown ``model_type`` falls back to
        OpenAI, as in agentic-rag-module.
    :rtype: pydantic_ai.models.Model

    :raises ValueError: For a provider whose model is not available yet.
    :raises ImportError: When the SDK of the provider is not installed.
    """
    model_type = configuration.get("model_type") or DEFAULT_MODEL_TYPE
    api_url = configuration.get("api_url")
    api_key = configuration.get("api_key")
    model = configuration.get("model") or DEFAULT_MODEL

    logger.debug(f"build_model model_type={model_type} model={model}")

    if model_type in CUSTOM_MODEL_TYPES:
        raise ValueError(f"LLM provider not supported yet: {model_type}")

    match model_type:
        case ModelType.OPENAI.value:
            from pydantic_ai.models.openai import OpenAIChatModel
            from pydantic_ai.providers.openai import OpenAIProvider

            # An empty api_url is the standard OpenAI endpoint: None selects it,
            # while an empty base_url would fail on every request.
            provider = OpenAIProvider(base_url=api_url or None, api_key=api_key)
            return OpenAIChatModel(model, provider=provider)

        case ModelType.CHAT_VERTEX_AI.value:
            from pydantic_ai.models.google import GoogleModel
            from pydantic_ai.providers.google_cloud import GoogleCloudProvider

            # The provider reads the credentials saved here through the
            # Application Default Credentials.
            google_credentials = configuration["chat_vertex_ai_credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]
            provider = GoogleCloudProvider(project=project_id)
            return GoogleModel(model, provider=provider)

        case ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value:
            # Model Garden serves an OpenAI-compatible endpoint behind an OAuth
            # token of the service account.
            from google.auth import default
            from google.auth.transport.requests import Request
            from pydantic_ai.models.openai import OpenAIChatModel
            from pydantic_ai.providers.openai import OpenAIProvider

            model_garden = configuration["chat_vertex_ai_model_garden"]
            google_credentials = model_garden["credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]
            endpoint_id = model_garden["endpoint_id"]
            location = model_garden["location"]

            credentials, _ = default()
            credentials.refresh(Request())
            base_url = (
                f"https://{endpoint_id}/v1/projects/{project_id}"
                f"/locations/{location}/endpoints/openapi"
            )
            provider = OpenAIProvider(base_url=base_url, api_key=credentials.token)
            return OpenAIChatModel(model, provider=provider)

        case ModelType.AWS_BEDROCK.value:
            from pydantic_ai.models.bedrock import BedrockConverseModel
            from pydantic_ai.providers.bedrock import BedrockProvider

            # The api_key is a Bedrock API key, sent as a bearer token.
            provider = BedrockProvider(
                region_name=configuration["aws_bedrock"]["region_name"],
                api_key=api_key,
            )
            return BedrockConverseModel(model, provider=provider)

        case _:
            from pydantic_ai.models.openai import OpenAIChatModel
            from pydantic_ai.providers.openai import OpenAIProvider

            return OpenAIChatModel(model, provider=OpenAIProvider(api_key=api_key))
