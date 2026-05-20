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

import json
import os
from enum import Enum

from app.external_services.grpc.grpc_client import (
    get_llm_configuration,
    get_rag_configuration,
)
from app.rag.custom_hugging_face_model import CustomChatHuggingFaceModel
from google.auth import default, transport
from langchain_aws import ChatBedrockConverse
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import PromptTemplate
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_ibm import ChatWatsonx
from langchain_ollama import ChatOllama
from langchain_openai import ChatOpenAI

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


def save_google_application_credentials(credentials):
    """
    Save Google Application credentials to a JSON file and configure environment variables.

    Serializes credentials to a JSON file and sets the GOOGLE_APPLICATION_CREDENTIALS environment
    variable to enable automatic credential discovery by Google Cloud client libraries.

    .. note::
        The environment variable modification only affects the current process and child processes.

    :param dict credentials: Dictionary containing Google Application credentials data.
        Expected to contain service account or user credential fields.
        Must be JSON-serializable (typically contains key/values with primitive types).

    :raises json.JSONEncodeError: If credentials contain non-serializable data types
    :raises OSError: If file writing operations fail (e.g., permission issues)

    Example::

        "credentials": {
            "account": "",
            "client_id": "client_id",
            "client_secret": "client_secret",
            "quota_project_id": "quota_project_id",
            "refresh_token": "refresh_token",
            "type": "type",
            "universe_domain": "universe_domain"
            }
        save_google_cloud_credentials(credentials)
    """
    json_credentials = json.dumps(credentials, indent=2, sort_keys=True)
    credential_file_path = "application_default_credentials.json"

    with open(credential_file_path, "w", encoding="utf-8") as outfile:
        outfile.write(json_credentials)

    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credential_file_path


def get_configurations(
    rag_type,
    grpc_host,
    virtual_host,
):
    rag_configuration = get_rag_configuration(grpc_host, virtual_host, rag_type)
    llm_configuration = get_llm_configuration(grpc_host, virtual_host)

    configurations = {
        "rag_configuration": rag_configuration,
        "llm_configuration": llm_configuration,
    }

    return configurations


