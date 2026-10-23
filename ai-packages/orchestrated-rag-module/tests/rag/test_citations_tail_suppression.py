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

from app.rag.agentic_rag import CITATIONS_DELIMITER, CitationsTailFilter, RagGraph

QUERY = "Quali garanzie accessorie copre la polizza?"


def _chunk(text):
    """A langgraph message chunk carrying plain-string content."""
    return SimpleNamespace(content=text)


def _graph(chunk_texts, retrieve_citations, guardrail_type=0):
    """RagGraph stub whose graph replays the given llm_response chunks."""
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_citations = retrieve_citations
    graph.output_guardrail = {
        "enable_output_guardrail": guardrail_type != 0,
    }
    graph.output_guardrail_type = guardrail_type
    graph.output_guardrail_chunk_interval = 2
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "SIMPLE_GENERATE"

    graph.graph = MagicMock()
    graph.graph.stream.return_value = [
        (_chunk(text), {"langgraph_node": "llm_response"}) for text in chunk_texts
    ]
    graph.graph.get_state.return_value = SimpleNamespace(values={})

    graph._resolve_target_language = MagicMock(return_value="Italian")
    graph._llm_output_guardrail = MagicMock(return_value="NONE")
    return graph


def _streamed_text(graph):
    return "".join(
        event["chunk"]
        for event in (json.loads(event) for event in graph.stream(QUERY))
        if event["type"] == "CHUNK"
    )


def test_tail_is_suppressed_from_the_streamed_answer():
    graph = _graph(
        [
            "La polizza copre ",
            "gli infortuni del conducente.",
            f"{CITATIONS_DELIMITER}[",
            '{"document_id": "1", "quotes": ["infortuni del conducente"]}]',
        ],
        retrieve_citations=True,
    )

    assert _streamed_text(graph) == "La polizza copre gli infortuni del conducente."


def test_delimiter_split_across_tokens_never_leaks():
    # The marker arrives one character at a time: no fragment of it may reach
    # the client, not even the leading "<".
    graph = _graph(
        ["Risposta."] + list(CITATIONS_DELIMITER) + ['[{"document_id": "1"}]'],
        retrieve_citations=True,
    )

    assert _streamed_text(graph) == "Risposta."


def test_text_that_only_looks_like_the_delimiter_is_emitted():
    # Held-back characters that turn out to be plain text must be flushed,
    # including when the answer itself ends with them.
    graph = _graph(["Confronta a < b ", "e poi c <<"], retrieve_citations=True)

    assert _streamed_text(graph) == "Confronta a < b e poi c <<"


def test_flag_off_streams_the_text_untouched():
    # With the flag off nothing is buffered or dropped, even if the answer
    # happens to contain the marker.
    graph = _graph(
        ["Risposta.", f"{CITATIONS_DELIMITER}[]"],
        retrieve_citations=False,
    )

    assert _streamed_text(graph) == f"Risposta.{CITATIONS_DELIMITER}[]"


def test_tail_is_suppressed_in_the_output_guardrail_branch():
    # Every stream() branch shares the same filter: the batching guardrail
    # branch must not emit the tail either, and the guardrail check itself only
    # sees the clean answer.
    graph = _graph(
        ["Prima parte. ", "Seconda parte.", f"{CITATIONS_DELIMITER}[]"],
        retrieve_citations=True,
        guardrail_type=1,
    )

    assert _streamed_text(graph) == "Prima parte. Seconda parte."
    assert CITATIONS_DELIMITER not in graph._llm_output_guardrail.call_args.args[0]


def test_filter_holds_back_only_a_possible_delimiter_prefix():
    citations_filter = CitationsTailFilter(enabled=True)

    # "<<" could still become the delimiter, so it waits; the next token
    # settles it and both parts are emitted in order.
    assert citations_filter.feed("Testo <<") == "Testo "
    assert citations_filter.feed("a") == "<<a"
    assert citations_filter.flush() == ""


def test_filter_stops_emitting_once_the_tail_started():
    citations_filter = CitationsTailFilter(enabled=True)

    assert citations_filter.feed(f"Testo{CITATIONS_DELIMITER}[") == "Testo"
    assert citations_filter.feed('{"document_id": "1"}]') == ""
    assert citations_filter.flush() == ""
