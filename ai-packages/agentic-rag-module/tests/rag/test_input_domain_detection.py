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

"""Domain detection of the user query.

input_domain_node keeps the domains of the documents scoring above
domain_threshold in the domain index; when none clears it, _llm_input_domain
asks the utility LLM to pick one among the configured domains. The detected
domain is what the retriever later filters on, and what intent detection
reads to skip a second detection.
"""

from unittest.mock import patch

from langchain_core.documents import Document
from langchain_core.language_models.fake_chat_models import FakeListChatModel
from langchain_core.messages import AIMessage
from langchain_core.runnables import RunnableLambda

from app.rag.agentic_rag import Domain, GraphState, RagGraph

QUERY = "Che copertura ho per la grandine?"


def _graph(utility_llm=None):
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "AGENTIC"
    graph.opensearch_host = "http://localhost:9200"
    graph.utility_llm = utility_llm
    graph.configuration = {
        "grpc_host_datasource": "localhost:50051",
        "grpc_host_embedding": "localhost:50052",
        "tenant_id": "tenant-1",
        "domain_threshold": 0.7,
    }
    return graph


def _run_node(graph, retrieved_docs, configured_domains):
    with patch(
        "app.rag.agentic_rag.get_embedding_model_configuration", return_value={}
    ), patch("app.rag.agentic_rag.OpenSearchDomainDocumentsRetriever") as mock_class:
        retriever = mock_class.return_value
        retriever.get_domains.return_value = configured_domains
        retriever.invoke.return_value = retrieved_docs

        return graph.input_domain_node(GraphState(current_query=QUERY))


def _domain_document(domain, score):
    return Document("chunk", metadata={"domain": domain, "score": score})


def test_only_domains_above_threshold_are_kept():
    graph = _graph()

    state = _run_node(
        graph,
        [
            _domain_document("insurance", 0.9),
            _domain_document("insurance", 0.8),
            _domain_document("claims", 0.75),
            _domain_document("hr", 0.2),
        ],
        configured_domains=["insurance", "claims", "hr"],
    )

    assert sorted(state.domain) == ["claims", "insurance"]


def test_score_equal_to_threshold_counts_as_a_match():
    graph = _graph()

    state = _run_node(
        graph, [_domain_document("insurance", 0.7)], configured_domains=["insurance"]
    )

    assert state.domain == ["insurance"]


def test_detected_domain_lets_intent_detection_skip_a_new_detection():
    graph = _graph()

    state = _run_node(
        graph, [_domain_document("insurance", 0.9)], configured_domains=["insurance"]
    )

    assert graph.intent_detection_decision(state) == "rag_router"


def test_llm_fallback_domain_is_stored_in_the_state():
    graph = _graph()

    with patch.object(
        RagGraph, "_llm_input_domain", return_value=Domain(domain=["claims"])
    ):
        state = _run_node(
            graph, [_domain_document("insurance", 0.1)], configured_domains=["claims"]
        )

    assert state.domain == ["claims"]


def test_llm_fallback_without_a_match_leaves_the_domain_empty():
    graph = _graph()

    with patch.object(RagGraph, "_llm_input_domain", return_value=Domain(domain="")):
        state = _run_node(
            graph, [_domain_document("insurance", 0.1)], configured_domains=["claims"]
        )

    assert state.domain is None


def test_llm_input_domain_parses_the_model_output():
    graph = _graph(FakeListChatModel(responses=['{"domain": ["insurance"]}']))

    result = graph._llm_input_domain(QUERY, {"insurance", "claims"})

    assert result.domain == ["insurance"]


def test_llm_input_domain_wraps_a_single_domain_in_a_list():
    graph = _graph(FakeListChatModel(responses=['{"domain": "insurance"}']))

    result = graph._llm_input_domain(QUERY, {"insurance"})

    assert result.domain == ["insurance"]


def test_llm_input_domain_maps_an_empty_domain_to_none():
    graph = _graph(FakeListChatModel(responses=['{"domain": ""}']))

    result = graph._llm_input_domain(QUERY, {"insurance"})

    assert result.domain is None


def test_llm_input_domain_reads_list_shaped_content():
    # Some providers return the content as a list of typed parts.
    graph = _graph(
        RunnableLambda(
            lambda _prompt: AIMessage(
                content=[{"type": "text", "text": '{"domain": ["claims"]}'}]
            )
        )
    )

    result = graph._llm_input_domain(QUERY, {"claims"})

    assert result.domain == ["claims"]


def test_llm_input_domain_offers_the_candidates_to_the_model():
    prompts = []

    def llm(prompt_value):
        prompts.append(prompt_value.to_string())
        return AIMessage(content='{"domain": ["claims"]}')

    graph = _graph(RunnableLambda(llm))

    graph._llm_input_domain(QUERY, {"claims"})

    assert "{'claims'}" in prompts[0]
    assert QUERY in prompts[0]
