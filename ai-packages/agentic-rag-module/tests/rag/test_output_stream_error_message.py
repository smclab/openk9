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

"""Tests that a refusal's reason reaches the SSE stream.

A query the datasource refuses (an unacceptable media, for instance) surfaces as
an HTTPException below 500 raised from inside the graph. The ERROR event must
then carry the reason in `message`, which the frontend already reads. An
internal failure keeps the pre-existing opaque event.
"""

import json
from unittest.mock import MagicMock, patch

import pytest
from fastapi import HTTPException
from langgraph.checkpoint.memory import InMemorySaver
from langgraph.graph import StateGraph

from app.rag import chain as chain_module
from app.rag.agentic_rag import GraphState, RagGraph
from app.rag.chain import UNEXPECTED_ERROR_MESSAGE

QUERY = "Qual è la copertura per i danni da grandine?"
REFUSAL = "media is only supported on KNN tokens, got tokenType=TEXT"


def _graph(stream_exception):
    """Bare RagGraph whose graph.stream immediately raises, so stream() drops
    straight into the outer except."""
    graph = RagGraph.__new__(RagGraph)
    graph.output_guardrail = {}
    graph.output_guardrail_type = 0
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "SIMPLE_GENERATE"
    graph.graph = MagicMock()
    graph.graph.stream.side_effect = stream_exception
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


def test_refused_query_reports_its_reason():
    graph = _graph(HTTPException(status_code=400, detail=REFUSAL))

    events = _events(graph)

    assert len(events) == 1
    assert events[0]["type"] == "ERROR"
    assert events[0]["message"] == REFUSAL


def test_internal_failure_carries_no_message():
    # Behaviour of today, unchanged: without `message` the frontend shows its
    # own fallback text.
    graph = _graph(HTTPException(status_code=500, detail=UNEXPECTED_ERROR_MESSAGE))

    events = _events(graph)

    assert events[0]["type"] == "ERROR"
    assert "message" not in events[0]


def test_non_http_failure_carries_no_message():
    graph = _graph(RuntimeError("searcher unreachable"))

    events = _events(graph)

    assert events == [{"chunk": "searcher unreachable", "type": "ERROR"}]


def _chain_events(setup_exception):
    """Run get_agentic_rag with the graph construction failing, so its own
    except block is the one that emits the ERROR event."""
    with patch.object(chain_module, "RagGraph", side_effect=setup_exception):
        return [
            json.loads(event)
            for event in chain_module.get_agentic_rag(
                "CHAT_RAG",
                None,
                None,
                None,
                None,
                None,
                None,
                {},
                None,
                None,
                None,
                QUERY,
                None,
                None,
                "tenant-1",
                False,
                None,
                "1",
                1,
                {},
                {},
                {},
                "http://localhost:9200",
                "localhost:50052",
                "localhost:50051",
            )
        ]


def test_a_node_failure_reaches_the_stream_unwrapped():
    # The refusal is raised by the retriever, deep inside a graph node, and the
    # whole feature rests on langgraph letting it through as it is: were an
    # upgrade to start wrapping node exceptions, `message` would silently stop
    # appearing and no other test here would notice.
    def refuse(state):
        raise HTTPException(status_code=400, detail=REFUSAL)

    workflow = StateGraph(GraphState)
    workflow.add_node("refuse", refuse)
    workflow.set_entry_point("refuse")
    graph = workflow.compile(checkpointer=InMemorySaver())

    with pytest.raises(HTTPException) as raised:
        for _ in graph.stream(
            {"current_query": ""},
            config={"configurable": {"thread_id": "thread-1"}},
            stream_mode="messages",
        ):
            pass

    assert raised.value.status_code == 400
    assert raised.value.detail == REFUSAL


def test_the_other_chain_applies_the_same_rule():
    # The rule must not differ between the two except blocks, or an error
    # message would appear on one path and vanish on the other.
    assert _chain_events(HTTPException(status_code=400, detail=REFUSAL)) == [
        {"chunk": UNEXPECTED_ERROR_MESSAGE, "type": "ERROR", "message": REFUSAL}
    ]
    assert _chain_events(RuntimeError("boom")) == [
        {"chunk": UNEXPECTED_ERROR_MESSAGE, "type": "ERROR"}
    ]
