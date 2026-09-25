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

"""opensearch_retriever_node hands the bucket configuration to the searcher.

Outside the uploaded-documents branch the node builds an OpenSearchRetriever
from the bucket configuration and searches the current query with it: the
configured token list when there is one, a single token of the bucket retrieve
type otherwise.
"""

from unittest.mock import patch

from langchain_core.documents import Document

from app.models import models
from app.rag import agentic_rag
from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Che copertura ho per la grandine?"

# every entry gets a distinct value so a swapped or dropped one shows up
CONFIGURATION = {
    "search_query": None,
    "datasource_ids": None,
    "rerank": True,
    "chunk_window": 2,
    "range_values": [0, 5],
    "after_key": "after-key",
    "suggest_keyword": "suggest-keyword",
    "suggestion_category_id": 7,
    "tenant_id": "tenant-1",
    "jwt": "a-jwt",
    "extra": {"extra-key": "extra-value"},
    "sort": [{"date": "desc"}],
    "sort_after_key": "sort-after-key",
    "language": "it_IT",
    "context_window": 100_000,
    "metadata": {"web": {"title": "webTitle"}},
    "retrieve_type": "HYBRID",
    "score_threshold": 0.3,
    "opensearch_host": "http://opensearch:9200",
    "grpc_host_datasource": "searcher:50051",
}


def _graph(**configuration):
    # a logged user without the uploaded-documents flag still searches the
    # bucket, not the uploaded documents
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_from_uploaded_documents = False
    graph.user_id = "user-1"
    graph.chat_id = "chat-1"
    graph.tenant_id = "tenant-1"
    graph.configuration = {**CONFIGURATION, **configuration}
    return graph


def _run_node(graph, state, retrieved=()):
    with (
        patch.object(
            agentic_rag, "OpenSearchUploadedDocumentsRetriever"
        ) as mock_uploaded_retriever,
        patch.object(agentic_rag, "OpenSearchRetriever") as mock_retriever,
    ):
        mock_retriever.return_value.invoke.return_value = list(retrieved)
        result = graph.opensearch_retriever_node(state)

    mock_uploaded_retriever.assert_not_called()
    return result, mock_retriever


def _retriever_kwargs(mock_retriever):
    mock_retriever.assert_called_once()
    kwargs = dict(mock_retriever.call_args.kwargs)
    # not part of the contract under test
    kwargs.pop("reranker_api_url", None)
    return kwargs


def test_searcher_retriever_receives_the_bucket_configuration():
    documents = [Document("chunk", metadata={"document_id": "doc-1"})]
    state = GraphState(current_query=QUERY, use_rag=True)

    result, mock_retriever = _run_node(_graph(), state, documents)

    assert _retriever_kwargs(mock_retriever) == {
        "search_query": [
            models.SearchToken(
                tokenType="HYBRID",
                keywordKey="",
                values=[QUERY],
                filter=True,
                entityType="",
                entityName="",
                extra={},
            )
        ],
        "search_text": QUERY,
        "rerank": True,
        "chunk_window": 2,
        "range_values": [0, 5],
        "after_key": "after-key",
        "suggest_keyword": "suggest-keyword",
        "suggestion_category_id": 7,
        "tenant_id": "tenant-1",
        "jwt": "a-jwt",
        "extra": {"extra-key": "extra-value"},
        "sort": [{"date": "desc"}],
        "sort_after_key": "sort-after-key",
        "language": "it_IT",
        "context_window": 100_000,
        "metadata": {"web": {"title": "webTitle"}},
        "retrieve_type": "HYBRID",
        "score_threshold": 0.3,
        "opensearch_host": "http://opensearch:9200",
        "grpc_host": "searcher:50051",
    }
    mock_retriever.return_value.invoke.assert_called_once_with(QUERY)
    assert result.context == documents


def test_configured_search_query_is_kept_ahead_of_the_filters():
    configured = models.SearchToken(
        tokenType="TEXT", keywordKey="title", values=["grandine"], filter=False
    )
    state = GraphState(current_query=QUERY, use_rag=True, domain=["insurance"])

    _, mock_retriever = _run_node(
        _graph(search_query=[configured], datasource_ids=[3]), state
    )

    search_query = _retriever_kwargs(mock_retriever)["search_query"]
    assert [
        (token.tokenType, token.keywordKey, token.values) for token in search_query
    ] == [
        ("TEXT", "title", ["grandine"]),
        ("TEXT", "domain", ["insurance"]),
        ("DATASOURCE", "", ["3"]),
    ]
