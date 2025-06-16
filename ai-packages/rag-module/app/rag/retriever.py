from typing import List, Optional

import requests
from langchain.schema import Document
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch

from app.external_services.grpc.grpc_client import query_parser
from app.rag.chunk_window import get_context_window_merged

TOKEN_SIZE = 3.5
MAX_CONTEXT_WINDOW_PERCENTAGE = 0.85
HYBRID_RETRIEVE_TYPE = "HYBRID"
VECTORIAL_RETRIEVE_TYPES = ["KNN", "HYBRID"]
SCORE_THRESHOLD = 0


class OpenSearchRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving documents."""

    search_query: Optional[list] = None
    search_text: str
    rerank: Optional[bool] = False
    reranker_api_url: Optional[str] = ""
    chunk_window: Optional[int] = 0
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
    context_window: int
    metadata: Optional[dict] = None
    retrieve_type: str
    opensearch_host: str
    grpc_host: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:

        search_query = (
            [
                {
                    "entityType": query_element.entityType,
                    "entityName": query_element.entityName,
                    "tokenType": query_element.tokenType,
                    "keywordKey": query_element.keywordKey,
                    "values": query_element.values,
                    "extra": query_element.extra,
                    "filter": query_element.filter,
                }
                for query_element in self.search_query
            ]
            if self.search_query
            else [
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
        )

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
            grpc_host=self.grpc_host,
        )

        query = query_data["query"]
        index_name = list(query_data["index_name"])
        params = (
            dict(query_data["query_parameters"])
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
