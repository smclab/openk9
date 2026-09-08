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


from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Quali sono i massimali della polizza?"
DOCUMENT = Document(
    page_content="Il massimale è di 100.000 euro.",
    metadata={"document_id": "chunk-1"},
)


def _graph(model_type, structured_response):
    """RagGraph stub whose LLMs record how the structured chain was asked for,
    so the method reaching `with_structured_output` can be asserted on."""
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.chat_sequence_number = 2
    graph.reformulate = False
    graph.configuration = {
        "model_type": model_type,
        "rag_tool_description": "TOOL DESCRIPTION",
        "analyze_query_prompt_template": "",
    }
    graph.llm = MagicMock()
    graph.llm.with_structured_output.return_value = (
        lambda _prompt_value: structured_response
    )
    graph.utility_llm = MagicMock()
    graph.utility_llm.with_structured_output.return_value = (
        lambda _prompt_value: structured_response
    )
    return graph


def _method_of(with_structured_output):
    return with_structured_output.call_args.kwargs["method"]


def _analysis_state():
    return GraphState(
        current_query=QUERY,
        messages=[
            HumanMessage(content="Che polizza è?"),
            AIMessage(content="È una polizza auto."),
        ],
    )


def _routing_decision(value):
    return SimpleNamespace(response=SimpleNamespace(value=value))


def _relevance_verdict():
    return SimpleNamespace(
        chunk_id="chunk-1",
        judgment=SimpleNamespace(value="RELEVANT"),
        explanation="Il testo riporta il massimale richiesto.",
        vote=9,
    )


def test_query_analysis_follows_the_tenant_provider():
    ollama = _graph("ollama", _routing_decision("NEW_QUESTION"))
    openai = _graph("openai", _routing_decision("NEW_QUESTION"))

    ollama.analyze_and_rewrite_query_node(_analysis_state())
    openai.analyze_and_rewrite_query_node(_analysis_state())

    assert _method_of(ollama.utility_llm.with_structured_output) == "json_schema"
    assert _method_of(openai.utility_llm.with_structured_output) == "function_calling"


def test_rag_router_follows_the_tenant_provider():
    ollama = _graph("ollama", _routing_decision("RAG"))
    openai = _graph("openai", _routing_decision("RAG"))

    ollama.rag_router_node(GraphState(current_query=QUERY, messages=[]))
    openai.rag_router_node(GraphState(current_query=QUERY, messages=[]))

    assert _method_of(ollama.llm.with_structured_output) == "json_schema"
    assert _method_of(openai.llm.with_structured_output) == "function_calling"


def test_retriever_evaluation_follows_the_tenant_provider():
    ollama = _graph("ollama", _relevance_verdict())
    openai = _graph("openai", _relevance_verdict())
    state = GraphState(current_query=QUERY, context=[DOCUMENT])

    ollama.opensearch_retriever_evaluation_node(state.model_copy(deep=True))
    openai.opensearch_retriever_evaluation_node(state.model_copy(deep=True))

    assert _method_of(ollama.llm.with_structured_output) == "json_schema"
    assert _method_of(openai.llm.with_structured_output) == "function_calling"


def test_chunks_evaluation_follows_the_tenant_provider():
    verdicts = SimpleNamespace(evaluations=[_relevance_verdict()])
    ollama = _graph("ollama", verdicts)
    openai = _graph("openai", verdicts)
    state = GraphState(current_query=QUERY, context=[DOCUMENT])

    ollama.opensearch_retriever_chunks_evaluation_node(state.model_copy(deep=True))
    openai.opensearch_retriever_chunks_evaluation_node(state.model_copy(deep=True))

    assert _method_of(ollama.llm.with_structured_output) == "json_schema"
    assert _method_of(openai.llm.with_structured_output) == "function_calling"


def test_per_chunk_evaluation_follows_the_tenant_provider():
    ollama = _graph("ollama", _relevance_verdict())
    openai = _graph("openai", _relevance_verdict())
    state = GraphState(current_query=QUERY, context=[DOCUMENT])

    ollama.opensearch_retriever_chunks_evaluation_for_node(state.model_copy(deep=True))
    openai.opensearch_retriever_chunks_evaluation_for_node(state.model_copy(deep=True))

    assert _method_of(ollama.llm.with_structured_output) == "json_schema"
    assert _method_of(openai.llm.with_structured_output) == "function_calling"


def test_response_evaluation_follows_the_tenant_provider():
    clarity = SimpleNamespace(
        judgment=SimpleNamespace(value="CLEAR"),
        explanation="La risposta indica il massimale.",
        vote=9,
    )
    ollama = _graph("ollama", clarity)
    openai = _graph("openai", clarity)
    state = GraphState(current_query=QUERY, response="Il massimale è 100.000 euro.")

    ollama.response_evaluation_node(state.model_copy(deep=True))
    openai.response_evaluation_node(state.model_copy(deep=True))

    assert _method_of(ollama.llm.with_structured_output) == "json_schema"
    assert _method_of(openai.llm.with_structured_output) == "function_calling"


def test_the_method_follows_a_provider_change_without_a_restart():
    # The graph is built once per request but the tenant configuration is read
    # at each use: switching the LLM provider must take effect on the next
    # request, not on the next deploy.
    graph = _graph("openai", _routing_decision("RAG"))

    graph.rag_router_node(GraphState(current_query=QUERY, messages=[]))
    assert _method_of(graph.llm.with_structured_output) == "function_calling"

    graph.configuration["model_type"] = "ollama"
    graph.rag_router_node(GraphState(current_query=QUERY, messages=[]))
    assert _method_of(graph.llm.with_structured_output) == "json_schema"
