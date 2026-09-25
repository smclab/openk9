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


from unittest.mock import MagicMock, patch

import pytest
from fastapi import HTTPException
from langchain_core.documents import Document

from app.rag import agentic_rag
from app.rag.agentic_rag import GraphState, RagGraph
from app.rag.retrievers import uploaded_documents_retriever
from app.rag.retrievers.uploaded_documents_retriever import (
    OpenSearchUploadedDocumentsRetriever,
)

VECTOR = [0.1, 0.2]
# only this user's documents in this chat may be searched
ISOLATION_FILTERS = [
    {"term": {"user_id.keyword": "user-1"}},
    {"term": {"chat_id.keyword": "chat-1"}},
]


def _build_retriever(retrieve_type="TEXT"):
    return OpenSearchUploadedDocumentsRetriever(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={"model": "an-embedding-model"},
        uploaded_documents_index="test-index",
        retrieve_type=retrieve_type,
        user_id="user-1",
        chat_id="chat-1",
        search_text="zephyr",
    )


def test_uploaded_document_metadata_includes_title_built_from_filename():
    retriever = _build_retriever()

    client = MagicMock()
    client.indices.exists.return_value = True
    client.search.return_value = {
        "hits": {
            "hits": [
                {
                    "_source": {
                        "document_id": "doc-1",
                        "filename": "zephyr2",
                        "file_extension": ".md",
                        "chunkText": "some content",
                    },
                    "_score": 3.65,
                }
            ]
        }
    }

    with patch.object(
        uploaded_documents_retriever, "get_opensearch_client", return_value=client
    ), patch.object(
        uploaded_documents_retriever,
        "query_embedding",
        return_value=[0.1, 0.2],
    ):
        documents = retriever._get_relevant_documents(
            "zephyr", run_manager=MagicMock()
        )

    assert [document.page_content for document in documents] == ["some content"]
    # The source card title must be the original file name with its extension.
    assert documents[0].metadata == {
        "document_id": "doc-1",
        "filename": "zephyr2",
        "file_extension": ".md",
        "title": "zephyr2.md",
        "score": 3.65,
    }


def test_history_handler_preserves_flag_on_followup_turn():
    rag_graph = RagGraph.__new__(RagGraph)
    rag_graph.rag_type = "RAG"
    rag_graph.chat_sequence_number = 2
    rag_graph.user_id = "user-1"
    rag_graph.chat_id = "chat-1"
    rag_graph.graph = MagicMock()
    rag_graph.config = {}
    rag_graph._load_domain_from_checkpoints = MagicMock(return_value=None)

    # On follow-up turns the value resumed from the checkpoint must not be lost:
    # the node reloads the history but must leave retrieve_from_uploaded_documents
    # untouched.
    with patch.object(
        agentic_rag, "load_messages_from_snapshot", return_value=[]
    ):
        state = rag_graph.history_handler_node(
            GraphState(retrieve_from_uploaded_documents=True)
        )

    assert state.retrieve_from_uploaded_documents is True


def _search(retriever, hits=(), index_exists=True):
    """Run the retriever against a mocked OpenSearch and embedding service, and
    return the documents with the mocks to inspect."""
    client = MagicMock()
    client.indices.exists.return_value = index_exists
    client.search.return_value = {"hits": {"hits": list(hits)}}

    with patch.object(
        uploaded_documents_retriever, "get_opensearch_client", return_value=client
    ) as mock_get_client, patch.object(
        uploaded_documents_retriever, "query_embedding", return_value=VECTOR
    ) as mock_embedding:
        documents = retriever.invoke("zephyr")

    return documents, client, mock_get_client, mock_embedding


def test_text_search_is_restricted_to_the_user_chat():
    _, client, mock_get_client, _ = _search(_build_retriever("TEXT"))

    mock_get_client.assert_called_once_with("http://localhost:9200")
    client.indices.exists.assert_called_once_with(index="test-index")
    client.search.assert_called_once_with(
        body={
            "query": {
                "bool": {
                    "must": [{"match": {"chunkText": {"query": "zephyr"}}}],
                    "filter": ISOLATION_FILTERS,
                }
            },
        },
        index="test-index",
    )


