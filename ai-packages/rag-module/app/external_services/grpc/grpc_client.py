#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import grpc
from fastapi import HTTPException, status
from google.protobuf import json_format
from google.protobuf.json_format import ParseDict

from app.external_services.grpc.embedding import embedding_pb2, embedding_pb2_grpc
from app.external_services.grpc.searcher import searcher_pb2, searcher_pb2_grpc
from app.external_services.grpc.searcher.searcher_pb2 import SearchTokenRequest, Value
from app.external_services.grpc.tenant_manager import (
    tenant_manager_pb2,
    tenant_manager_pb2_grpc,
)
from app.utils.chat_history import save_uploaded_documents
from app.utils.logger import logger

UNEXPECTED_ERROR_MESSAGE = "Unexpected error"


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
    try:
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

        query = response.query
        query_parameters = response.queryParameters
        index_name = response.indexName

        configuration = {
            "query": query,
            "query_parameters": query_parameters,
            "index_name": index_name,
        }

        return configuration

    except grpc.RpcError as e:
        error_message = f"gRPC communication failed: {e.details()}"
        logger.error(error_message)
    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE} : {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=UNEXPECTED_ERROR_MESSAGE,
    )


def get_rag_configuration(grpc_host, virtual_host, rag_type):
    """Get rag configuration from grpc."""
    try:
        with grpc.insecure_channel(grpc_host) as channel:
            stub = searcher_pb2_grpc.SearcherStub(channel)
            response = stub.GetRAGConfigurations(
                searcher_pb2.GetRAGConfigurationsRequest(
                    virtualHost=virtual_host, ragType=rag_type
                )
            )

        prompt = response.prompt
        prompt_no_rag = response.promptNoRag
        rephrase_prompt = response.rephrasePrompt
        rag_tool_description = response.ragToolDescription
        chunk_window = response.chunkWindow
        reformulate = response.reformulate
        json_config = json_format.MessageToDict(response.jsonConfig)
        rerank = json_config.get("rerank")
        metadata = json_config.get("metadata")

        configuration = {
            "prompt": prompt,
            "prompt_no_rag": prompt_no_rag,
            "rephrase_prompt": rephrase_prompt,
            "rag_tool_description": rag_tool_description,
            "chunk_window": chunk_window,
            "reformulate": reformulate,
            "rerank": rerank,
            "metadata": metadata,
        }

        return configuration

    except grpc.RpcError as e:
        error_message = f"gRPC communication failed: {e.details()}"
        logger.error(error_message)
    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE} : {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=UNEXPECTED_ERROR_MESSAGE,
    )


def get_llm_configuration(grpc_host, virtual_host):
    """Get llm configuration from grpc."""
    try:
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

    except grpc.RpcError as e:
        error_message = f"gRPC communication failed: {e.details()}"
        logger.error(error_message)
    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE} : {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=UNEXPECTED_ERROR_MESSAGE,
    )


def get_embedding_model_configuration(grpc_host, virtual_host):
    """Get embedding model configuration from grpc."""
    try:
        with grpc.insecure_channel(grpc_host) as channel:
            stub = searcher_pb2_grpc.SearcherStub(channel)
            response = stub.GetEmbeddingModelConfigurations(
                searcher_pb2.GetEmbeddingModelConfigurationsRequest(
                    virtualHost=virtual_host,
                )
            )

        print(response)

        api_url = response.apiUrl
        api_key = response.apiKey
        model_type = response.providerModel.provider
        model = response.providerModel.model
        vector_size = response.vectorSize
        json_config = json_format.MessageToDict(response.jsonConfig)

        configuration = {
            "api_url": api_url,
            "api_key": api_key,
            "model_type": model_type,
            "model": model,
        }

        return configuration

    except grpc.RpcError as e:
        error_message = f"gRPC communication failed: {e.details()}"
        logger.error(error_message)
    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE} : {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=UNEXPECTED_ERROR_MESSAGE,
    )


def get_tenant_manager_configuration(grpc_host, virtual_host):
    """Get tenant configuration from grpc."""
    try:
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

    except grpc.RpcError as e:
        error_message = f"gRPC communication failed: {e.details()}"
        logger.error(error_message)
    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE} : {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=UNEXPECTED_ERROR_MESSAGE,
    )


def generate_documents_embeddings(
    grpc_host, openserach_host, chunk, embedding_model, document
):
    """Embedd uploaded documents."""
    try:
        with grpc.insecure_channel(grpc_host) as channel:
            stub = embedding_pb2_grpc.EmbeddingStub(channel)
            response = stub.GetMessages(
                embedding_pb2.EmbeddingRequest(
                    chunk=chunk, embeddingModel=embedding_model, text=document["text"]
                )
            )

            documents = []
            chunks = response.chunks
            for chunk in chunks:
                document = {
                    "filename": document["filename"],
                    "file_extension": document["file_extension"],
                    "user_id": document["user_id"],
                    "chat_id": document["chat_id"],
                    "chunk_number": chunk.number,
                    "total_chunks": chunk.total,
                    "chunkText": chunk.text,
                    "vector": list(chunk.vectors),
                }
                documents.append(document)

            save_uploaded_documents(openserach_host, documents)

            return documents

    except grpc.RpcError as e:
        error_message = f"gRPC communication failed: {e.details()}"
        logger.error(error_message)
    except Exception as e:
        logger.error(f"{UNEXPECTED_ERROR_MESSAGE} : {e}")
    raise HTTPException(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        detail=UNEXPECTED_ERROR_MESSAGE,
    )
