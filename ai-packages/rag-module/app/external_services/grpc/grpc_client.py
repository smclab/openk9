import grpc
from google.protobuf import json_format
from google.protobuf.json_format import ParseDict

from app.external_services.grpc.searcher import searcher_pb2, searcher_pb2_grpc
from app.external_services.grpc.searcher.searcher_pb2 import SearchTokenRequest, Value
from app.external_services.grpc.tenant_manager import (
    tenant_manager_pb2,
    tenant_manager_pb2_grpc,
)


def query_parser(
    search_query,
    range_values,
    after_key,
    suggest_keyword,
    suggestion_category_id,
    virtual_host,
    jwt,
    extra,
    sort,
    sort_after_key,
    language,
    grpc_host,
):
    """Get opensearch query and index from grpc."""

    search_query_to_proto_list = []
    for query in search_query:
        search_query_to_proto = ParseDict(query, SearchTokenRequest())
        search_query_to_proto_list.append(search_query_to_proto)

    for option in extra:
        extra[option] = ParseDict({"value": extra[option]}, Value())

    with grpc.insecure_channel(grpc_host) as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.QueryParser(
            searcher_pb2.QueryParserRequest(
                searchQuery=search_query_to_proto_list,
                range=range_values,
                afterKey=after_key,
                suggestKeyword=suggest_keyword,
                suggestionCategoryId=suggestion_category_id,
                virtualHost=virtual_host,
                jwt=jwt,
                extra=extra,
                sort=sort,
                sortAfterKey=sort_after_key,
                language=language,
            )
        )

    return response


def get_rag_configuration(grpc_host, virtual_host, rag_type):
    """Get rag configuration from grpc."""
    with grpc.insecure_channel(grpc_host) as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.GetRAGConfigurations(
            searcher_pb2.GetRAGConfigurationsRequest(
                virtualHost=virtual_host, ragType=rag_type
            )
        )

    rag_tool_description = response.ragToolDescription
    chunk_window = response.chunkWindow
    reformulate = response.reformulate
    json_config = json_format.MessageToDict(response.jsonConfig)
    rerank = json_config.get("rerank")
    metadata = json_config.get("metadata")

    configuration = {
        "rag_tool_description": rag_tool_description,
        "chunk_window": chunk_window,
        "reformulate": reformulate,
        "rerank": rerank,
        "metadata": metadata,
    }

    return configuration


def get_llm_configuration(grpc_host, virtual_host):
    """Get llm configuration from grpc."""
    with grpc.insecure_channel(grpc_host) as channel:
        stub = searcher_pb2_grpc.SearcherStub(channel)
        response = stub.GetLLMConfigurations(
            searcher_pb2.GetLLMConfigurationsRequest(
                virtualHost=virtual_host,
            )
        )

    api_url = response.apiUrl
    api_key = response.apiKey
    retrieve_type = response.retrieveType
    context_window = response.contextWindow
    retrieve_citations = response.retrieveCitations
    model_type = response.providerModel.provider
    model = response.providerModel.model
    json_config = json_format.MessageToDict(response.jsonConfig)
    rerank = json_config.get("rerank")
    watsonx_project_id = json_config.get("watsonx_project_id")
    chat_vertex_ai_credentials = json_config.get("credentials")
    chat_vertex_ai_model_garden = json_config.get("chat_vertex_ai_model_garden")

    configuration = {
        "api_url": api_url,
        "api_key": api_key,
        "model_type": model_type,
        "model": model,
        "context_window": context_window,
        "retrieve_citations": retrieve_citations,
        "rerank": rerank,
        "retrieve_type": retrieve_type,
        "watsonx_project_id": watsonx_project_id,
        "chat_vertex_ai_credentials": chat_vertex_ai_credentials,
        "chat_vertex_ai_model_garden": chat_vertex_ai_model_garden,
    }

    return configuration


def get_tenant_manager_configuration(grpc_host, virtual_host):
    """Get tenant configuration from grpc."""
    with grpc.insecure_channel(grpc_host) as channel:
        stub = tenant_manager_pb2_grpc.TenantManagerStub(channel)
        response = stub.FindTenant(
            tenant_manager_pb2.TenantRequest(
                virtualHost=virtual_host,
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
