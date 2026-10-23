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

"""The output guardrail checks the streamed answer every N chunks.

Type 1 holds each batch of output_guardrail_chunk_interval chunks back until
the answer so far passes the check, so a blocked batch never reaches the
client. Type 2 streams every chunk right away and checks only the latest
window. Both check the tail left over when the stream ends, and a verdict
other than NONE ends the stream with CANCEL.
"""

import json
from types import SimpleNamespace
from unittest.mock import MagicMock

import pytest

from app.rag.agentic_rag import RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"
CANCEL = {"chunk": "Inappropriate content", "type": "CANCEL"}
START = {"chunk": "", "type": "START"}
END = {"chunk": "", "type": "END"}


def _chunk_event(text):
    return {"chunk": text, "type": "CHUNK"}


def _graph(guardrail_type, chunk_texts, verdicts, interval=2):
    """RagGraph stub routed through the given output guardrail branch.

    graph.graph.stream replays the llm_response chunks, and
    _llm_output_guardrail returns the given verdicts in order."""
    graph = RagGraph.__new__(RagGraph)
    graph.output_guardrail = {"enable_output_guardrail": True}
    graph.output_guardrail_type = guardrail_type
    graph.output_guardrail_chunk_interval = interval
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "SIMPLE_GENERATE"

    graph.graph = MagicMock()
    graph.graph.stream.return_value = [
        (SimpleNamespace(content=text), {"langgraph_node": "llm_response"})
        for text in chunk_texts
    ]
    graph.graph.get_state.return_value = SimpleNamespace(values={})

    graph._resolve_target_language = MagicMock(return_value="Italian")
    graph._llm_output_guardrail = MagicMock(side_effect=verdicts)
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


def _checks(graph):
    return [call.args for call in graph._llm_output_guardrail.call_args_list]


def test_batched_clean_answer_is_released_batch_by_batch():
    graph = _graph(1, ["a", "b", "c"], ["NONE", "NONE"])

    events = _events(graph)

    assert events == [
        START,
        _chunk_event("a"),
        _chunk_event("b"),
        _chunk_event("c"),
        END,
    ]
    # Each check reads the whole answer so far.
    assert _checks(graph) == [("ab", "chunk_interval"), ("abc", "final_tail")]


def test_blocked_batch_is_never_released():
    graph = _graph(1, ["a", "b", "c"], ["VIOLENCE"])

    events = _events(graph)

    assert events == [START, CANCEL]


def test_blocked_tail_is_never_released():
    graph = _graph(1, ["a", "b", "c"], ["NONE", "VIOLENCE"])

    events = _events(graph)

    assert events == [START, _chunk_event("a"), _chunk_event("b"), CANCEL]


def test_batched_answer_ending_on_an_interval_has_no_tail_check():
    graph = _graph(1, ["a", "b"], ["NONE"])

    events = _events(graph)

    assert events == [START, _chunk_event("a"), _chunk_event("b"), END]
    assert _checks(graph) == [("ab", "chunk_interval")]


def test_streamed_clean_answer_checks_each_window():
    graph = _graph(2, ["a", "b", "c"], ["NONE", "NONE"])

    events = _events(graph)

    assert events == [
        START,
        _chunk_event("a"),
        _chunk_event("b"),
        _chunk_event("c"),
        END,
    ]
    # Each check reads only the chunks since the previous one.
    assert _checks(graph) == [("ab", "chunk_interval"), ("c", "final_tail")]


def test_streamed_answer_stops_on_the_chunk_that_fails_the_check():
    graph = _graph(2, ["a", "b", "c"], ["VIOLENCE"])

    events = _events(graph)

    assert events == [START, _chunk_event("a"), CANCEL]


def test_streamed_answer_blocked_on_the_tail():
    graph = _graph(2, ["a", "b", "c"], ["NONE", "VIOLENCE"])

    events = _events(graph)

    assert events == [
        START,
        _chunk_event("a"),
        _chunk_event("b"),
        _chunk_event("c"),
        CANCEL,
    ]


def test_streamed_answer_ending_on_an_interval_has_no_tail_check():
    graph = _graph(2, ["a", "b"], ["NONE"])

    events = _events(graph)

    assert events == [START, _chunk_event("a"), _chunk_event("b"), END]
    assert _checks(graph) == [("ab", "chunk_interval")]


@pytest.mark.parametrize("guardrail_type", [1, 2])
def test_only_llm_response_chunks_are_streamed(guardrail_type):
    graph = _graph(guardrail_type, [], ["NONE"], interval=5)
    graph.graph.stream.return_value = [
        (SimpleNamespace(content="router"), {"langgraph_node": "rag_router"}),
        (SimpleNamespace(content=""), {"langgraph_node": "llm_response"}),
        (
            SimpleNamespace(content=[{"type": "text", "text": "answer"}]),
            {"langgraph_node": "llm_response"},
        ),
    ]

    events = _events(graph)

    assert events == [START, _chunk_event("answer"), END]


@pytest.mark.parametrize("guardrail_type", [1, 2])
def test_rate_limit_is_reported_as_error(guardrail_type):
    graph = _graph(guardrail_type, [], [])
    graph.graph.stream.side_effect = Exception("Error code: 429 - rate_limit")

    events = _events(graph)

    assert events == [
        {"chunk": "Rate limit exceeded. Try again later.", "type": "ERROR"}
    ]
