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


"""Tests for the request lifecycle records of stream().

Every way out of the generator must report how the request ended, so that
"the chatbot did not answer me" can be told apart from a guardrail block
without lowering the level and without reading the answer.
"""

import json
import logging
from types import SimpleNamespace
from unittest.mock import MagicMock

from app.rag.agentic_rag import RagGraph

QUERY = "Quali corsi avete in catalogo?"
ANSWER = "Il catalogo comprende corsi di formazione Liferay."
REDIRECT = "Posso aiutarti solo su temi di questo dominio."


def _chunk(text):
    return SimpleNamespace(content=text)


def _graph(chunk_texts=(ANSWER,), state_values=None, stream_exception=None):
    """RagGraph stub routed through the plain branch of stream()."""
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = "litwick"
    graph.user_id = None
    graph.chat_id = "abc123"
    graph.rag_type = "SIMPLE_GENERATE"
    graph.output_guardrail = {}
    graph.output_guardrail_type = 0
    graph.config = {}
    graph.chat_sequence_number = 1
    graph._resolve_target_language = MagicMock(return_value="Italian")
    graph.graph = MagicMock()

    if stream_exception is not None:
        graph.graph.stream.side_effect = stream_exception
    else:
        graph.graph.stream.return_value = [
            (_chunk(text), {"langgraph_node": "llm_response"}) for text in chunk_texts
        ]

    graph.graph.get_state.return_value = SimpleNamespace(
        values=state_values if state_values is not None else {}
    )
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


def _messages(caplog, level):
    return [record.getMessage() for record in caplog.records if record.levelno == level]


def _record(caplog, marker):
    return [m for m in _messages(caplog, logging.INFO) if marker in m][0]


def test_request_start_is_reported_without_the_query(caplog):
    graph = _graph()

    with caplog.at_level(logging.INFO):
        _events(graph)

    start = _record(caplog, "[request] start")
    assert "rag_type=SIMPLE_GENERATE" in start
    assert "tenant_id=litwick" in start
    assert "chat_id=abc123" in start
    assert f"query_chars={len(QUERY)}" in start
    assert QUERY not in start


def test_completed_request_reports_its_duration(caplog):
    graph = _graph()

    with caplog.at_level(logging.INFO):
        events = _events(graph)

    assert events[-1] == {"chunk": "", "type": "END"}
    end = _record(caplog, "[request] end")
    assert "outcome=COMPLETED" in end
    assert "duration_ms=" in end
    assert "chain=agentic_rag" in end


def test_input_block_is_reported_as_blocked_input(caplog):
    graph = _graph(
        state_values={"guardrail_check": True, "response": "Guardrail violation"}
    )

    with caplog.at_level(logging.INFO):
        events = _events(graph)

    assert any(event["type"] == "GUARDRAIL" for event in events)
    assert "outcome=BLOCKED_INPUT" in _record(caplog, "[request] end")


def test_scope_gate_block_is_reported_as_off_scope(caplog):
    graph = _graph()
    graph.output_guardrail = {"enable_output_guardrail": True}
    graph.output_guardrail_type = 3
    graph.scope_gate_prefix_chars = 5
    graph.scope_gate_redirect_message = REDIRECT
    graph._llm_scope_gate = MagicMock(return_value="OFF_SCOPE")
    graph._get_retrieved_context_text = MagicMock(return_value="Contesto.")

    with caplog.at_level(logging.INFO):
        events = _events(graph)

    assert events[-1] == {"chunk": REDIRECT, "type": "CANCEL"}
    assert "outcome=OFF_SCOPE" in _record(caplog, "[request] end")


def test_output_block_is_reported_as_blocked_output(caplog):
    graph = _graph()
    graph.output_guardrail = {"enable_output_guardrail": True}
    graph.output_guardrail_type = 1
    graph.output_guardrail_chunk_interval = 1
    graph._llm_output_guardrail = MagicMock(return_value="VIOLENCE/WEAPONS")

    with caplog.at_level(logging.INFO):
        events = _events(graph)

    assert events[-1] == {"chunk": "Inappropriate content", "type": "CANCEL"}
    assert "outcome=BLOCKED_OUTPUT" in _record(caplog, "[request] end")


def test_failure_is_reported_as_error(caplog):
    graph = _graph(stream_exception=RuntimeError("opensearch unavailable"))

    with caplog.at_level(logging.INFO):
        _events(graph)

    assert "outcome=ERROR" in _record(caplog, "[request] end")


def test_provider_content_policy_is_reported_as_a_block(caplog):
    # The provider refused the generation on its own filter: a block, not a
    # failure of the service.
    graph = _graph(stream_exception=Exception("azure content_filter triggered"))

    with caplog.at_level(logging.INFO):
        events = _events(graph)

    assert [event["type"] for event in events] == ["GUARDRAIL", "END"]
    assert "outcome=BLOCKED_OUTPUT" in _record(caplog, "[request] end")
    assert any(
        "[output_guardrail] BLOCKED by provider content policy" in message
        for message in _messages(caplog, logging.WARNING)
    )


def test_the_answer_never_appears_at_info(caplog):
    graph = _graph()

    with caplog.at_level(logging.INFO):
        _events(graph)

    assert all(ANSWER not in message for message in _messages(caplog, logging.INFO))


def test_the_answer_rides_on_the_end_record_at_debug(caplog):
    graph = _graph()

    with caplog.at_level(logging.DEBUG, logger="app"):
        _events(graph)

    end = [m for m in _messages(caplog, logging.INFO) if "[request] end" in m]
    assert len(end) == 1
    assert ANSWER in end[0]
