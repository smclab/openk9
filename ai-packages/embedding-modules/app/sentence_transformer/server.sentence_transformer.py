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
DEFAULT_EMBEDDING_MODEL = "intfloat/multilingual-e5-large"
DEFAULT_ENCODING_NAME = None
DEFAULT_IS_SEPARATOR_REGEX = False

# default text embedding parameters
EMBEDDING_MODEL = os.environ.get("EMBEDDING_MODEL", DEFAULT_EMBEDDING_MODEL)


class EmbeddingServicer(embedding_pb2_grpc.EmbeddingServicer):
    def __init__(self, embeddings):
        super().__init__()
        self.embeddings = embeddings  # Store embeddings as an instance variable

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
            text_splitter = SemanticChunker(self.embeddings)
            text_splitted = text_splitter.split_text(text)

        total_chunks = len(text_splitted)

        for index, chunk_text in enumerate(text_splitted, start=1):
            chunk = {
                "number": index,
                "total": total_chunks,
                "text": chunk_text,
                "vectors": self.embeddings.embed_query(chunk_text),
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

class HealthCheckServicer(HealthServicer):
    """gRPC health check servicer with embedding model monitoring.

    Attributes:
        embedding_model (HuggingFaceEmbeddings): Embedding model to monitor
    """
    def __init__(self, embedding_model):
        super().__init__()
        self.embedding_model = embedding_model

    def check_embedding_model_health(self):
        try:
            response = self.embedding_model.embed_query("embedding model health check")
            return True if response else False
        except Exception as e:
            logger.error("Embedding model health check failed: %s", e)
            return False

    def Check(self, request, context):
        if self.check_embedding_model_health():
            return health_pb2.HealthCheckResponse(status=health_pb2.HealthCheckResponse.SERVING)
        else:
            return health_pb2.HealthCheckResponse(status=health_pb2.HealthCheckResponse.NOT_SERVING)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))

    embeddings = HuggingFaceEmbeddings(model_name=EMBEDDING_MODEL)

    health_servicer = HealthCheckServicer(embeddings)
    health_pb2_grpc.add_HealthServicer_to_server(health_servicer, server)

    embedding_servicer = EmbeddingServicer(embeddings)
    embedding_pb2_grpc.add_EmbeddingServicer_to_server(embedding_servicer, server)

    service_names = (
        embedding_pb2.DESCRIPTOR.services_by_name["Embedding"].full_name,
        health_pb2.DESCRIPTOR.services_by_name["Health"].full_name,
        reflection.SERVICE_NAME,
    )
    reflection.enable_server_reflection(service_names, server)

    if health_servicer.check_embedding_model_health():
        # Start the server
        server.add_insecure_port("[::]:5000")
        server.start()
        logger.info("Server started")
        health_servicer.set("", health_pb2.HealthCheckResponse.SERVING)
        logger.info("Health status set to SERVING")
        server.wait_for_termination()
    else:
        logger.error("Embedding Model Health Check Failed")
        health_servicer.set("", health_pb2.HealthCheckResponse.NOT_SERVING)
        logger.error("Health status set to NOT_SERVING")


if __name__ == "__main__":
    serve()
