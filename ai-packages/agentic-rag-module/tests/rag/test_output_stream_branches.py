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


"""What every branch of stream() has in common.

stream() takes one of four paths depending on the output guardrail type: plain
streaming, batched check (1), windowed check (2) and scope gate (3). Each runs
the graph on the same input, streams only the answer produced by the
llm_response node, and turns a rate limit from the provider into the same
ERROR event.
"""

import json
from types import SimpleNamespace
from unittest.mock import MagicMock

import pytest

from app.rag.agentic_rag import RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"
CONFIG = {"configurable": {"thread_id": "chat-1"}}
RATE_LIMIT = {"chunk": "Rate limit exceeded. Try again later.", "type": "ERROR"}
START = {"chunk": "", "type": "START"}
END = {"chunk": "", "type": "END"}
BRANCHES = [0, 1, 2, 3]


def _graph(guardrail_type, enabled=True):
    """RagGraph stub routed through the branch of the given guardrail type,
    with every check it can run answering "let it through"."""
    graph = RagGraph.__new__(RagGraph)
    graph.output_guardrail = {"enable_output_guardrail": enabled}
    graph.output_guardrail_type = guardrail_type
    graph.output_guardrail_chunk_interval = 5
    graph.scope_gate_prefix_chars = 1000
    graph.scope_gate_redirect_message = "Posso aiutarti solo su temi di dominio."
    graph.config = CONFIG
    graph.chat_sequence_number = 3
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "SIMPLE_GENERATE"

    graph.graph = MagicMock()
    graph.graph.stream.return_value = []
    graph.graph.get_state.return_value = SimpleNamespace(values={})

    graph._resolve_target_language = MagicMock(return_value="English")
    graph._llm_output_guardrail = MagicMock(return_value="NONE")
    graph._llm_scope_gate = MagicMock(return_value="VALID")
    graph._get_retrieved_context_text = MagicMock(return_value="Contesto.")
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


@pytest.mark.parametrize("guardrail_type", BRANCHES)
def test_the_graph_runs_on_the_turn_input(guardrail_type):
    graph = _graph(guardrail_type)

    _events(graph)

    graph._resolve_target_language.assert_called_once_with(QUERY)
    graph.graph.stream.assert_called_once_with(
        {"current_query": QUERY, "chat_sequence_number": 3, "target_lang": "English"},
        config=CONFIG,
        stream_mode="messages",
    )
    graph.graph.get_state.assert_called_once_with(CONFIG)


# Types 1 and 2 are covered in test_output_guardrail_chunk_interval.py.
@pytest.mark.parametrize("guardrail_type", [0, 3])
def test_only_llm_response_chunks_are_streamed(guardrail_type):
    graph = _graph(guardrail_type)
    graph.graph.stream.return_value = [
        (SimpleNamespace(content="router"), {"langgraph_node": "rag_router"}),
        (SimpleNamespace(content=""), {"langgraph_node": "llm_response"}),
        (
            SimpleNamespace(content=[{"type": "text", "text": "ans"}]),
            {"langgraph_node": "llm_response"},
        ),
        (SimpleNamespace(content="wer"), {"langgraph_node": "llm_response"}),
    ]

    events = _events(graph)

    assert events == [
        START,
        {"chunk": "ans", "type": "CHUNK"},
        {"chunk": "wer", "type": "CHUNK"},
        END,
    ]


@pytest.mark.parametrize("guardrail_type", BRANCHES)
@pytest.mark.parametrize("message", ["Rate_Limit reached", "Error code: 429"])
def test_either_rate_limit_marker_is_reported_as_a_rate_limit(guardrail_type, message):
    graph = _graph(guardrail_type)
    graph.graph.stream.side_effect = Exception(message)

    assert _events(graph) == [RATE_LIMIT]


@pytest.mark.parametrize("guardrail_type", BRANCHES)
def test_any_other_failure_is_reported_as_it_is(guardrail_type):
    graph = _graph(guardrail_type)
    graph.graph.stream.side_effect = RuntimeError("searcher unreachable")

    assert _events(graph) == [{"chunk": "searcher unreachable", "type": "ERROR"}]


@pytest.mark.parametrize("guardrail_type", [1, 2, 3])
def test_a_disabled_guardrail_ignores_its_type(guardrail_type):
    graph = _graph(guardrail_type, enabled=False)
    graph.graph.stream.return_value = [
        (SimpleNamespace(content=text), {"langgraph_node": "llm_response"})
        for text in ["a", "b"]
    ]

    events = _events(graph)

    assert events == [
        START,
        {"chunk": "a", "type": "CHUNK"},
        {"chunk": "b", "type": "CHUNK"},
        END,
    ]
    graph._llm_output_guardrail.assert_not_called()
    graph._llm_scope_gate.assert_not_called()
