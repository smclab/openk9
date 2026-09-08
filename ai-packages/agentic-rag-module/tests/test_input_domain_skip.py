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

"""Domain detection is skipped when no domain is configured.

The domain filter is opt-in: it needs documents labelled in
``domain-documents-index``. With that index empty or absent, ``input_domain_node``
used to embed the query and then ask the LLM to pick a domain out of an empty
set, paying an embedding round-trip and a model call per turn for an answer the
parser could never use. These tests pin the early exit and the fact that the
configured domains are read once and reused by the below-threshold fallback.
"""

from unittest.mock import MagicMock, patch

from langchain_core.documents import Document

from app.rag.agentic_rag import Domain, GraphState, RagGraph


def _graph():
    """Bare RagGraph carrying only what input_domain_node reaches for."""
    graph = RagGraph.__new__(RagGraph)
    graph.opensearch_host = "http://localhost:9200"
    graph.utility_llm = MagicMock()
    graph.configuration = {
        "grpc_host_datasource": "localhost:50051",
        "grpc_host_embedding": "localhost:50052",
        "tenant_id": "tenant-1",
        "domain_threshold": 0.7,
    }
    return graph


def _domain_document(domain, score):
    return Document("chunk", metadata={"domain": domain, "score": score})


def test_no_domain_configured_skips_retrieval_and_llm():
    graph = _graph()
    state = GraphState(current_query="Che copertura ho?")

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ), patch("app.rag.agentic_rag.OpenSearchDomainDocumentsRetriever") as mock_class:
        retriever = mock_class.return_value
        retriever.get_domains.return_value = []

        result = graph.input_domain_node(state)

    retriever.get_domains.assert_called_once_with()
    retriever.invoke.assert_not_called()
    graph.utility_llm.invoke.assert_not_called()
    assert result.domain is None


def test_no_domain_configured_clears_the_intent_marker():
    # analyze_and_rewrite_query_node parks NEW_QUESTION in the same field, and
    # opensearch_retriever_node reads it back as a domain filter: leaving it in
    # place would filter retrieval on a domain that does not exist.
    graph = _graph()
    state = GraphState(current_query="Che copertura ho?", domain=["NEW_QUESTION"])

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ), patch("app.rag.agentic_rag.OpenSearchDomainDocumentsRetriever") as mock_class:
        mock_class.return_value.get_domains.return_value = []

        result = graph.input_domain_node(state)

    assert result.domain is None


def test_domain_configured_runs_detection():
    graph = _graph()
    state = GraphState(current_query="Che copertura ho?", domain=["NEW_QUESTION"])

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ), patch("app.rag.agentic_rag.OpenSearchDomainDocumentsRetriever") as mock_class:
        retriever = mock_class.return_value
        retriever.get_domains.return_value = ["insurance"]
        retriever.invoke.return_value = [_domain_document("insurance", 0.9)]

        result = graph.input_domain_node(state)

    retriever.invoke.assert_called_once_with("Che copertura ho?")
    graph.utility_llm.invoke.assert_not_called()
    assert result.domain == ["insurance"]


def test_below_threshold_fallback_reuses_the_configured_domains():
    graph = _graph()
    state = GraphState(current_query="Che copertura ho?")

    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ), patch(
        "app.rag.agentic_rag.OpenSearchDomainDocumentsRetriever"
    ) as mock_class, patch.object(
        RagGraph, "_llm_input_domain", return_value=Domain(domain=["insurance"])
    ) as mock_llm_domain:
        retriever = mock_class.return_value
        retriever.get_domains.return_value = ["insurance"]
        retriever.invoke.return_value = [_domain_document("insurance", 0.1)]

        result = graph.input_domain_node(state)

    # One read of the configured domains, shared between the early-exit check
    # and the fallback candidates.
    retriever.get_domains.assert_called_once_with()
    mock_llm_domain.assert_called_once_with("Che copertura ho?", {"insurance"})
    assert result.domain == ["insurance"]
