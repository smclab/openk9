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


import json
from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.documents import Document

from app.rag.agentic_rag import RagGraph

DOCUMENTS = [
    Document(
        page_content="Primo chunk del documento uno.",
        metadata={"document_id": "doc-1", "title": "Uno", "url": "http://uno"},
    ),
    Document(
        page_content="Secondo chunk del documento uno.",
        metadata={"document_id": "doc-1", "title": "Uno", "url": "http://uno"},
    ),
    Document(
        page_content="Documento due.",
        metadata={"document_id": "doc-2", "title": "Due", "url": "http://due"},
    ),
]


def _documents(citations=None):
    graph = RagGraph.__new__(RagGraph)
    return [
        json.loads(event)["chunk"]
        for event in graph._stream_documents(DOCUMENTS, citations)
    ]


def test_without_citations_every_source_is_emitted_once():
    assert _documents() == [
        {"citations": [], "title": "Uno", "url": "http://uno"},
        {"citations": [], "title": "Due", "url": "http://due"},
    ]


def test_only_cited_sources_are_emitted_with_their_quotes():
    documents = _documents({"doc-2": ["Documento due."]})

    assert documents == [
        {
            "citations": [{"quote": "Documento due."}],
            "title": "Due",
            "url": "http://due",
        }
    ]


def test_a_cited_source_is_emitted_once_across_its_chunks():
    documents = _documents({"doc-1": ["Primo chunk", "Secondo chunk"]})

    assert documents == [
        {
            "citations": [{"quote": "Primo chunk"}, {"quote": "Secondo chunk"}],
            "title": "Uno",
            "url": "http://uno",
        }
    ]


def test_cited_ids_that_were_never_retrieved_produce_no_event():
    assert _documents({"doc-99": ["inventata"]}) == []


def test_stream_passes_the_state_citations_to_the_document_events():
    # End-to-end wiring guard: what llm_response_node stored in the state is
    # what filters the DOCUMENT events.
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_citations = True
    graph.output_guardrail = {"enable_output_guardrail": False}
    graph.output_guardrail_type = 0
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "CHAT_RAG"
    graph.graph = MagicMock()
    graph.graph.stream.return_value = []
    graph.graph.get_state.return_value = SimpleNamespace(
        values={
            "context": DOCUMENTS,
            "citations": {"doc-2": ["Documento due."]},
            "response": "Risposta.",
        }
    )
    graph._resolve_target_language = MagicMock(return_value="Italian")

    events = [json.loads(event) for event in graph.stream("Domanda?")]
    documents = [event["chunk"] for event in events if event["type"] == "DOCUMENT"]

    assert documents == [
        {
            "citations": [{"quote": "Documento due."}],
            "title": "Due",
            "url": "http://due",
        }
    ]


# --- the ordinal: which marker in the answer points at this document ------


def _documents_with_ordinals(citations, ordinals):
    graph = RagGraph.__new__(RagGraph)
    return [
        json.loads(event)["chunk"]
        for event in graph._stream_documents(DOCUMENTS, citations, ordinals)
    ]


def test_each_cited_source_carries_the_number_the_answer_marks_it_with():
    chunks = _documents_with_ordinals({"doc-1": [], "doc-2": []}, {1: "doc-1", 2: "doc-2"})

    assert [(c["ordinal"], c["title"]) for c in chunks] == [(1, "Uno"), (2, "Due")]


def test_the_ordinal_survives_a_gap_in_the_numbering():
    # Only source 2 was marked: its event must still say 2, because the
    # answer reads [2] and a client cannot infer it from the position.
    chunks = _documents_with_ordinals({"doc-2": []}, {2: "doc-2"})

    assert [c["ordinal"] for c in chunks] == [2]


def test_sources_shown_without_an_ordinal_carry_none():
    # The fallback: nothing was marked, so every source is shown and there is
    # no number to attach.
    chunks = _documents_with_ordinals(None, None)

    assert all("ordinal" not in chunk for chunk in chunks)
    assert [chunk["title"] for chunk in chunks] == ["Uno", "Due"]
