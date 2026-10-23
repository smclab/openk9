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


"""The SOURCES event: the numbering sent ahead of the answer.

The answer carries [2] in its text while it is being written, but the
DOCUMENT events that resolve it only arrive at the end. This event carries
the mapping up front so a client can render the link next to the sentence.
"""

from types import SimpleNamespace
from unittest.mock import MagicMock

import json

from langchain_core.documents import Document

from app.rag.agentic_rag import RagGraph

CONTEXT = [
    Document(page_content="chunk A", metadata={"document_id": "doc-1",
                                               "title": "Uno", "url": "http://uno"}),
    Document(page_content="chunk B", metadata={"document_id": "doc-1",
                                               "title": "Uno", "url": "http://uno"}),
    Document(page_content="chunk C", metadata={"document_id": "doc-2",
                                               "title": "Due", "url": "http://due"}),
]


def _graph(context):
    graph = RagGraph.__new__(RagGraph)
    graph.config = {}
    graph.graph = MagicMock()
    graph.graph.get_state.return_value = SimpleNamespace(values={"context": context})
    return graph


def _event(context):
    events = [json.loads(e) for e in _graph(context)._stream_sources_map()]
    return events[0] if events else None


def test_every_retrieved_source_is_listed_with_its_number():
    event = _event(CONTEXT)

    assert event["type"] == "SOURCES"
    assert event["chunk"] == [
        {"ordinal": 1, "document_id": "doc-1", "title": "Uno", "url": "http://uno"},
        {"ordinal": 2, "document_id": "doc-2", "title": "Due", "url": "http://due"},
    ]


def test_the_numbering_matches_the_one_the_prompt_gave_the_model():
    """The whole thing is worthless if the two disagree: the client would
    resolve [2] to the wrong document. Both number per source, in order of
    first appearance, skipping chunks with no document_id."""
    event = _event(CONTEXT)

    ordinals = {}
    for document in CONTEXT:
        if document_id := document.metadata.get("document_id"):
            ordinals.setdefault(str(document_id), len(ordinals) + 1)

    assert {s["document_id"]: s["ordinal"] for s in event["chunk"]} == ordinals


def test_chunks_without_a_document_id_get_no_number():
    context = [
        Document(page_content="orfano", metadata={}),
        Document(page_content="buono", metadata={"document_id": "doc-9",
                                                 "title": "T", "url": "u"}),
    ]

    assert [s["ordinal"] for s in _event(context)["chunk"]] == [1]
    assert [s["document_id"] for s in _event(context)["chunk"]] == ["doc-9"]


def test_nothing_is_emitted_without_retrieved_sources():
    # SIMPLE_GENERATE, or a router that answered without retrieving: there is
    # no numbering to announce and the client must not get an empty event.
    assert _event([]) is None
    assert _event(None) is None


def test_it_lists_every_retrieved_source_not_only_the_cited_ones():
    # It is sent before the answer exists, so which sources end up cited is
    # not yet known: the client prunes later, with the DOCUMENT events.
    assert len(_event(CONTEXT)["chunk"]) == 2


def test_the_document_id_is_there_for_a_bibliography_across_turns():
    """Ordinals are per request: the [2] of this turn and the [2] of the next
    are different sources. A client keeping one list for the conversation
    keys on the document_id, which DOCUMENT does not carry."""
    ids = [s["document_id"] for s in _event(CONTEXT)["chunk"]]

    assert ids == ["doc-1", "doc-2"]
