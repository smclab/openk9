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

import time

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
    """
    Retrieve RAG (Retrieval-Augmented Generation) configuration from gRPC service.

    This function communicates with a gRPC service to fetch RAG-specific configuration
    for a particular virtual host/tenant and RAG type, including prompts, chunking settings,
    and retrieval parameters.

    :param grpc_host: gRPC server host address for the configuration service
    :type grpc_host: str
    :param virtual_host: Virtual host identifier for tenant-specific configuration
    :type virtual_host: str
    :param rag_type: Type of RAG configuration to retrieve (e.g., "CHAT_RAG", "DOCUMENT_RAG")
    :type rag_type: str

    :return: Dictionary containing complete RAG configuration for the specified type and tenant
    :rtype: dict

    :raises HTTPException 500: If gRPC communication fails or unexpected error occurs

    :Example:

    .. code-block:: python

        config = get_rag_configuration(
            grpc_host="localhost:50051",
            virtual_host="tenant1.example.com",
            rag_type="CHAT_RAG"
        )

        # Returns:
        # {
        #     "prompt": "You are a helpful assistant...",
        #     "prompt_no_rag": "Answer based on your knowledge...",
        #     "rephrase_prompt": "Rephrase the following question...",
        #     "rag_tool_description": "Searches through documents...",
        #     "chunk_window": 512,
        #     "reformulate": True,
        #     "rerank": False,
        #     "metadata": {}
        # }

    .. note::
        - Supports different RAG types for various use cases
        - Configuration is tenant-specific based on virtual_host
        - Includes multiple prompt templates for different scenarios
        - Handles document chunking and retrieval optimization
        - Converts protobuf JSON configuration to Python dictionary
        - Supports metadata for extended configuration options

    .. warning::
        - gRPC communication failures will result in HTTP 500 errors
        - Ensure gRPC server is running and accessible at grpc_host
        - Virtual host must correspond to a configured tenant with RAG settings
        - RAG type must match available configurations on the server

    .. seealso::
        - :class:`searcher_pb2.GetRAGConfigurationsRequest` gRPC request message
        - :class:`searcher_pb2_grpc.SearcherStub` gRPC service stub
        - :func:`json_format.MessageToDict` for protobuf to dictionary conversion
        - :func:`get_llm_configuration` For retrieving LLM-specific configurations

    Configuration Dictionary Fields:
        * **prompt** (str): Primary prompt template for RAG-enhanced responses
        * **prompt_no_rag** (str): Prompt template for when the RAG tool is not called
        * **rephrase_prompt** (str): Prompt template for query rephrasing
        * **rag_tool_description** (str): Description of the RAG tool for RAG as tool
        * **chunk_window** (int): Size of text chunks for document processing and retrieval
        * **reformulate** (bool): Whether to enable query reformulation for better retrieval
        * **enable_conversation_title** (bool): Whether to enable conversation title
        * **rerank** (bool): Whether to enable re-ranking of retrieved results
        * **metadata** (dict): Additional metadata
    """
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
        enable_conversation_title = response.enableConversationTitle
        range_values = response.range
        json_config = json_format.MessageToDict(response.jsonConfig)
        rerank = json_config.get("rerank")
        metadata = json_config.get("metadata")
        analyze_query_prompt = json_config.get("analyze_query_prompt", "")
        enable_real_time_evaluation = json_config.get(
            "enable_real_time_evaluation", False
        )
        bypass_rag = json_config.get("bypass_rag", False)

        enable_input_guardrail = json_config.get("enable_input_guardrail", True)
        input_guardrail_threshold = json_config.get("input_guardrail_threshold", 0.5)
        input_guardrail_provider = json_config.get("input_guardrail_provider", "")
        input_guardrail_aws_bedrock = json_config.get("input_guardrail_aws_bedrock", {})
        input_guardrail_google_model_armor = json_config.get(
            "input_guardrail_google_model_armor", {}
        )
        input_guardrail_openai_moderation = json_config.get(
            "input_guardrail_openai_moderation", {}
        )

        output_guardrail_type = json_config.get("output_guardrail_type", 0)
        output_guardrail_chunk_interval = json_config.get(
            "output_guardrail_chunk_interval", 10
        )
        output_guardrail_provider = json_config.get(
            "output_guardrail_provider", "openai_moderation"
        )
        output_guardrail_aws_bedrock = json_config.get(
            "output_guardrail_aws_bedrock", {}
        )
        output_guardrail_google_model_armor = json_config.get(
            "output_guardrail_google_model_armor", {}
        )
        output_guardrail_openai_moderation = json_config.get(
            "output_guardrail_openai_moderation", {}
        )

        domain_threshold = json_config.get("domain_threshold", 0.7)

        input_guardrail = {
            "enable_input_guardrail": enable_input_guardrail,
            "input_guardrail_threshold": input_guardrail_threshold,
            "input_guardrail_provider": input_guardrail_provider,
            "input_guardrail_aws_bedrock": input_guardrail_aws_bedrock,
            "input_guardrail_google_model_armor": input_guardrail_google_model_armor,
            "input_guardrail_openai_moderation": input_guardrail_openai_moderation,
        }

        output_guardrail = {
            "output_guardrail_type": output_guardrail_type,
            "output_guardrail_chunk_interval": output_guardrail_chunk_interval,
            "output_guardrail_provider": output_guardrail_provider,
            "output_guardrail_aws_bedrock": output_guardrail_aws_bedrock,
            "output_guardrail_google_model_armor": output_guardrail_google_model_armor,
            "output_guardrail_openai_moderation": output_guardrail_openai_moderation,
        }

        guardrails_configuration = {
            "input_guardrail": input_guardrail,
            "output_guardrail": output_guardrail,
        }

        configuration = {
            "prompt": prompt,
            "prompt_no_rag": prompt_no_rag,
            "rephrase_prompt": rephrase_prompt,
            "rag_tool_description": rag_tool_description,
            "analyze_query_prompt": analyze_query_prompt,
            "enable_real_time_evaluation": enable_real_time_evaluation,
            "chunk_window": chunk_window,
            "reformulate": reformulate,
            "enable_conversation_title": enable_conversation_title,
            "bypass_rag": bypass_rag,
            "range_values": range_values,
            "rerank": rerank,
            "metadata": metadata,
            "guardrails_configuration": guardrails_configuration,
            "domain_threshold": domain_threshold,
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
        aws_bedrock = json_config.get("aws_bedrock")

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
            "aws_bedrock": aws_bedrock,
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
            "vector_size": vector_size,
            "json_config": json_config,
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


def generate_documents_embeddings(grpc_host, chunk, embedding_model, document):
    """
    Generate embeddings for uploaded documents using gRPC embedding service.

    This function sends document text to a gRPC embedding service which processes the text
    into chunks and generates vector embeddings for each chunk. The embeddings are used
    for semantic search and similarity matching in vector databases.

    :param grpc_host: gRPC server host address for the embedding service
    :type grpc_host: str
    :param chunk: Chunking configuration parameters for text segmentation
    :type chunk: dict
    :param embedding_model: Embedding model configuration to use for vector generation
    :type embedding_model: dict
    :param document: Document dictionary containing text content and metadata
    :type document: dict

    :return: List of document chunks with generated vector embeddings and metadata
    :rtype: list[dict]

    :raises HTTPException 500: If gRPC communication fails or unexpected error occurs

    :Example:

    .. code-block:: python

        embedded_docs = generate_documents_embeddings(
            grpc_host="localhost:50053",
            chunk={"type": 1, "jsonConfig": json_config},
            embedding_model={
                "apiKey": api_key,
                "providerModel": provider_model,
                "jsonConfig": json_config,
            },
            document={
                "filename": "report.pdf",
                "file_extension": ".pdf",
                "user_id": "user_123",
                "chat_id": "chat_456",
                "text": "This is the document content..."
            }
        )

        # Returns:
        # [
        #     {
        #         "filename": "report.pdf",
        #         "file_extension": ".pdf",
        #         "user_id": "user_123",
        #         "chat_id": "chat_456",
        #         "chunk_number": 1,
        #         "total_chunks": 3,
        #         "chunkText": "This is the first chunk...",
        #         "vector": [0.1, 0.2, 0.3, ...]
        #     },
        #     ...
        # ]

    .. note::
        - Automatically chunks large documents into smaller segments for processing
        - Preserves document metadata across all generated chunks
        - Each chunk includes positional information (chunk_number, total_chunks)
        - Vectors are typically high-dimensional arrays (e.g., 768, 1536 dimensions)
        - Used for storing documents in vector databases for semantic search

    .. warning::
        - gRPC communication failures will result in HTTP 500 errors
        - Ensure embedding gRPC server is running and accessible at grpc_host
        - Large documents may generate many chunks, impacting storage and performance
        - Vector dimensions must match the expected size in the target vector database

    .. seealso::
        - :class:`embedding_pb2.EmbeddingRequest` gRPC request message
        - :class:`embedding_pb2_grpc.EmbeddingStub` gRPC service stub
        - :func:`get_embedding_model_configuration` For retrieving embedding model settings
        - :func:`save_uploaded_documents` For storing embedded documents in OpenSearch

    Return List Structure:
        Each item in the returned list contains:

        * **filename** (str): Original filename of the source document
        * **file_extension** (str): File extension indicating document type
        * **document_id** (str): Document identifier
        * **user_id** (str): User identifier who uploaded the document
        * **chat_id** (str): Chat session identifier for document association
        * **chunk_number** (int): Sequential position of this chunk in the document
        * **total_chunks** (int): Total number of chunks generated from the document
        * **chunkText** (str): The actual text content of this specific chunk
        * **vector** (list[float]): Embedding vector representation of the chunk text
    """
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
                timestamp = int(time.time() * 1000)
                document = {
                    "timestamp": timestamp,
                    "document_id": document.get("document_id"),
                    "chunk_number": chunk.number,
                    "total_chunks": chunk.total,
                    "chunkText": chunk.text,
                    "vector": list(chunk.vectors),
                }
                documents.append(document)

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
