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

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
handler = TimedRotatingFileHandler(
    "/var/log/openk9/embedding-module.log", when="D", interval=1, backupCount=10
)
handler.setFormatter(formatter)
logger.addHandler(handler)

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


def save_google_application_credentials(credentials, credentials_file_path='./'):
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
        credential_file = f'{credentials_file_path}application_default_credentials.json'

        with open(credential_file, "w", encoding="utf-8") as outfile:
            outfile.write(json_credentials)

        os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credential_file
    except json.JSONDecodeError as e:
        raise ValueError("Invalid JSON structure in credentials.") from e
    except OSError as e:
        raise OSError("Failed to write credentials file.") from e


def initialize_embedding_model(configuration):
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
            embeddings = OpenAIEmbeddings(model=model)
        case ModelType.OLLAMA.value:
            embeddings = OllamaEmbeddings(
                model=model, base_url=api_url
            )
        case ModelType.IBM_WATSONX.value:
            os.environ["WATSONX_APIKEY"] = api_key
            watsonx_project_id = configuration["watsonx_project_id"]
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
            configuration = {
                "type": "chat_vertex_ai_model_garden",
                "model": "text-embedding-005",
                "chat_vertex_ai_model_garden": {
                    "credentials": {
                        "account": "",
                        "client_id": "client_id",
                        "client_secret": "client_secret",
                        "quota_project_id": "project_id",
                        "refresh_token": "",
                        "type": "authorized_user",
                        "universe_domain": "googleapis.com",
                    },
                    "endpoint_id": "us-central1-aiplatform.googleapis.com",
                    "location": "us-central1",
                },
            }

            chat_vertex_ai_model_garden = configuration["chat_vertex_ai_model_garden"]
            google_credentials = chat_vertex_ai_model_garden["credentials"]
            save_google_application_credentials(google_credentials)
            project_id = google_credentials["quota_project_id"]
            model = configuration["model"]

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
        api_key = request.api_key
        os.environ["OPENAI_API_KEY"] = api_key
        text = clean_text(request.text)
        text_splitted = []
        chunks = []

        embeddings = OpenAIEmbeddings(model=DEFAULT_MODEL)

        if chunk_type == 1:
            chunk_size = (
                int(chunk_json_config["size"])
                if "size" in chunk_json_config
                else DEFAULT_CHUNK_SIZE
            )
            chunk_overlap = (
                int(chunk_json_config["overlap"])
                if "overlap" in chunk_json_config
                else DEFAULT_CHUNK_OVERLAP
            )
            text_splitter = DerivedTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
            )
            text_splitted = text_splitter.split_text(text)

        elif chunk_type == 2:
            chunk_size = (
                int(chunk_json_config["size"])
                if "size" in chunk_json_config
                else DEFAULT_CHUNK_SIZE
            )
            chunk_overlap = (
                int(chunk_json_config["overlap"])
                if "overlap" in chunk_json_config
                else DEFAULT_CHUNK_OVERLAP
            )
            chunk_separator = (
                chunk_json_config["separator"]
                if "separator" in chunk_json_config
                else DEFAULT_SEPARATOR
            )
            chunk_model_name = (
                chunk_json_config["model_name"]
                if "model_name" in chunk_json_config
                else DEFAULT_MODEL_NAME
            )
            chunk_encoding = (
                chunk_json_config["encoding"]
                if "encoding" in chunk_json_config
                else DEFAULT_ENCODING_NAME
            )
            chunk_is_separator_regex = (
                chunk_json_config["is_separator_regex"]
                if "is_separator_regex" in chunk_json_config
                else DEFAULT_IS_SEPARATOR_REGEX
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
            chunk_size = (
                int(chunk_json_config["size"])
                if "size" in chunk_json_config
                else DEFAULT_CHUNK_SIZE
            )
            chunk_overlap = (
                int(chunk_json_config["overlap"])
                if "overlap" in chunk_json_config
                else DEFAULT_CHUNK_OVERLAP
            )
            chunk_separator = (
                chunk_json_config["separator"]
                if "separator" in chunk_json_config
                else DEFAULT_SEPARATOR
            )
            chunk_is_separator_regex = (
                chunk_json_config["is_separator_regex"]
                if "is_separator_regex" in chunk_json_config
                else DEFAULT_IS_SEPARATOR_REGEX
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

        logger.info("request: %s", request)
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
