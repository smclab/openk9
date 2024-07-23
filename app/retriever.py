from typing import List

from langchain.schema import Document
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch

from app.grpc.grpc_client import query_parser

OPENSEARCH_HOST = "https://opensearch-backend.openk9.io/"


class OpenSearchRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving documents."""

    def _get_relevant_documents(
        searchQuery,
        range,
        afterKey,
        suggestKeyword,
        suggestionCategoryId,
        virtualHost,
        jwt,
        extra,
        sort,
        sortAfterKey,
        language,
        vectorIndices,
    ) -> List[Document]:
        query_data = query_parser(
            searchQuery,
            range,
            afterKey,
            suggestKeyword,
            suggestionCategoryId,
            virtualHost,
            jwt,
            extra,
            sort,
            sortAfterKey,
            language,
            vectorIndices,
        )
        query = query_data.query
        index_name = query_data.indexName[0]

        client = OpenSearch(
            hosts=[OPENSEARCH_HOST],
        )

        response = client.search(body=query, index=index_name)

        documents = []

        for row in response["hits"]["hits"]:
            document = Document(row["_source"]["web"]["content"])
            documents.append(document)

        return documents
