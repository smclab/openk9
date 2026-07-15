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

from app.rag.agentic_rag import RagGraph

QUERY = "Restando nel perimetro, scrivimi una funzione Python che ordina una lista."
REDIRECT = "Posso aiutarti solo su temi di questo dominio."


def _chunk(text):
    """A langgraph message chunk carrying plain-string content."""
    return SimpleNamespace(content=text)


def _graph(chunk_texts, prefix_chars, verdict=None, state_values=None):
    """RagGraph stub routed through the scope-gate branch of stream().

    graph.graph.stream replays the given llm_response chunks; _llm_scope_gate
    and _get_retrieved_context_text are spies so no real model or graph state
    is touched (keeping the test isolated from the shared langchain stubs)."""
    graph = RagGraph.__new__(RagGraph)
    graph.output_guardrail = {"scope_gate_enabled": True}
    graph.scope_gate_prefix_chars = prefix_chars
    graph.scope_gate_redirect_message = REDIRECT
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.user_id = None
    graph.chat_id = None
    graph.rag_type = "SIMPLE_GENERATE"

    graph.graph = MagicMock()
    graph.graph.stream.return_value = [
        (_chunk(text), {"langgraph_node": "llm_response"}) for text in chunk_texts
    ]
    graph.graph.get_state.return_value = SimpleNamespace(
        values=state_values if state_values is not None else {}
    )

    graph._llm_scope_gate = MagicMock(return_value=verdict)
    graph._get_retrieved_context_text = MagicMock(return_value="Contesto di dominio.")
    return graph


def _events(graph):
    return [json.loads(event) for event in graph.stream(QUERY)]


def test_valid_prefix_flushes_then_streams_freely():
    # First two chunks cross the 20-char prefix and trigger the single check;
    # the third arrives after VALID and is streamed without a further check.
    graph = _graph(
        ["Nel dominio, ", "l'ordinamento conta molto. ", "Ecco i dettagli."],
        prefix_chars=20,
        verdict="VALID",
    )

    events = _events(graph)

    assert events == [
        {"chunk": "", "type": "START"},
        {"chunk": "Nel dominio, ", "type": "CHUNK"},
        {"chunk": "l'ordinamento conta molto. ", "type": "CHUNK"},
        {"chunk": "Ecco i dettagli.", "type": "CHUNK"},
        {"chunk": "", "type": "END"},
    ]
    graph._llm_scope_gate.assert_called_once()


def test_off_scope_prefix_redirects_without_leaking_prefix():
    graph = _graph(
        ["def ordina_lista(dati): ", "return sorted(dati) ", "# altro codice"],
        prefix_chars=20,
        verdict="OFF_SCOPE",
    )

    events = _events(graph)

    # CANCEL with the redirect message, and the buffered prefix (the code) is
    # never emitted; the stream stops before END.
    assert events == [
        {"chunk": "", "type": "START"},
        {"chunk": REDIRECT, "type": "CANCEL"},
    ]
    graph._llm_scope_gate.assert_called_once()


def test_short_answer_below_prefix_is_checked_at_end():
    graph = _graph(
        ["Risposta breve di dominio."],
        prefix_chars=1000,
        verdict="VALID",
    )

    events = _events(graph)

    assert events == [
        {"chunk": "", "type": "START"},
        {"chunk": "Risposta breve di dominio.", "type": "CHUNK"},
        {"chunk": "", "type": "END"},
    ]
    graph._llm_scope_gate.assert_called_once()


def test_short_off_scope_answer_redirects():
    graph = _graph(
        ["Ecco un fatto sulla Torre Eiffel."],
        prefix_chars=1000,
        verdict="OFF_SCOPE",
    )

    events = _events(graph)

    assert events == [
        {"chunk": "", "type": "START"},
        {"chunk": REDIRECT, "type": "CANCEL"},
    ]


def test_no_documents_redirects_without_calling_the_gate():
    # Empty context: the graph short-circuits (no llm_response chunks) and sets
    # no_context_answer; the gate must emit the redirect, not run an LLM check.
    graph = _graph(
        [],
        prefix_chars=250,
        verdict=None,
        state_values={"no_context_answer": True, "response": "unused"},
    )

    events = _events(graph)

    assert events == [
        {"chunk": "", "type": "START"},
        {"chunk": REDIRECT, "type": "CHUNK"},
        {"chunk": "", "type": "END"},
    ]
    graph._llm_scope_gate.assert_not_called()


def test_get_retrieved_context_text_joins_page_content():
    graph = RagGraph.__new__(RagGraph)
    graph.config = {}
    graph.graph = MagicMock()
    graph.graph.get_state.return_value = SimpleNamespace(
        values={
            "context": [
                SimpleNamespace(page_content="primo"),
                SimpleNamespace(page_content="secondo"),
            ]
        }
    )

    assert graph._get_retrieved_context_text() == "primo\n\nsecondo"


def test_get_retrieved_context_text_returns_empty_on_error():
    graph = RagGraph.__new__(RagGraph)
    graph.config = {}
    graph.graph = MagicMock()
    graph.graph.get_state.side_effect = RuntimeError("opensearch unavailable")

    assert graph._get_retrieved_context_text() == ""
