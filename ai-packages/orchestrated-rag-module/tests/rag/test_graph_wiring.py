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

"""How RagGraph wires its nodes and where it keeps the conversation.

The chat graph starts from the input guardrail and reaches the answer through
history, query analysis, optional domain detection, routing and retrieval.
The real-time evaluation graph drops guardrail and domain detection and adds
the judge nodes. A saved chat checkpoints on OpenSearch, in an index of its
own tenant and user; an anonymous one only in memory.
"""

from unittest.mock import MagicMock, patch

from langgraph.checkpoint.memory import InMemorySaver

from app.rag import agentic_rag
from app.rag.agentic_rag import RagGraph

CHAT_NODES = {
    "input_guardrail",
    "guardrail_violation_response",
    "history_handler",
    "analyze_and_rewrite_query",
    "input_domain",
    "rag_router",
    "opensearch_retriever",
    "llm_response",
    "history_saver",
}

CHAT_EDGES = {
    ("__start__", "input_guardrail"),
    ("input_guardrail", "guardrail_violation_response"),
    ("input_guardrail", "history_handler"),
    ("guardrail_violation_response", "__end__"),
    ("history_handler", "analyze_and_rewrite_query"),
    ("analyze_and_rewrite_query", "input_domain"),
    ("analyze_and_rewrite_query", "rag_router"),
    ("input_domain", "rag_router"),
    ("rag_router", "opensearch_retriever"),
    ("rag_router", "llm_response"),
    ("opensearch_retriever", "llm_response"),
    ("llm_response", "history_saver"),
    ("history_saver", "__end__"),
}

EVALUATION_NODES = {
    "history_handler",
    "analyze_and_rewrite_query",
    "rag_router",
    "rag_router_evaluation",
    "opensearch_retriever",
    "opensearch_retriever_evaluation",
    "llm_response",
    "history_saver",
    "response_evaluation",
}

EVALUATION_EDGES = {
    ("__start__", "history_handler"),
    ("history_handler", "analyze_and_rewrite_query"),
    ("analyze_and_rewrite_query", "rag_router"),
    ("rag_router", "opensearch_retriever"),
    ("rag_router", "llm_response"),
    ("opensearch_retriever", "opensearch_retriever_evaluation"),
    ("opensearch_retriever_evaluation", "llm_response"),
    ("llm_response", "rag_router_evaluation"),
    ("rag_router_evaluation", "history_saver"),
    ("history_saver", "response_evaluation"),
    ("response_evaluation", "__end__"),
}


def _configuration(**overrides):
    configuration = {
        "rag_type": "CHAT_RAG",
        "tenant_id": "tenant-1",
        "user_id": None,
        "chat_id": None,
        "chat_sequence_number": 1,
        "opensearch_host": "http://localhost:9200",
        "guardrails_configuration": {
            "input_guardrail": {},
            "output_guardrail": {},
            "guardrail_categories": [],
        },
    }
    configuration.update(overrides)
    return configuration


def _build(configuration):
    """Build a RagGraph with the OpenSearch client and saver replaced; returns
    the graph and the spy standing in for OpenSearchSaver."""
    saver = MagicMock(side_effect=lambda **_kwargs: InMemorySaver())

    with patch.object(agentic_rag, "get_opensearch_client"), patch.object(
        agentic_rag, "OpenSearchSaver", saver
    ):
        graph = RagGraph(MagicMock(), configuration)

    return graph, saver


def _wiring(graph):
    drawable = graph.graph.get_graph()
    nodes = set(drawable.nodes) - {"__start__", "__end__"}
    edges = {(edge.source, edge.target) for edge in drawable.edges}
    return nodes, edges


def test_chat_graph_wiring():
    graph, _ = _build(_configuration())

    nodes, edges = _wiring(graph)

    assert nodes == CHAT_NODES
    assert edges == CHAT_EDGES


def test_real_time_evaluation_graph_wiring():
    graph, _ = _build(_configuration(enable_real_time_evaluation=True))

    nodes, edges = _wiring(graph)

    assert nodes == EVALUATION_NODES
    assert edges == EVALUATION_EDGES


