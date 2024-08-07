import grpc

from .searcher import searcher_pb2, searcher_pb2_grpc


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
    grpc_host,
):

    with grpc.insecure_channel(grpc_host) as channel:
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


def get_llm_configuration(grpc_host, virtualHost):

    with grpc.insecure_channel(grpc_host) as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.GetLLMConfigurations(
            searcher_pb2.GetLLMConfigurationsRequest(
                virtualHost=virtualHost,
            )
        )

    api_url = response.apiUrl
    api_key = response.apiKey
    json_config = response.jsonConfig.fields

    configuration = {
        "api_url": api_url,
        "api_key": api_key,
        "json_config": json_config,
    }

    return configuration
