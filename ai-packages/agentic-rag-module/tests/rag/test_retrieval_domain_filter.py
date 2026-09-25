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

"""The detected domain filters retrieval.

opensearch_retriever_node turns the domain of the state into a TEXT filter
token on the domain field, next to the query token; a turn the router sent
straight to the LLM (route_decision) retrieves nothing.
"""

from unittest.mock import patch

from langchain_core.documents import Document

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Che copertura ho per la grandine?"


def _graph():
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_from_uploaded_documents = False
    graph.user_id = None
    graph.tenant_id = None
    graph.chat_id = None
    graph.configuration = {"search_query": None, "retrieve_type": "HYBRID"}
    return graph


def _run_node(graph, state, retrieved=()):
    with patch("app.rag.agentic_rag.OpenSearchRetriever") as mock_retriever:
        mock_retriever.return_value.invoke.return_value = list(retrieved)
        result = graph.opensearch_retriever_node(state)

    return result, mock_retriever


def test_detected_domain_becomes_a_filter_token():
    state = GraphState(current_query=QUERY, use_rag=True, domain=["insurance"])

    _, mock_retriever = _run_node(_graph(), state)

    search_query = mock_retriever.call_args.kwargs["search_query"]
    domain_tokens = [token for token in search_query if token.keywordKey == "domain"]
    assert len(domain_tokens) == 1
    assert domain_tokens[0].tokenType == "TEXT"
    assert domain_tokens[0].values == ["insurance"]
    assert domain_tokens[0].filter is True
    assert search_query[0].values == [QUERY]


def test_no_domain_no_filter():
    state = GraphState(current_query=QUERY, use_rag=True)

    _, mock_retriever = _run_node(_graph(), state)

    search_query = mock_retriever.call_args.kwargs["search_query"]
    assert all(token.keywordKey != "domain" for token in search_query)


def test_retrieved_documents_become_the_context():
    documents = [Document("chunk", metadata={"document_id": "doc-1"})]
    state = GraphState(current_query=QUERY, use_rag=True)

    result, _ = _run_node(_graph(), state, documents)

    assert result.context == documents


def test_rag_decision_routes_to_the_retriever():
    state = GraphState(current_query=QUERY, use_rag=True)

    assert _graph().route_decision(state) == "opensearch_retriever"


def test_direct_decision_routes_to_the_llm():
    state = GraphState(current_query=QUERY, use_rag=False)

    assert _graph().route_decision(state) == "llm_response"


def test_direct_answer_retrieves_nothing():
    state = GraphState(current_query=QUERY, use_rag=False)

    result, mock_retriever = _run_node(_graph(), state)

    mock_retriever.assert_not_called()
    assert result.context == []
