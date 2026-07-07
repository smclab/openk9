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

from app.models import models
from app.rag.agentic_rag import GraphState, RagGraph


def _retriever_graph(datasource_ids):
    """Build a RagGraph stub exercising opensearch_retriever_node in isolation
    on the normal (non-uploaded-documents) retrieval branch, with the given
    datasource_ids in its configuration."""
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_from_uploaded_documents = False
    graph.user_id = None
    graph.tenant_id = None
    graph.chat_id = None
    graph.configuration = {
        "search_query": None,
        "datasource_ids": datasource_ids,
        "retrieve_type": "TEXT",
    }
    return graph


def _run_node(graph):
    """Run the retriever node with OpenSearchRetriever mocked, and return the
    search_query list that was handed to the retriever."""
    state = GraphState(current_query="ciao", use_rag=True)

    with patch("app.rag.agentic_rag.OpenSearchRetriever") as mock_retriever:
        mock_retriever.return_value.invoke.return_value = []
        graph.opensearch_retriever_node(state)

    _, kwargs = mock_retriever.call_args
    return kwargs["search_query"]


def test_datasource_filter_token_added_when_ids_present():
    # selected datasource ids reach the retriever as a DATASOURCE filter token
    graph = _retriever_graph([1, 2])

    search_query = _run_node(graph)

    # a single DATASOURCE token is appended, with the ids stringified
    datasource_tokens = [t for t in search_query if t.tokenType == "DATASOURCE"]
    assert len(datasource_tokens) == 1
    assert datasource_tokens[0].values == ["1", "2"]


def test_no_datasource_filter_when_ids_absent():
    # no selection -> no DATASOURCE token -> current behavior (all sources)
    graph = _retriever_graph(None)

    search_query = _run_node(graph)

    assert all(t.tokenType != "DATASOURCE" for t in search_query)


def test_no_datasource_filter_when_ids_empty():
    # an empty list is treated like no selection (avoids a match-nothing filter)
    graph = _retriever_graph([])

    search_query = _run_node(graph)

    assert all(t.tokenType != "DATASOURCE" for t in search_query)
