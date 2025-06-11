from typing import List, Optional

import requests
from langchain.schema import Document
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch

from app.rag.chunk_window import get_context_window_merged

TOKEN_SIZE = 3.5
MAX_CONTEXT_WINDOW_PERCENTAGE = 0.85
HYBRID_RETRIEVE_TYPE = "HYBRID"
VECTORIAL_RETRIEVE_TYPES = ["KNN", "HYBRID"]
SCORE_THRESHOLD = 0


class OpenSearchRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving documents."""

    query_data: dict
    search_text: str
    rerank: Optional[bool] = False
    reranker_api_url: Optional[str] = ""
    chunk_window: Optional[int] = 0
    context_window: int
    metadata: Optional[dict] = None
    retrieve_type: str
    opensearch_host: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:

        opensearch_query = self.query_data["query"]
        index_name = list(self.query_data["index_name"])
        params = (
            dict(self.query_data["query_parameters"])
            if self.retrieve_type == HYBRID_RETRIEVE_TYPE
            else None
        )

        documents = []

        if len(index_name) > 0:
            client = OpenSearch(
                hosts=[self.opensearch_host],
            )

            total_tokens = 0

            response = client.search(
                body=opensearch_query,
                index=index_name,
                params=params,
            )

            for row in response["hits"]["hits"]:
                score = row.get("_score")
                document_source = row.get("_source")
                if score < SCORE_THRESHOLD:
                    continue
                if self.retrieve_type in VECTORIAL_RETRIEVE_TYPES:
                    document_types = document_source.get("documentTypes")
                    dynamic_metadata = {}
                    for document_type in document_types:
                        if document_type in self.metadata.keys():
                            for key, value in self.metadata[document_type].items():
                                if metadata_value := document_source.get(
                                    document_type
                                ).get(value):
                                    dynamic_metadata[key] = metadata_value
                    document_id = document_source.get("contentId")
                    page_content = document_source.get("chunkText")
                    source = "local"
                    chunk_idx = document_source.get("number")
                    previous_chunks = document_source.get("previous")
                    previous_chunk = (
                        [element.get("chunkText") for element in previous_chunks]
                        if previous_chunks
                        else None
                    )
                    next_chunks = document_source.get("next")
                    next_chunk = (
                        [element.get("chunkText") for element in next_chunks]
                        if next_chunks
                        else None
                    )

                    metadata = {
                        "source": source,
                        "document_id": document_id,
                        "score": score,
                        "chunk_idx": chunk_idx,
                        "prev": previous_chunk,
                        "next": next_chunk,
                    }

                    metadata.update(dynamic_metadata)

                    document = Document(
                        page_content,
                        metadata=metadata,
                    )
                    document_tokens_number = len(page_content + source) / TOKEN_SIZE
                    total_tokens += document_tokens_number
                else:
                    document = Document(document_source.get("rawContent"), metadata={})
                    document_tokens_number = (
                        len(document_source.get("rawContent")) / TOKEN_SIZE
                    )
                    total_tokens += document_tokens_number

                if total_tokens < self.context_window * MAX_CONTEXT_WINDOW_PERCENTAGE:
                    documents.append(document)

        if self.rerank:
            documents_to_rerank = [
                {
                    "document_id": doc.metadata.get("document_id"),
                    "content": doc.page_content,
                }
                for doc in documents
            ]

            response = requests.get(
                self.reranker_api_url,
                json={
                    "query": self.search_text,
                    "context": documents_to_rerank,
                    "limit": len(documents_to_rerank),
                    "threshold": 0,
                    "max_length": 512,
                },
                timeout=None,
            )

            reranked_documents_dict = response.json()["context"]
            reranked_documents_ids = [
                doc["document_id"] for doc in reranked_documents_dict
            ]
            reranked_documents = sorted(
                documents,
                key=lambda x: reranked_documents_ids.index(x.metadata["document_id"]),
            )
            documents = reranked_documents

        if self.chunk_window > 0:
            documents_to_merge = [
                {
                    "document_id": doc.metadata["document_id"],
                    "chunk_idx": doc.metadata["chunk_idx"],
                    "prev": doc.metadata["prev"],
                    "next": doc.metadata["next"],
                    "title": doc.metadata["title"],
                    "url": doc.metadata["url"],
                    "source": doc.metadata["source"],
                    "score": doc.metadata["score"],
                    "content": doc.page_content,
                }
                for doc in documents
            ]
            merged_documents = get_context_window_merged(
                documents_to_merge, window_size=self.chunk_window
            )

            documents = []
            for merged_document in merged_documents:
                page_content = merged_document["content"]
                source = merged_document["source"]
                title = merged_document["title"]
                url = merged_document["url"]
                document_id = merged_document["document_id"]
                score = merged_document["score"]

                document = Document(
                    page_content,
                    metadata={
                        "source": source,
                        "title": title,
                        "url": url,
                        "document_id": document_id,
                        "score": score,
                    },
                )

                documents.append(document)

        return documents
