from typing import List

from langchain.schema import Document
from langchain_core.callbacks.manager import CallbackManagerForRetrieverRun
from langchain_core.retrievers import BaseRetriever
from opensearchpy import OpenSearch


class OpenSearchUploadedDocumentsRetriever(BaseRetriever):
    """Retriever that uses OpenSearch's store for retrieving uploaded documents."""

    opensearch_host: str
    uploaded_documents_index: str
    user_id: str
    chat_id: str

    def _get_relevant_documents(
        self, query: str, *, run_manager: CallbackManagerForRetrieverRun
    ) -> List[Document]:
        open_search_client = OpenSearch(
            hosts=[self.opensearch_host],
        )

        documents = []

        if open_search_client.indices.exists(index=self.uploaded_documents_index):
            open_search_query = {
                "query": {
                    "bool": {
                        "must": [
                            {"term": {"chat_id.keyword": self.chat_id}},
                            {"term": {"user_id.keyword": self.user_id}},
                        ]
                    }
                }
            }
            response = open_search_client.search(
                body=open_search_query, index=self.uploaded_documents_index
            )

            for row in response["hits"]["hits"]:
                document_source = row.get("_source")
                filename = document_source.get("filename")
                file_extension = document_source.get("file_extension")
                score = row.get("_score", 0)
                uploaded_documents = document_source.get("chunks", [])

                for item in uploaded_documents:
                    document = Document(
                        item,
                        metadata={
                            "document_id": filename,
                            "filename": filename,
                            "file_extension": file_extension,
                            "score": score,
                        },
                    )
                    documents.append(document)

        return documents