@pytest.mark.parametrize("retrieve_type", ["HYBRID", "KNN"])
def test_vectorial_search_is_restricted_to_the_user_chat(retrieve_type):
    # a hybrid query cannot hold a filter itself: the isolation is a post filter
    _, client, _, mock_embedding = _search(_build_retriever(retrieve_type))

    mock_embedding.assert_called_once_with(
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={"model": "an-embedding-model"},
        text="zephyr",
    )
    client.search.assert_called_once_with(
        body={
            "query": {
                "hybrid": {
                    "queries": [
                        {"knn": {"vector": {"vector": VECTOR, "k": 10}}},
                        {"match": {"chunkText": {"query": "zephyr", "boost": 0.5}}},
                    ]
                }
            },
            "post_filter": {"bool": {"filter": ISOLATION_FILTERS}},
        },
        index="test-index",
    )


def test_missing_index_returns_nothing():
    documents, client, _, _ = _search(_build_retriever(), index_exists=False)

    client.search.assert_not_called()
    assert documents == []


def test_hit_without_optional_fields_becomes_an_untitled_document():
    documents, _, _, _ = _search(
        _build_retriever(), hits=[{"_source": {"document_id": "doc-1"}}]
    )

    assert [document.page_content for document in documents] == [""]
    assert documents[0].metadata == {
        "document_id": "doc-1",
        "filename": None,
        "file_extension": None,
        "title": "",
        "score": 0,
    }


def _uploaded_documents_graph():
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_from_uploaded_documents = True
    graph.user_id = "user-1"
    graph.chat_id = "chat-1"
    graph.tenant_id = "tenant-1"
    graph.configuration = {
        "grpc_host_datasource": "datasource:50051",
        "grpc_host_embedding": "embedding:50052",
        "opensearch_host": "http://opensearch:9200",
        "tenant_id": "tenant-1",
        "retrieve_type": "HYBRID",
    }
    return graph


def test_node_searches_the_uploaded_documents_of_the_user_chat():
    graph = _uploaded_documents_graph()
    documents = [Document("chunk", metadata={"document_id": "doc-1"})]
    state = GraphState(current_query="zephyr", use_rag=True)

    with patch.object(
        agentic_rag,
        "get_embedding_model_configuration",
        return_value={"model": "an-embedding-model"},
    ) as mock_embedding_configuration, patch.object(
        agentic_rag, "OpenSearchUploadedDocumentsRetriever"
    ) as mock_retriever, patch.object(
        agentic_rag, "OpenSearchRetriever"
    ) as mock_searcher_retriever:
        mock_retriever.return_value.invoke.return_value = documents
        result = graph.opensearch_retriever_node(state)

    mock_embedding_configuration.assert_called_once_with(
        grpc_host="datasource:50051", tenant_id="tenant-1"
    )
    mock_retriever.assert_called_once_with(
        opensearch_host="http://opensearch:9200",
        grpc_host_embedding="embedding:50052",
        embedding_model_configuration={"model": "an-embedding-model"},
        uploaded_documents_index="tenant-1-uploaded-documents-index",
        retrieve_type="HYBRID",
        user_id="user-1",
        chat_id="chat-1",
        search_text="zephyr",
    )
    mock_retriever.return_value.invoke.assert_called_once_with("zephyr")
    mock_searcher_retriever.assert_not_called()
    assert result.context == documents


def test_node_refuses_uploaded_documents_without_a_user():
    graph = _uploaded_documents_graph()
    graph.user_id = None

    with patch.object(
        agentic_rag, "OpenSearchUploadedDocumentsRetriever"
    ) as mock_retriever, patch.object(
        agentic_rag, "OpenSearchRetriever"
    ) as mock_searcher_retriever, pytest.raises(HTTPException) as error:
        graph.opensearch_retriever_node(
            GraphState(current_query="zephyr", use_rag=True)
        )

    assert error.value.status_code == 401
    mock_retriever.assert_not_called()
    mock_searcher_retriever.assert_not_called()
