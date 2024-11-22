import grpc
from google.protobuf.struct_pb2 import Struct

import embedding_pb2
import embedding_pb2_grpc


def run(chunk, api_key, text):
    with grpc.insecure_channel("localhost:5000") as channel:
        stub = embedding_pb2_grpc.EmbeddingStub(channel)
        response = stub.GetMessages(
            embedding_pb2.EmbeddingRequest(chunk=chunk, api_key=api_key, text=text)
        )
    print(f"Chunks: {response.chunks}")

