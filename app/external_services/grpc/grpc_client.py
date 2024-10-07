import grpc
from google.protobuf import json_format

from .searcher import searcher_pb2, searcher_pb2_grpc
from .tenant_manager import tenant_manager_pb2, tenant_manager_pb2_grpc


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
    json_config = json_format.MessageToDict(response.jsonConfig)
    model_type = json_config["type"]
    model = json_config["model"]
    prompt = json_config["prompt"]
    rephrase_prompt = json_config["rephrase_prompt"]

    configuration = {
        "api_url": api_url,
        "api_key": api_key,
        "model_type": model_type,
        "model": model,
        "prompt": prompt,
        "rephrase_prompt": rephrase_prompt,
    }

    return configuration


def get_tenant_manager_configuration(grpc_host, virtualHost):

    with grpc.insecure_channel(grpc_host) as channel:
        stub = tenant_manager_pb2_grpc.TenantManagerStub(channel)
        response = stub.FindTenant(
            tenant_manager_pb2.TenantRequest(
                virtualHost=virtualHost,
            )
        )

        client_id = response.clientId
        realm_name = response.realmName
        server_url = response.virtualHost

        configuration = {
            "client_id": client_id,
            "realm_name": realm_name,
            "server_url": server_url,
        }

        return configuration