def test_saved_chat_is_checkpointed_on_its_tenant_and_user_index():
    graph, saver = _build(_configuration(user_id="user-1", chat_id="chat-1"))

    saver.assert_called_once()
    assert saver.call_args.kwargs["checkpoint_index_name"] == "tenant-1-user-1"
    assert saver.call_args.kwargs["writes_index_name"] == "tenant-1-user-1-writes"
    assert graph.config == {"configurable": {"thread_id": "chat-1"}}


def test_anonymous_chat_is_checkpointed_in_memory():
    graph, saver = _build(_configuration())

    saver.assert_not_called()
    assert isinstance(graph.checkpointer, InMemorySaver)
    assert graph.config == {"configurable": {"thread_id": "not_logged_user"}}


def test_user_without_chat_is_checkpointed_in_memory():
    graph, saver = _build(_configuration(user_id="user-1"))

    saver.assert_not_called()
    assert isinstance(graph.checkpointer, InMemorySaver)


def test_guardrail_categories_are_numbered_for_the_prompt():
    configuration = _configuration()
    configuration["guardrails_configuration"]["guardrail_categories"] = [
        {"name": "VIOLENCE", "description": "violent content"},
        {"name": "HATE", "description": "hate speech"},
    ]

    graph, _ = _build(configuration)

    assert graph.guardrail_categories == (
        "1. VIOLENCE - violent content\n2. HATE - hate speech"
    )


def test_utility_llm_defaults_to_the_chat_llm():
    llm = MagicMock()

    with patch.object(agentic_rag, "get_opensearch_client"):
        graph = RagGraph(llm, _configuration())

    assert graph.utility_llm is llm


def test_guardrail_category_without_name_or_description_renders_empty():
    configuration = _configuration()
    configuration["guardrails_configuration"]["guardrail_categories"] = [
        {"name": "VIOLENCE"},
        {"description": "hate speech"},
    ]

    graph, _ = _build(configuration)

    assert graph.guardrail_categories == "1. VIOLENCE - \n2.  - hate speech"


def test_tenant_configuration_is_read_into_the_graph():
    llm = MagicMock()
    utility_llm = MagicMock()
    configuration = _configuration(
        chat_sequence_number=3,
        chat_history=[{"question": "q", "answer": "a"}],
        reformulate=True,
        retrieve_from_uploaded_documents=True,
        answer_only_with_context=False,
    )
    configuration["guardrails_configuration"] = {
        "input_guardrail": {"input_guardrail_provider": "MODEL_ARMOR"},
        "output_guardrail": {
            "output_guardrail_type": 3,
            "output_guardrail_chunk_interval": 5,
            "output_guardrail_provider": "LLM",
            "scope_gate_prefix_chars": 600,
            "scope_gate_domain_description": "polizze auto",
            "scope_gate_redirect_message": "Solo polizze auto.",
        },
    }

    with patch.object(agentic_rag, "get_opensearch_client") as client:
        graph = RagGraph(llm, configuration, utility_llm=utility_llm)

    client.assert_called_once_with("http://localhost:9200")
    assert graph.open_search_client is client.return_value
    assert graph.llm is llm
    assert graph.utility_llm is utility_llm
    assert graph.rag_type == "CHAT_RAG"
    assert graph.opensearch_host == "http://localhost:9200"
    assert graph.chat_sequence_number == 3
    assert graph.chat_history == [{"question": "q", "answer": "a"}]
    assert graph.reformulate is True
    assert graph.retrieve_from_uploaded_documents is True
    assert graph.answer_only_with_context is False
    assert graph.input_guardrail_provider == "MODEL_ARMOR"
    assert graph.output_guardrail_type == 3
    assert graph.output_guardrail_chunk_interval == 5
    assert graph.output_guardrail_provider == "LLM"
    assert graph.scope_gate_prefix_chars == 600
    assert graph.scope_gate_domain_description == "polizze auto"
    assert graph.scope_gate_redirect_message == "Solo polizze auto."


def test_defaults_of_the_optional_settings():
    graph, _ = _build(_configuration())

    assert graph.answer_only_with_context is True
    assert graph.scope_gate_prefix_chars == 250
    assert graph.scope_gate_domain_description == ""
    assert graph.scope_gate_redirect_message == (
        "Posso aiutarti solo su temi di questo dominio."
    )
