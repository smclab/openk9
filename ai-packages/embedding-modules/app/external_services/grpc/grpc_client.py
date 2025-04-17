import grpc
import searcher_pb2
import searcher_pb2_grpc


def get_embedding_model_configuration(grpc_host, virtual_host):
    """Get embedding model configuration from grpc."""
    with grpc.insecure_channel(grpc_host) as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.GetEmbeddingModelConfigurations(
            searcher_pb2.GetEmbeddingModelConfigurationsRequest(
                virtualHost=virtual_host
            )
        )

    return response
