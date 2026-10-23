from typing import List

from app.utils.embedding import query_embedding
from app.utils.opensearch_client import get_opensearch_client
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever

VECTORIAL_RETRIEVE_TYPES = ["KNN", "HYBRID"]


class OpenSearchDomainDocumentsRetriever(BaseRetriever):
    """
    LangChain retriever for searching through domain documents in OpenSearch.

    This retriever searches the domain documents that an administrator has embedded
    for a tenant, each labelled with its domain. Tenant isolation comes from the index
    name (``<tenant_id>-domain-documents-index``): neither the query nor the domain
    aggregation applies a filter. It supports both vector similarity search and
    keyword search.

    :param opensearch_host: OpenSearch cluster host URL
    :type opensearch_host: str
    :param grpc_host_embedding: gRPC service host for embedding generation
    :type grpc_host_embedding: str
    :param embedding_model_configuration: Configuration for the embedding model
    :type embedding_model_configuration: dict
    :param uploaded_documents_index: Name of the OpenSearch index containing domain documents
    :type uploaded_documents_index: str
    :param retrieve_type: Type of retrieval to perform ("KNN", "HYBRID", or keyword)
    :type retrieve_type: str
    :param search_text: The search query text to find relevant documents
    :type search_text: str

    :Example:

    .. code-block:: python

        retriever = OpenSearchDomainDocumentsRetriever(
            opensearch_host="http://localhost:9200",
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration={
                "api_url": "https://api.openai.com/v1/embeddings",
                "api_key": "sk-...",
                "model_type": "OPEN_AI",
                "model": "text-embedding-ada-002"
            },
            uploaded_documents_index="my-tenant-domain-documents-index",
            retrieve_type="HYBRID",
            search_text="What is machine learning?"
        )


    .. note::
        - Only searches within the domain documents of the index it is given
        - Vector retrieval runs a pure KNN query, so each score is an absolute similarity
        - Automatically generates embeddings for the query using gRPC service
        - Returns LangChain Document objects with metadata for RAG pipelines

    .. warning::
        - Returns no documents and no domains if the domain index does not exist
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
    search_text: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        """
        Retrieve relevant documents from the domain documents in OpenSearch.

        This method performs the actual document retrieval by:
        1. Generating vector embeddings for the query
        2. Constructing appropriate OpenSearch queries based on retrieval type
        3. Converting results to LangChain Document format

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
            - Applies no filter: isolation comes from the index name
            - Returns empty list if the domain documents index doesn't exist
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
            if self.retrieve_type in VECTORIAL_RETRIEVE_TYPES:
                # A pure KNN query: its score is an absolute similarity the
                # caller can hold against a fixed threshold, while a hybrid
                # score is normalized against the other hits of the same query.
                open_search_query = {
                    "query": {"knn": {"vector": {"vector": vector_query, "k": 10}}},
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
                        }
                    },
                }

            response = open_search_client.search(
                body=open_search_query, index=self.uploaded_documents_index
            )

            for row in response["hits"]["hits"]:
                document_source = row.get("_source")
                document_id = document_source.get("document_id")
                domain = document_source.get("domain")
                score = row.get("_score", 0)
                page_content = document_source.get("chunkText", "")

                document = Document(
                    page_content,
                    metadata={
                        "domain": domain,
                        "document_id": document_id,
                        "score": score,
                    },
                )
                documents.append(document)

        return documents

    def get_domains(
        self,
        size: int = 1000,
    ) -> List[str]:
        open_search_client = get_opensearch_client(self.opensearch_host)
        if not open_search_client.indices.exists(index=self.uploaded_documents_index):
            return []

        response = open_search_client.search(
            index=self.uploaded_documents_index,
            body={
                "size": 0,
                "aggs": {
                    "distinct_domains": {
                        "terms": {
                            "field": "domain",
                            "size": size,
                        }
                    }
                },
            },
        )
        return [
            bucket["key"]
            for bucket in response["aggregations"]["distinct_domains"]["buckets"]
        ]
