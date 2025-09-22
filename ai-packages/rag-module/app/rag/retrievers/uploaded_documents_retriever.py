from typing import List

from langchain.schema import Document
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch

from app.utils.embedding import documents_embedding

VECTORIAL_RETRIEVE_TYPES = ["KNN", "HYBRID"]


class OpenSearchUploadedDocumentsRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving uploaded documents."""

    opensearch_host: str
    grpc_host_datasource: str
    grpc_host_embedding: str
    virtual_host: str
    uploaded_documents_index: str
    retrieve_type: str
    user_id: str
    chat_id: str
    search_text: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        open_search_client = OpenSearch(
            hosts=[self.opensearch_host],
        )

        document = {
            "text": self.search_text,
        }

        embedded_query = documents_embedding(
            grpc_host_datasource=self.grpc_host_datasource,
            grpc_host_embedding=self.grpc_host_embedding,
            virtual_host=self.virtual_host,
            document=document,
        )

        vector_query = embedded_query[0].get("vector")

        documents = []

        if open_search_client.indices.exists(index=self.uploaded_documents_index):
            filters = [
                {"term": {"user_id.keyword": self.user_id}},
                {"term": {"chat_id.keyword": self.chat_id}},
            ]
            if self.retrieve_type in VECTORIAL_RETRIEVE_TYPES:
                open_search_query = {
                    "query": {
                        "hybrid": {
                            "queries": [
                                {"knn": {"vector": {"vector": vector_query, "k": 10}}},
                                {
                                    "match": {
                                        "chunkText": {
                                            "query": self.search_text,
                                            "boost": 0.5,
                                        }
                                    }
                                },
                            ]
                        }
                    },
                    "post_filter": {"bool": {"filter": filters}},
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

            response = open_search_client.search(
                body=open_search_query, index=self.uploaded_documents_index
            )

            for row in response["hits"]["hits"]:
                document_source = row.get("_source")
                document_id = row.get("_id")
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
                        "score": score,
                    },
                )
                documents.append(document)

        return documents
