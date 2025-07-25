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

import json
import logging
import os
import time
from concurrent import futures
from enum import Enum
from logging.handlers import TimedRotatingFileHandler

import embedding_pb2
import embedding_pb2_grpc
import grpc
from derived_text_splitter import DerivedTextSplitter
from dotenv import load_dotenv
from google.protobuf import json_format
from grpc_health.v1 import health_pb2, health_pb2_grpc
from grpc_health.v1.health import HealthServicer
from grpc_reflection.v1alpha import reflection
from ibm_watsonx_ai.metanames import EmbedTextParamsMetaNames
from langchain_experimental.text_splitter import SemanticChunker
from langchain_google_vertexai import VertexAIEmbeddings
from langchain_ibm import WatsonxEmbeddings
from langchain_ollama import OllamaEmbeddings
from langchain_openai import OpenAIEmbeddings
from langchain_text_splitters import CharacterTextSplitter
from text_cleaner import clean_text

load_dotenv()

LOGGING_LEVEL = os.getenv("LOGGING_LEVEL", "INFO")

logger = logging.getLogger(__name__)
logger.setLevel(LOGGING_LEVEL)
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
file_handler = TimedRotatingFileHandler(
    "/var/log/openk9/embedding-module.log", when="D", interval=1, backupCount=10
)
file_handler.setFormatter(formatter)
console_handler = logging.StreamHandler()
console_handler.setFormatter(formatter)
logger.addHandler(file_handler)
logger.addHandler(console_handler)

# default text splitters parameters
DEFAULT_CHUNK_SIZE = 100
DEFAULT_CHUNK_OVERLAP = 10
DEFAULT_SEPARATOR = "\n\n"
DEFAULT_MODEL_NAME = "gpt2"
DEFAULT_ENCODING_NAME = None
DEFAULT_IS_SEPARATOR_REGEX = False

# default text embedding parameters
DEFAULT_MODEL_TYPE = "openai"
DEFAULT_MODEL = "text-embedding-3-small"


class ModelType(Enum):
    OPENAI = "openai"
    OLLAMA = "ollama"
    IBM_WATSONX = "watsonx"
    CHAT_VERTEX_AI = "chat_vertex_ai"


def save_google_application_credentials(credentials, credentials_file_path="./"):
    """
    Save Google Application credentials to a JSON file and configure environment variables.

    Serializes credentials to a JSON file and sets the GOOGLE_APPLICATION_CREDENTIALS environment
    variable to enable automatic credential discovery by Google Cloud client libraries.

    .. note::
        The environment variable modification only affects the current process and child processes.

    :param dict credentials: Dictionary containing Google Application credentials data.
        Expected to contain service account or user credential fields.
        Must be JSON-serializable (typically contains key/values with primitive types).
    :param str credentials_file_path: Path to directory for credentials file (default: current directory)

    :raises json.JSONDecodeError: If credentials contain non-serializable data types.
    :raises OSError: If file writing operations fail (e.g., permission issues).

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
        save_google_cloud_credentials(credentials, "path/to/credentials/")
    """
    try:
        json_credentials = json.dumps(credentials, indent=2, sort_keys=True)
        credential_file = f"{credentials_file_path}application_default_credentials.json"

        with open(credential_file, "w", encoding="utf-8") as outfile:
            outfile.write(json_credentials)

        os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credential_file
    except json.JSONDecodeError as e:
        raise ValueError("Invalid JSON structure in credentials.") from e
    except OSError as e:
        raise OSError("Failed to write credentials file.") from e


def initialize_embedding_model(configuration):
    """
    Initialize and return an embedding model based on the specified model type
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
                in the ModelType enumeration (e.g., 'OPENAI', 'OLLAMA', 'IBM_WATSONX',
                'CHAT_VERTEX_AI').
            - "model": str
                Name of the model to use; defaults to DEFAULT_MODEL if not provided.
            - "watsonx_project_id": str
                Project ID for IBM WatsonX (required if using WatsonX).
            - "chat_vertex_ai_credentials": dict
                Credentials for Google Vertex AI (required if using Vertexai).

    Returns:
    -------
    embeddings : object
        An instance of an embedding model corresponding to the specified model type.

    """
    api_key = configuration.get("api_key")
    api_url = configuration.get("api_url")
    model_type = configuration.get("model_type", DEFAULT_MODEL_TYPE)
    model = configuration.get("model", DEFAULT_MODEL)

    match model_type:
        case ModelType.OPENAI.value:
            os.environ["OPENAI_API_KEY"] = api_key
            embeddings = OpenAIEmbeddings(model=model)
        case ModelType.OLLAMA.value:
            embeddings = OllamaEmbeddings(model=model, base_url=api_url)
        case ModelType.IBM_WATSONX.value:
            os.environ["WATSONX_APIKEY"] = api_key
            watsonx_project_id = configuration.get("watsonx_project_id")
            embed_params = {
                EmbedTextParamsMetaNames.TRUNCATE_INPUT_TOKENS: 3,
                EmbedTextParamsMetaNames.RETURN_OPTIONS: {"input_text": True},
            }
            embeddings = WatsonxEmbeddings(
                model_id=model,
                url=api_url,
                project_id=watsonx_project_id,
                params=embed_params,
            )
        case ModelType.CHAT_VERTEX_AI.value:
            chat_vertex_ai_model_garden = configuration.get(
                "chat_vertex_ai_model_garden"
            )
            google_credentials = chat_vertex_ai_model_garden.get("credentials")
            save_google_application_credentials(google_credentials)
            project_id = google_credentials.get("quota_project_id")
            model = configuration.get("model")

            embeddings = VertexAIEmbeddings(model_name=model, project=project_id)
        case _:
            embeddings = OpenAIEmbeddings(model=model)

    return embeddings


