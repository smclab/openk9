import grpc

from .datasource import datasource_pb2, datasource_pb2_grpc
from .searcher import searcher_pb2, searcher_pb2_grpc

GRPC_HOST = "159.122.129.226:30370"


def query_parser(
    searchQuery,
    range,
    afterKey,
    suggestKeyword,
    suggestionCategoryId,
    virtualHost,
    jwt,
    extra,
    sort,
    sortAfterKey,
    language,
    vectorIndices,
):

    with grpc.insecure_channel(GRPC_HOST) as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.QueryParser(
            searcher_pb2.QueryParserRequest(
                searchQuery=searchQuery,
                range=range,
                afterKey=afterKey,
                suggestKeyword=suggestKeyword,
                suggestionCategoryId=suggestionCategoryId,
                virtualHost=virtualHost,
                jwt=jwt,
                extra=extra,
                sort=sort,
                sortAfterKey=sortAfterKey,
                language=language,
                vectorIndices=vectorIndices,
            )
        )

    return response


def get_llm_configuration(virtualHost):

    with grpc.insecure_channel(GRPC_HOST) as channel:
        stub = datasource_pb2_grpc.DatasourceStub(channel)
        response = stub.GetLLMConfigurations(
            datasource_pb2.GetLLMConfigurationsRequest(
                schemaName=virtualHost,
            )
        )

    api_url = response.apiUrl
    api_key = response.apiKey
    json_config = response.jsonConfig

    configuration = {"api_url": api_url, "api_key": api_key, "json_config": json_config}

    return configuration
