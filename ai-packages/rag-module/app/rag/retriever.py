from typing import List, Optional

from langchain.schema import Document
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch

from app.external_services.grpc.grpc_client import query_parser
from app.rag.chunk_window import get_context_window_merged

import requests

TOKEN_SIZE = 3.5
MAX_CONTEXT_WINDOW_PERCENTAGE = 0.85
HYBRID_RETRIEVE_TYPE = "HYBRID"
SCORE_THRESHOLD = 0.5


class OpenSearchRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving documents."""

    search_text: str
    rerank: Optional[bool] = False
    reranker_api_url: Optional[str] = ""
    chunk_window: Optional[bool] = True
    range_values: list
    after_key: Optional[str] = None
    suggest_keyword: Optional[str] = None
    suggestion_category_id: Optional[int] = None
    virtual_host: str
    jwt: Optional[str] = None
    extra: Optional[dict] = None
    sort: Optional[list] = None
    sort_after_key: Optional[str] = None
    language: Optional[str] = None
    vector_indices: Optional[bool] = True
    context_window: int
    retrieve_type: str
    opensearch_host: str
    grpc_host: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        search_query = [
            {
                "entityType": "",
                "entityName": "",
                "tokenType": self.retrieve_type,
                "keywordKey": "",
                "values": [query],
                "extra": {},
                "filter": True,
            }
        ]
        query_data = query_parser(
            search_query=search_query,
            range_values=self.range_values,
            after_key=self.after_key,
            suggest_keyword=self.suggest_keyword,
            suggestion_category_id=self.suggestion_category_id,
            virtual_host=self.virtual_host,
            jwt=self.jwt,
            extra=self.extra,
            sort=self.sort,
            sort_after_key=self.sort_after_key,
            language=self.language,
            vector_indices=self.vector_indices,
            grpc_host=self.grpc_host,
        )
        query = query_data.query
        index_name = list(query_data.indexName)
        params = (
            dict(query_data.queryParameters)
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
                body=query,
                index=index_name,
                params=params,
            )

            for row in response["hits"]["hits"]:
                score = row["_score"]
                if score < SCORE_THRESHOLD:
                    continue
                if self.vector_indices:
                    document_id = row["_source"]["contentId"]
                    page_content = row["_source"]["chunkText"]
                    title = row["_source"]["title"]
                    url = row["_source"]["url"]
                    source = "local"
                    chunk_idx = row["_source"]["number"]
                    prev_chunk = [
                        element["chunkText"] for element in row["_source"]["previous"]
                    ]
                    next_chunk = [
                        element["chunkText"] for element in row["_source"]["next"]
                    ]

                    document = Document(
                        page_content,
                        metadata={
                            "source": source,
                            "title": title,
                            "url": url,
                            "document_id": document_id,
                            "score": score,
                            "chunk_idx": chunk_idx,
                            "prev": prev_chunk,
                            "next": next_chunk,
                        },
                    )
                    document_tokens_number = (
                        len(page_content + title + url + source) / TOKEN_SIZE
                    )
                    total_tokens += document_tokens_number
                else:
                    document = Document(row["_source"]["rawContent"], metadata={})
                    document_tokens_number = (
                        len(row["_source"]["rawContent"]) / TOKEN_SIZE
                    )
                    total_tokens += document_tokens_number

                if total_tokens < self.context_window * MAX_CONTEXT_WINDOW_PERCENTAGE:
                    documents.append(document)

        if self.rerank:
            documents_to_rerank = [
                {
                    "document_id": doc.metadata["document_id"],
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

        if self.chunk_window:
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
                documents_to_merge, window_size=4
            )

            for merged_document in merged_documents:
                page_content = merged_document["content"]
                source = merged_document["content"]
                title = merged_document["title"]
                url = merged_document["url"]
                document_id = merged_document["document_id"]
                score = merged_document["score"]

                documents = []

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
