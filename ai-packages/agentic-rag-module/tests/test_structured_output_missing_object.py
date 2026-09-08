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


import logging
from unittest.mock import MagicMock

from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Quali sono i massimali della polizza?"
DOCUMENT = Document(
    page_content="Il massimale è di 100.000 euro.",
    metadata={"document_id": "chunk-1"},
)


def _graph():
    """RagGraph stub whose structured chains answer in prose: LangChain hands
    back `None` without filling `parsing_error`, which is what an Ollama model
    that ignores the tool call actually produces."""
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.chat_sequence_number = 2
    graph.reformulate = True
    graph.configuration = {
        "model_type": "ollama",
        "rag_tool_description": "TOOL DESCRIPTION",
        "analyze_query_prompt_template": "",
    }
    graph.llm = MagicMock()
    graph.llm.with_structured_output.return_value = lambda _prompt_value: None
    graph.utility_llm = MagicMock()
    graph.utility_llm.with_structured_output.return_value = lambda _prompt_value: None
    graph._rewrite_query = MagicMock(return_value="REWRITTEN QUERY")
    return graph


def _errors(caplog):
    return [
        record.getMessage()
        for record in caplog.records
        if record.levelno == logging.ERROR
    ]


def test_query_analysis_reports_the_failure_and_keeps_the_query(caplog):
    graph = _graph()
    state = GraphState(
        current_query=QUERY,
        messages=[
            HumanMessage(content="Che polizza è?"),
            AIMessage(content="È una polizza auto."),
        ],
    )

    with caplog.at_level(logging.ERROR):
        state = graph.analyze_and_rewrite_query_node(state)

    assert state.domain == ["NEW_QUESTION"]
    assert state.current_query == QUERY
    graph._rewrite_query.assert_not_called()
    assert "schema=AnalyzeQuestion" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]


def test_rag_router_reports_the_failure_and_retrieves(caplog):
    graph = _graph()

    with caplog.at_level(logging.ERROR):
        state = graph.rag_router_node(GraphState(current_query=QUERY, messages=[]))

    # Answering without context is the worse failure for a tenant that has a
    # corpus, so an unreadable routing decision retrieves.
    assert state.use_rag is True
    assert "schema=RouterResponse" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]


def test_retriever_evaluation_reports_the_failure_and_leaves_no_verdict(caplog):
    graph = _graph()

    with caplog.at_level(logging.ERROR):
        state = graph.opensearch_retriever_evaluation_node(
            GraphState(current_query=QUERY, context=[DOCUMENT])
        )

    assert state.retriever_evaluation is None
    assert "schema=RetrieverEvaluationResponse" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]


def test_chunks_evaluation_reports_the_failure_and_leaves_no_verdict(caplog):
    graph = _graph()

    with caplog.at_level(logging.ERROR):
        state = graph.opensearch_retriever_chunks_evaluation_node(
            GraphState(current_query=QUERY, context=[DOCUMENT])
        )

    assert state.retriever_chunks_evaluation is None
    assert "schema=RetrieverEvaluationResponseList" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]


def test_per_chunk_evaluation_reports_the_failure_and_skips_the_chunk(caplog):
    graph = _graph()

    with caplog.at_level(logging.ERROR):
        state = graph.opensearch_retriever_chunks_evaluation_for_node(
            GraphState(current_query=QUERY, context=[DOCUMENT])
        )

    assert state.retriever_chunks_evaluation == []
    assert "chunk-1" in _errors(caplog)[0]
    assert "schema=RetrieverEvaluationResponse" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]


def test_response_evaluation_reports_the_failure_and_leaves_no_verdict(caplog):
    graph = _graph()

    with caplog.at_level(logging.ERROR):
        state = graph.response_evaluation_node(
            GraphState(current_query=QUERY, response="Il massimale è 100.000 euro.")
        )

    assert state.response_evaluation is None
    assert "schema=ClassificationResponse" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]
