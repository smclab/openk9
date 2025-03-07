import logging
import os
import time
from concurrent import futures
from logging import StreamHandler
from logging.handlers import TimedRotatingFileHandler

import embedding_pb2
import embedding_pb2_grpc
import grpc
from derived_text_splitter import DerivedTextSplitter
from google.protobuf import json_format
from grpc_health.v1.health import HealthServicer
from grpc_health.v1 import health_pb2_grpc, health_pb2
from grpc_reflection.v1alpha import reflection
from langchain_experimental.text_splitter import SemanticChunker
from langchain_huggingface.embeddings import HuggingFaceEmbeddings
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
logger.addHandler(StreamHandler())

# default text splitters parameters
DEFAULT_CHUNK_SIZE = 100
DEFAULT_CHUNK_OVERLAP = 10
DEFAULT_SEPARATOR = "\n\n"
DEFAULT_MODEL_NAME = "gpt2"
DEFAULT_ENCODING_NAME = None
DEFAULT_IS_SEPARATOR_REGEX = False

# default text embedding parameters
EMBEDDING_MODEL = os.environ.get("EMBEDDING_MODEL")
if EMBEDDING_MODEL is None:
    EMBEDDING_MODEL = "intfloat/multilingual-e5-large"

embeddings = HuggingFaceEmbeddings(model_name=EMBEDDING_MODEL)

logger.info("Embedding Model Loaded")


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
