import logging
import os
import time
from concurrent import futures
from logging.handlers import TimedRotatingFileHandler

import grpc
from google.protobuf import json_format
from langchain_openai import OpenAIEmbeddings
from langchain_text_splitters import CharacterTextSplitter

import embedding_pb2
import embedding_pb2_grpc
from derived_text_splitter import DerivedTextSplitter

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
handler = TimedRotatingFileHandler(
    "./logs/embedding-module.log", when="D", interval=1, backupCount=10
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
DEFAULT_OPENAI_EMBEDDING_MODEL = "text-embedding-3-small"


class EmbeddingServicer(embedding_pb2_grpc.EmbeddingServicer):
    def GetMessages(self, request, context):
        start = time.time()

        chunk = request.chunk
        chunk_type = chunk.type
        chunk_jsonConfig = json_format.MessageToDict(chunk.jsonConfig)
        api_key = request.api_key
        os.environ["OPENAI_API_KEY"] = api_key
        text = request.text
        text_splitted = []
        chunks = []

        if chunk_type == 1:
            chunk_size = (
                int(chunk_jsonConfig["size"])
                if "size" in chunk_jsonConfig
                else DEFAULT_CHUNK_SIZE
            )
            chunk_overlap = (
                int(chunk_jsonConfig["overlap"])
                if "overlap" in chunk_jsonConfig
                else DEFAULT_CHUNK_OVERLAP
            )
            text_splitter = DerivedTextSplitter(
                chunk_size=chunk_size,
                chunk_overlap=chunk_overlap,
            )
            text_splitted = text_splitter.split_text(text)

        elif chunk_type == 2:
            chunk_size = (
                int(chunk_jsonConfig["size"])
                if "size" in chunk_jsonConfig
                else DEFAULT_CHUNK_SIZE
            )
            chunk_overlap = (
                int(chunk_jsonConfig["overlap"])
                if "overlap" in chunk_jsonConfig
                else DEFAULT_CHUNK_OVERLAP
            )
            chunk_separator = (
                chunk_jsonConfig["separator"]
                if "separator" in chunk_jsonConfig
                else DEFAULT_SEPARATOR
            )
            chunk_model_name = (
                chunk_jsonConfig["model_name"]
                if "model_name" in chunk_jsonConfig
                else DEFAULT_MODEL_NAME
            )
            chunk_encoding = (
                chunk_jsonConfig["encoding"]
                if "encoding" in chunk_jsonConfig
                else DEFAULT_ENCODING_NAME
            )
            chunk_is_separator_regex = (
                chunk_jsonConfig["is_separator_regex"]
                if "is_separator_regex" in chunk_jsonConfig
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

        elif chunk_type == 3:
            chunk_size = (
                int(chunk_jsonConfig["size"])
                if "size" in chunk_jsonConfig
                else DEFAULT_CHUNK_SIZE
            )
            chunk_overlap = (
                int(chunk_jsonConfig["overlap"])
                if "overlap" in chunk_jsonConfig
                else DEFAULT_CHUNK_OVERLAP
            )
            chunk_separator = (
                chunk_jsonConfig["separator"]
                if "separator" in chunk_jsonConfig
                else DEFAULT_SEPARATOR
            )
            chunk_is_separator_regex = (
                chunk_jsonConfig["is_separator_regex"]
                if "is_separator_regex" in chunk_jsonConfig
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

        total_chunks = len(text_splitted)

        embeddings = OpenAIEmbeddings(model=DEFAULT_OPENAI_EMBEDDING_MODEL)

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
    embedding_pb2_grpc.add_EmbeddingServicer_to_server(EmbeddingServicer(), server)
    server.add_insecure_port("[::]:5000")
    server.start()
    server.wait_for_termination()


if __name__ == "__main__":
    serve()
