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

from app.external_services.grpc.grpc_client import (
    generate_documents_embeddings,
)


def documents_embedding(
    grpc_host_embedding,
    embedding_model_configuration,
    document,
):
    """
    Prepare embedding configuration and generate document embeddings.

    This function acts as a wrapper that transforms embedding model configuration
    into the proper format required by the gRPC embedding service, then calls
    the service to generate vector embeddings for document chunks.

    :param grpc_host_embedding: gRPC server host address for the embedding service
    :type grpc_host_embedding: str
    :param embedding_model_configuration: Dictionary containing embedding model settings
    :type embedding_model_configuration: dict
    :param document: Document dictionary containing text content and metadata
    :type document: dict

    :return: List of document chunks with generated vector embeddings and metadata
    :rtype: list[dict]

    :raises HTTPException 500: If gRPC communication fails or unexpected error occurs

    :Example:

    .. code-block:: python

        embedded_docs = documents_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration={
                "api_url": "https://api.openai.com/v1/embeddings",
                "api_key": "sk-...",
                "model_type": "OPEN_AI",
                "model": "text-embedding-ada-002",
                "vector_size": 1536,
                "json_config": {"batch_size": 32}
            },
            document={
                "filename": "research_paper.pdf",
                "file_extension": ".pdf",
                "user_id": "user_123",
                "chat_id": "chat_456",
                "text": "Full document text content here..."
            }
        )

        # Returns list of chunks with embeddings (same structure as generate_documents_embeddings)

    .. note::
        - Transforms high-level embedding configuration into gRPC-compatible format
        - Combines provider information and model details into provider_model structure
        - Preserves all original document metadata in the embedding process
        - The actual embedding generation is delegated to the gRPC service

    .. warning::
        - gRPC communication failures will propagate from the underlying service
        - Ensure all required embedding configuration fields are present
        - API keys are passed through to the gRPC service - ensure secure handling
        - Chunking type and JSON config must be compatible with the embedding service

    .. seealso::
        - :func:`generate_documents_embeddings` The underlying gRPC embedding function
        - :func:`get_embedding_model_configuration` For retrieving embedding configuration
        - :func:`save_uploaded_documents` For storing the generated embeddings

    Configuration Transformation:
        This function transforms the embedding configuration into the format
        expected by the gRPC service:

        * **chunk**: Contains chunking strategy and JSON configuration
        * **embedding_model**: Contains API key, provider model details, and JSON config
        * **provider_model**: Nested structure with provider type and model name
    """
    api_url = embedding_model_configuration.get("api_url")
    api_key = embedding_model_configuration.get("api_key")
    model_type = embedding_model_configuration.get("model_type")
    model = embedding_model_configuration.get("model")
    vector_size = embedding_model_configuration.get("vector_size")
    json_config = embedding_model_configuration.get("json_config")

    chunk = {"type": 1, "jsonConfig": json_config}
    provider_model = {"provider": model_type, "model": model}
    embedding_model = {
        "apiKey": api_key,
        "providerModel": provider_model,
        "jsonConfig": json_config,
    }

    embedded_documents = generate_documents_embeddings(
        grpc_host_embedding, chunk, embedding_model, document
    )

    return embedded_documents