class EmbeddingServicer(embedding_pb2_grpc.EmbeddingServicer):
    def GetMessages(self, request, context):
        start = time.time()

        chunk = request.chunk
        chunk_type = chunk.type
        chunk_json_config = json_format.MessageToDict(chunk.jsonConfig)
        embedding_model = request.embeddingModel
        model_type = embedding_model.providerModel.provider
        model = embedding_model.providerModel.model
        api_key = embedding_model.apiKey
        embedding_model_json_config = json_format.MessageToDict(
            embedding_model.jsonConfig
        )

        configuration = {
            "api_key": api_key,
            "api_url": embedding_model_json_config.get("api_url"),
            "model_type": model_type,
            "model": model,
            "watsonx_project_id": embedding_model_json_config.get("watsonx_project_id"),
            "chat_vertex_ai_model_garden": embedding_model_json_config.get(
                "chat_vertex_ai_model_garden"
            ),
        }
        embeddings = initialize_embedding_model(configuration)

        text = clean_text(request.text)
        text_splitted = []
        chunks = []

        info = {
            "text": text,
            "chunk_type": chunk_type,
            "provider": model_type,
            "model": model,
            "chunk_config": chunk_json_config,
        }

        logger.info(info)

        if chunk_type == 1:
            chunk_size = int(chunk_json_config.get("size", DEFAULT_CHUNK_SIZE))
            chunk_overlap = int(chunk_json_config.get("overlap", DEFAULT_CHUNK_OVERLAP))

            text_splitter = DerivedTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
            )
            text_splitted = text_splitter.split_text(text)

        elif chunk_type == 2:
            chunk_size = int(chunk_json_config.get("size", DEFAULT_CHUNK_SIZE))
            chunk_overlap = int(chunk_json_config.get("overlap", DEFAULT_CHUNK_OVERLAP))
            chunk_separator = chunk_json_config.get("separator", DEFAULT_SEPARATOR)
            chunk_model_name = chunk_json_config.get("model_name", DEFAULT_MODEL_NAME)
            chunk_encoding = chunk_json_config.get("encoding", DEFAULT_ENCODING_NAME)
            chunk_is_separator_regex = chunk_json_config.get(
                "is_separator_regex", DEFAULT_IS_SEPARATOR_REGEX
            )
            text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
                separator=chunk_separator,
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
                model_name=chunk_model_name,
                encoding_name=chunk_encoding,
                is_separator_regex=chunk_is_separator_regex,
            )
            text_splitted = text_splitter.split_text(text)

        elif chunk_type == 3 or chunk_type == 0:
            chunk_size = int(chunk_json_config.get("size", DEFAULT_CHUNK_SIZE))
            chunk_overlap = int(chunk_json_config.get("overlap", DEFAULT_CHUNK_OVERLAP))
            chunk_separator = chunk_json_config.get("separator", DEFAULT_SEPARATOR)
            chunk_is_separator_regex = chunk_json_config.get(
                "is_separator_regex", DEFAULT_IS_SEPARATOR_REGEX
            )
            text_splitter = CharacterTextSplitter(
                separator=chunk_separator,
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
                length_function=len,
                is_separator_regex=chunk_is_separator_regex,
            )
            text_splitted = text_splitter.split_text(text)

        elif chunk_type == 4:
            text_splitter = SemanticChunker(embeddings)
            text_splitted = text_splitter.split_text(text)

        total_chunks = len(text_splitted)

        for index, chunk_text in enumerate(text_splitted, start=1):
            chunk = {
                "number": index,
                "total": total_chunks,
                "text": chunk_text,
                "vectors": embeddings.embed_query(chunk_text),
            }
            chunks.append(chunk)

        end = time.time()

        logger.info(
            "text splitted in %s chunks in %s seconds",
            total_chunks,
            round(end - start, 2),
        )

        return embedding_pb2.EmbeddingResponse(chunks=chunks)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    # Add Health Service
    health_servicer = HealthServicer()
    health_pb2_grpc.add_HealthServicer_to_server(health_servicer, server)

    # Register Embedding Service
    embedding_pb2_grpc.add_EmbeddingServicer_to_server(EmbeddingServicer(), server)

    # Enable reflection
    service_names = (
        embedding_pb2.DESCRIPTOR.services_by_name["Embedding"].full_name,
        health_pb2.DESCRIPTOR.services_by_name["Health"].full_name,
        reflection.SERVICE_NAME,
    )
    reflection.enable_server_reflection(service_names, server)

    # Start the server
    server.add_insecure_port("[::]:5000")
    server.start()
    logger.info("Server started")

    # Update health status to SERVING once the server is ready
    health_servicer.set("", health_pb2.HealthCheckResponse.SERVING)
    logger.info("Health status set to SERVING")

    server.wait_for_termination()


if __name__ == "__main__":
    serve()
