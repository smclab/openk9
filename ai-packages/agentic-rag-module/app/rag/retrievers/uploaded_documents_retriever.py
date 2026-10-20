from typing import List, Optional

from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever

from app.external_services.grpc.grpc_client import query_parser
from app.utils.embedding import query_embedding
from app.utils.opensearch_client import get_opensearch_client

VECTORIAL_RETRIEVE_TYPES = ["KNN", "HYBRID"]
HYBRID_RETRIEVE_TYPE = "HYBRID"


class OpenSearchUploadedDocumentsRetriever(BaseRetriever):
    """
    LangChain retriever for searching through user-uploaded documents in OpenSearch.

    This retriever specializes in searching through documents that users have previously
    uploaded and stored in a tenant-specific OpenSearch index. It supports both vector
    similarity search and keyword search, with proper user and chat isolation.

    :param opensearch_host: OpenSearch cluster host URL
    :type opensearch_host: str
    :param grpc_host_embedding: gRPC service host for embedding generation
    :type grpc_host_embedding: str
    :param embedding_model_configuration: Configuration for the embedding model
    :type embedding_model_configuration: dict
    :param uploaded_documents_index: Name of the OpenSearch index containing uploaded documents
    :type uploaded_documents_index: str
    :param retrieve_type: Type of retrieval to perform ("KNN", "HYBRID", or keyword)
    :type retrieve_type: str
    :param user_id: Unique identifier for the user (for document isolation)
    :type user_id: str
    :param chat_id: Unique identifier for the chat session (for document isolation)
    :type chat_id: str
    :param search_text: The search query text to find relevant documents
    :type search_text: str
    :param grpc_host: gRPC host of the datasource, asked for the search pipeline of
        the tenant's Search Config
    :type grpc_host: str, optional
    :param tenant_id: Tenant whose Search Config applies
    :type tenant_id: str, optional
    :param jwt: JWT of the user, forwarded to the datasource
    :type jwt: str, optional

    :Example:

    .. code-block:: python

        retriever = OpenSearchUploadedDocumentsRetriever(
            opensearch_host="http://localhost:9200",
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration={
                "api_url": "https://api.openai.com/v1/embeddings",
                "api_key": "sk-...",
                "model_type": "OPEN_AI",
                "model": "text-embedding-ada-002"
            },
            uploaded_documents_index="my-realm-uploaded-documents-index",
            retrieve_type="HYBRID",
            user_id="user_123",
            chat_id="chat_456",
            search_text="What is machine learning?"
        )


    .. note::
        - Only searches within documents uploaded by the specific user and chat session
        - Supports hybrid search combining vector similarity and keyword matching
        - With the "HYBRID" retrieval type, the hybrid search runs through the search
          pipeline of the tenant's Search Config, as the main retriever does, and
          through the index default pipeline when the bucket has no Search Config
        - Automatically generates embeddings for the query using gRPC service
        - Applies filters to ensure users can only access their own documents
        - Returns LangChain Document objects with metadata for RAG pipelines

    .. warning::
        - Requires the uploaded documents index to exist in OpenSearch
        - User must be authenticated to access their uploaded documents
        - gRPC embedding service must be available and responsive
        - Vector search only works if documents were embedded with compatible model

    .. seealso::
        - :class:`BaseRetriever` Base LangChain retriever class
        - :class:`OpenSearchRetriever` For general document retrieval
        - :func:`query_embedding` For generating query embeddings
    """

    opensearch_host: str
    grpc_host_embedding: str
    embedding_model_configuration: dict
    uploaded_documents_index: str
    retrieve_type: str
    user_id: str
    chat_id: str
    search_text: str
    grpc_host: Optional[str] = None
    tenant_id: Optional[str] = None
    jwt: Optional[str] = None

    def _search_pipeline(self) -> Optional[str]:
        """Name of the search pipeline of the tenant's Search Config, or None
        when the bucket has none.

        The datasource only reports it for a query with a hybrid token, and
        embeds the token's text to build that query. The token is sent without
        text: the query is already embedded here, and only the pipeline name
        is read back.
        """
        query_data = query_parser(
            search_query=[
                {
                    "entityType": "",
                    "entityName": "",
                    "tokenType": HYBRID_RETRIEVE_TYPE,
                    "keywordKey": "",
                    "values": [],
                    "extra": {},
                    "filter": True,
                }
            ],
            range_values=[],
            after_key=None,
            suggest_keyword=None,
            suggestion_category_id=None,
            tenant_id=self.tenant_id,
            jwt=self.jwt,
            extra={},
            sort=None,
            sort_after_key=None,
            language=None,
            grpc_host=self.grpc_host,
        )

        return dict(query_data["query_parameters"]).get("search_pipeline")

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        """
        Retrieve relevant documents from user's uploaded documents in OpenSearch.

        This method performs the actual document retrieval by:
        1. Generating vector embeddings for the query
        2. Constructing appropriate OpenSearch queries based on retrieval type
        3. Applying user and chat filters for security isolation
        4. Converting results to LangChain Document format

        :param query: The search query to find relevant documents
        :type query: str
        :param run_manager: Callback manager for retriever execution tracking
        :type run_manager: CallbackManagerForRetrieverRun

        :return: List of relevant documents with metadata
        :rtype: List[Document]

        :raises ConnectionError: If OpenSearch or gRPC services are unavailable
        :raises Exception: If embedding generation or search operations fail

        .. note::
            - For "KNN" and "HYBRID" retrieval types, uses vector similarity search
            - For other retrieval types, uses traditional keyword matching
            - Always filters by user_id and chat_id for security isolation
            - Returns empty list if the uploaded documents index doesn't exist
        """
        open_search_client = get_opensearch_client(self.opensearch_host)

        vector_query = query_embedding(
            grpc_host_embedding=self.grpc_host_embedding,
            embedding_model_configuration=self.embedding_model_configuration,
            text=self.search_text,
        )

        # Degenerate input (empty/whitespace/emoji-only) has nothing to embed;
        # with no vector there is nothing to retrieve.
        if vector_query is None:
            return []

        documents = []

        if open_search_client.indices.exists(index=self.uploaded_documents_index):
            filters = [
                {"term": {"user_id.keyword": self.user_id}},
                {"term": {"chat_id.keyword": self.chat_id}},
            ]
            if self.retrieve_type in VECTORIAL_RETRIEVE_TYPES:
                # Both branches are restricted to the user chat while scoring:
                # an approximate knn would pick its k neighbours among every
                # chat of the tenant, and indices created on nmslib reject a
                # knn filter. The vector branch therefore scores the chat
                # chunks exactly, with the l2 space of the index mapping.
                open_search_query = {
                    "query": {
                        # Text branch first, as the datasource builds it, so that
                        # the weights of a Search Config mean the same here.
                        "hybrid": {
                            "queries": [
                                {
                                    "bool": {
                                        "must": [
                                            {
                                                "match": {
                                                    "chunkText": {
                                                        "query": self.search_text,
                                                        "boost": 0.5,
                                                    }
                                                }
                                            }
                                        ],
                                        "filter": filters,
                                    }
                                },
                                {
                                    "script_score": {
                                        "query": {"bool": {"filter": filters}},
                                        "script": {
                                            "source": "knn_score",
                                            "lang": "knn",
                                            "params": {
                                                "field": "vector",
                                                "query_value": vector_query,
                                                "space_type": "l2",
                                            },
                                        },
                                    }
                                },
                            ]
                        }
                    },
                }
            else:
                open_search_query = {
                    "query": {
                        "bool": {
                            "must": [
                                {
                                    "match": {
                                        "chunkText": {
                                            "query": self.search_text,
                                        }
                                    }
                                }
                            ],
                            "filter": filters,
                        }
                    },
                }

            search_pipeline = (
                self._search_pipeline()
                if self.retrieve_type == HYBRID_RETRIEVE_TYPE
                and self.grpc_host
                and self.tenant_id
                else None
            )

            if search_pipeline:
                response = open_search_client.search(
                    body=open_search_query,
                    index=self.uploaded_documents_index,
                    params={"search_pipeline": search_pipeline},
                )
            else:
                response = open_search_client.search(
                    body=open_search_query, index=self.uploaded_documents_index
                )

            for row in response["hits"]["hits"]:
                document_source = row.get("_source")
                document_id = document_source.get("document_id")
                filename = document_source.get("filename")
                file_extension = document_source.get("file_extension")
                score = row.get("_score", 0)
                page_content = document_source.get("chunkText", "")

                document = Document(
                    page_content,
                    metadata={
                        "document_id": document_id,
                        "filename": filename,
                        "file_extension": file_extension,
                        "title": f"{filename or ''}{file_extension or ''}",
                        "score": score,
                    },
                )
                documents.append(document)

        return documents
