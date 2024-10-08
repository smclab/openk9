from typing import List, Optional

from langchain.schema import Document
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch

from app.external_services.grpc.grpc_client import query_parser


class OpenSearchRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving documents."""

    search_query: list
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
    opensearch_host: str
    grpc_host: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        search_query = (
            self.search_query
            if self.search_query
            else [
                {
                    "entityType": "",
                    "entityName": "",
                    "tokenType": "KNN",
                    "keywordKey": None,
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
            vector_indices=self.vector_indices,
            grpc_host=self.grpc_host,
        )
        query = query_data.query
        index_name = list(query_data.indexName)

        documents = []

        if len(index_name) > 0:
            client = OpenSearch(
                hosts=[self.opensearch_host],
            )

            response = client.search(body=query, index=index_name)

            for row in response["hits"]["hits"]:
                if self.vector_indices:
                    page_content = row["_source"]["chunkText"]
                    title = row["_source"]["title"]
                    url = row["_source"]["url"]
                    source = "local"
                    document = Document(
                        page_content,
                        metadata={"source": source, "title": title, "url": url},
                    )
                else:
                    document = Document(row["_source"]["rawContent"], metadata={})
                documents.append(document)

        return documents
