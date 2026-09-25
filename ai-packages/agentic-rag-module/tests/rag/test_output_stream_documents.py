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

"""The events stream() sends after the answer: title and sources.

Once the answer is streamed, stream() reads the final graph state and sends
the conversation title on the first turn of a saved chat, then one DOCUMENT
event per retrieved document. Chunks of the same document are sent once, and
only the metadata the document carries is included.
"""

import json
from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.documents import Document

from app.rag.agentic_rag import RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"

FULL_METADATA = {
    "document_id": "doc-1",
    "score": 1.5,
    "title": "Polizza casa",
    "url": "http://example.org/casa",
    "domain": "insurance",
}


def _graph(state_values, *, rag_type="AGENTIC", sequence_number=1, logged=True):
    """RagGraph stub on the plain stream branch, whose graph produces no
    answer chunk and ends in the given state."""
    graph = RagGraph.__new__(RagGraph)
    graph.output_guardrail = {}
    graph.output_guardrail_type = 0
    graph.config = {}
    graph.chat_sequence_number = sequence_number
    graph.tenant_id = None
    graph.user_id = "user-1" if logged else None
    graph.chat_id = "chat-1" if logged else None
    graph.rag_type = rag_type

    graph.graph = MagicMock()
    graph.graph.stream.return_value = []
    graph.graph.get_state.return_value = SimpleNamespace(values=state_values)

    graph._resolve_target_language = MagicMock(return_value="Italian")
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


def _documents(events):
    return [event["chunk"] for event in events if event["type"] == "DOCUMENT"]


def test_document_event_carries_the_document_metadata():
    graph = RagGraph.__new__(RagGraph)

    events = [
        json.loads(event)
        for event in graph._stream_documents(
            [Document("chunk", metadata=FULL_METADATA)]
        )
    ]

    assert events == [
        {
            "chunk": {
                "citations": [],
                "score": 1.5,
                "title": "Polizza casa",
                "url": "http://example.org/casa",
                "domain": "insurance",
            },
            "type": "DOCUMENT",
        }
    ]


def test_missing_metadata_is_left_out_of_the_event():
    graph = RagGraph.__new__(RagGraph)

    events = [
        json.loads(event)
        for event in graph._stream_documents(
            [Document("chunk", metadata={"document_id": "doc-1", "title": "Titolo"})]
        )
    ]

    assert events[0]["chunk"] == {"citations": [], "title": "Titolo"}


def test_chunks_of_the_same_document_are_sent_once():
    graph = RagGraph.__new__(RagGraph)
    documents = [
        Document("first", metadata={"document_id": "doc-1", "title": "A"}),
        Document("second", metadata={"document_id": "doc-1", "title": "A"}),
        Document("third", metadata={"document_id": "doc-2", "title": "B"}),
    ]

    events = [json.loads(event) for event in graph._stream_documents(documents)]

    assert [event["chunk"]["title"] for event in events] == ["A", "B"]


def test_document_without_id_is_not_sent():
    graph = RagGraph.__new__(RagGraph)

    events = list(graph._stream_documents([Document("chunk", metadata={"title": "A"})]))

    assert events == []


def test_stream_ends_with_title_then_sources():
    graph = _graph(
        {
            "conversation_title": "Grandine",
            "context": [Document("chunk", metadata=FULL_METADATA)],
        }
    )

    events = _events(graph)

    assert [event["type"] for event in events] == ["TITLE", "DOCUMENT", "END"]
    assert events[0]["chunk"] == "Grandine"


def test_title_is_sent_only_on_the_first_turn():
    graph = _graph({"conversation_title": "Grandine"}, sequence_number=2)

    events = _events(graph)

    assert [event["type"] for event in events] == ["END"]


def test_title_is_not_sent_for_an_unsaved_chat():
    graph = _graph({"conversation_title": "Grandine"}, logged=False)

    events = _events(graph)

    assert [event["type"] for event in events] == ["END"]


def test_simple_generate_sends_no_sources():
    graph = _graph(
        {"context": [Document("chunk", metadata=FULL_METADATA)]},
        rag_type="SIMPLE_GENERATE",
    )

    assert _documents(_events(graph)) == []


def test_blocked_query_sends_no_sources():
    graph = _graph(
        {
            "guardrail_check": True,
            "response": "Guardrail violation",
            "context": [Document("chunk", metadata=FULL_METADATA)],
        }
    )

    events = _events(graph)

    assert events == [
        {"chunk": "Guardrail violation", "type": "GUARDRAIL"},
        {"chunk": "", "type": "END"},
    ]