def initialize_language_model(configuration):
    """
    Initialize and return a language model based on the specified model type
    and configuration settings.

    Parameters:
    ----------

    configuration : dict
        A dictionary containing configuration settings required for model initialization.
        Expected keys include:
            - "api_url": str
                URL for the API endpoint.
            - "api_key": str
                API key for authentication.
            - "model_type": str
                The type of model to instantiate. Should match one of the values defined
                in the ModelType enumeration (e.g., 'OPENAI', 'OLLAMA', 'HUGGING_FACE_CUSTOM',
                'IBM_WATSONX', 'CHAT_VERTEX_AI').
            - "model": str
                Name of the model to use; defaults to DEFAULT_MODEL if not provided.
            - "prompt_template": str
                The initial prompt to be used with the model.
            - "rephrase_prompt_template": str
                A prompt for rephrasing tasks, if applicable.
            - "context_window": int
                Size of the context window for the model's input.
            - "retrieve_citations": bool
                Flag to enable citation extraction.
            - "rerank": bool
                Flag to enable document reranking.
            - "chunk_window": int
                If 0 disable context window merging, if > 0 and <=2 enable context window merging.
            - "retrieve_type": str
                Specifies the type of retrieval mechanism to be used with the model.
            - "watsonx_project_id": str
                Project ID for IBM WatsonX (required if using IBM_WATSONX).
            - "chat_vertex_ai_credentials": dict
                Credentials for Google Vertex AI (required if using CHAT_VERTEX_AI).
            - "chat_vertex_ai_model_garden": dict
                Configurations for Google Vertex AI Model Garden (required if using CHAT_VERTEX_AI_MODEL_GARDEN).
            - "aws_bedrock": dict
                Configurations for AWS Bedrock (required if using AWS_BEDROCK).

    Returns:
    -------
    llm : object
        An instance of a language model corresponding to the specified model type.
        The returned object can be used to perform various natural language processing tasks.

    """

    model_type = (
        configuration["model_type"]
        if configuration["model_type"]
        else DEFAULT_MODEL_TYPE
    )
    api_url = configuration["api_url"]
    api_key = configuration["api_key"]
    model = configuration["model"] if configuration["model"] else DEFAULT_MODEL
    match model_type:
        case ModelType.OPENAI.value:
            llm = ChatOpenAI(
                model=model,
                openai_api_key=api_key,
                openai_api_base=api_url,
                stream_usage=True,
                max_retries=0,
            )
        case ModelType.OLLAMA.value:
            context_window = configuration["context_window"]
            llm = ChatOllama(model=model, base_url=api_url, num_ctx=context_window)
        case ModelType.HUGGING_FACE_CUSTOM.value:
            llm = CustomChatHuggingFaceModel(base_url=api_url)
        case ModelType.IBM_WATSONX.value:
            watsonx_project_id = configuration["watsonx_project_id"]
            parameters = {
                "decoding_method": "sample",
                "max_new_tokens": 100,
                "min_new_tokens": 1,
                "temperature": 0.5,
                "top_k": 50,
                "top_p": 1,
            }
            llm = ChatWatsonx(
                model_id=model,
                url=api_url,
                apikey=api_key,
                project_id=watsonx_project_id,
                params=parameters,
            )
        case ModelType.CHAT_VERTEX_AI.value:
            google_credentials = configuration["chat_vertex_ai_credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]

            llm = ChatGoogleGenerativeAI(
                model=model,
                backend="vertex",
                project=project_id,
                temperature=0,
                max_tokens=None,
                max_retries=6,
                stop=None,
            )
        case ModelType.CHAT_VERTEX_AI_MODEL_GARDEN.value:
            chat_vertex_ai_model_garden = configuration["chat_vertex_ai_model_garden"]
            google_credentials = chat_vertex_ai_model_garden["credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]
            endpoint_id = chat_vertex_ai_model_garden["endpoint_id"]
            location = chat_vertex_ai_model_garden["location"]

            credentials, _ = default()
            auth_request = transport.requests.Request()
            credentials.refresh(auth_request)

            api_key = credentials.token
            base_url = f"https://{endpoint_id}/v1/projects/{project_id}/locations/{location}/endpoints/openapi"

            llm = ChatOpenAI(model=model, api_key=api_key, base_url=base_url)
        case ModelType.AWS_BEDROCK.value:
            os.environ["AWS_BEARER_TOKEN_BEDROCK"] = api_key
            aws_bedrock = configuration["aws_bedrock"]
            region_name = aws_bedrock["region_name"]

            llm = ChatBedrockConverse(
                model=model,
                region_name=region_name,
            )
        case _:
            llm = ChatOpenAI(model=model, openai_api_key=api_key)

    return llm


def generate_conversation_title(llm, search_text, result_answer):
    """
    Generate a conversation title based on user input and AI response.

    This function creates a title for a conversation by utilizing a language model.
    It takes the user's question and the AI's answer as input, formats them into a
    prompt, and invokes the language model to generate an appropriate title in
    Italian.

    Args:
        llm: An instance of a language model that processes the prompt and generates text.
        search_text (str): The question posed by the user, which serves as the basis for
                        the conversation title.
        result_answer (str): The response provided by the AI, which complements the
                            user's question in the title generation process.

    Returns:
        str: A generated title for the conversation in Italian.

    Example:
        >>> title = generate_conversation_title(llm_instance, "Qual è la tua opinione sul clima?",
                                "Credo che sia un problema serio.")
        >>> print(title)
        "Discussione sul cambiamento climatico"
    """
    title_prompt = PromptTemplate(
        input_variables=["question", "answer"],
        template="""Generate a title for a conversation where the user asks:
                            '{question}' and the AI responds: '{answer}'.""",
    )
    title_chain = title_prompt | llm | StrOutputParser()
    conversation_title = title_chain.invoke(
        {"question": search_text, "answer": result_answer},
    )

    return conversation_title
