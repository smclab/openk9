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

from app.rag.agentic_rag import GraphState, RagGraph
from app.rag.retrievers import uploaded_documents_retriever
from app.rag.retrievers.uploaded_documents_retriever import (
    OpenSearchUploadedDocumentsRetriever,
)


def test_graph_state_persists_retrieve_from_uploaded_documents():
    assert GraphState().retrieve_from_uploaded_documents is None
    assert (
        GraphState(retrieve_from_uploaded_documents=True).retrieve_from_uploaded_documents
        is True
    )


def test_history_handler_writes_flag_into_state_on_first_turn():
    rag_graph = RagGraph.__new__(RagGraph)
    rag_graph.rag_type = "RAG"
    rag_graph.chat_sequence_number = 1
    rag_graph.retrieve_from_uploaded_documents = True
    rag_graph._load_domain_from_checkpoints = MagicMock(return_value=None)

    state = rag_graph.history_handler_node(GraphState())

    # First turn -> the flag is written into the state so it gets checkpointed.
    assert state.retrieve_from_uploaded_documents is True


def _build_retriever(retrieve_type="TEXT"):
    return OpenSearchUploadedDocumentsRetriever(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50053",
        embedding_model_configuration={},
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
        "documents_embedding",
        return_value=[{"vector": [0.1, 0.2]}],
    ):
        documents = retriever._get_relevant_documents(
            "zephyr", run_manager=MagicMock()
        )

    assert len(documents) == 1
    # The source card title must be the original file name with its extension.
    assert documents[0].metadata["title"] == "zephyr2.md"


def test_history_handler_preserves_flag_on_followup_turn():
    rag_graph = RagGraph.__new__(RagGraph)
    rag_graph.rag_type = "RAG"
    rag_graph.chat_sequence_number = 2
    rag_graph.user_id = "user-1"
    rag_graph.chat_id = "chat-1"
    rag_graph._load_messages_from_checkpoints = MagicMock(return_value=[])
    rag_graph._load_domain_from_checkpoints = MagicMock(return_value=None)

    # On follow-up turns the value resumed from the checkpoint must not be lost.
    state = rag_graph.history_handler_node(
        GraphState(retrieve_from_uploaded_documents=True)
    )

    assert state.retrieve_from_uploaded_documents is True


@pytest.mark.parametrize("retrieve_type", ["HYBRID", "KNN"])
def test_vectorial_search_is_restricted_to_the_user_chat(retrieve_type):
    # Each branch filters while scoring: a post filter applied after an
    # approximate knn loses the chat chunks when other chats hold closer ones.
    # The vector branch scores exactly because nmslib indices reject knn filters.
    isolation_filters = [
        {"term": {"user_id.keyword": "user-1"}},
        {"term": {"chat_id.keyword": "chat-1"}},
    ]
    client = MagicMock()
    client.indices.exists.return_value = True
    client.search.return_value = {"hits": {"hits": []}}

    with patch.object(
        uploaded_documents_retriever, "get_opensearch_client", return_value=client
    ), patch.object(
        uploaded_documents_retriever,
        "documents_embedding",
        return_value=[{"vector": [0.1, 0.2]}],
    ):
        _build_retriever(retrieve_type).invoke("zephyr")

    client.search.assert_called_once_with(
        body={
            "query": {
                "hybrid": {
                    "queries": [
                        {
                            "script_score": {
                                "query": {"bool": {"filter": isolation_filters}},
                                "script": {
                                    "source": "knn_score",
                                    "lang": "knn",
                                    "params": {
                                        "field": "vector",
                                        "query_value": [0.1, 0.2],
                                        "space_type": "l2",
                                    },
                                },
                            }
                        },
                        {
                            "bool": {
                                "must": [
                                    {
                                        "match": {
                                            "chunkText": {
                                                "query": "zephyr",
                                                "boost": 0.5,
                                            }
                                        }
                                    }
                                ],
                                "filter": isolation_filters,
                            }
                        },
                    ]
                }
            },
        },
        index="test-index",
    )
