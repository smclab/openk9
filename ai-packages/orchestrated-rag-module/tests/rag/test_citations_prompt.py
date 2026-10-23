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


from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.documents import Document

from app.rag.agentic_rag import (
    CITATIONS_DELIMITER,
    GraphState,
    RagGraph,
)

QUERY = "Quali garanzie copre la polizza?"
CONTEXT = [
    Document(
        page_content="La garanzia Cristalli copre la rottura dei vetri.",
        metadata={"document_id": "doc-1"},
    ),
    Document(
        page_content="La garanzia Furto copre il furto del veicolo.",
        metadata={"document_id": "doc-2"},
    ),
]


def _graph(retrieve_citations, answer):
    graph = RagGraph.__new__(RagGraph)
    graph.retrieve_citations = retrieve_citations
    graph.answer_only_with_context = True
    graph.configuration = {"prompt_template": "SYSTEM", "prompt_no_rag": "SYSTEM"}
    graph.llm = MagicMock()
    graph.llm.return_value = SimpleNamespace(content=answer)
    return graph


def _rendered_prompt(graph):
    return graph.llm.call_args.args[0].to_string()


def _state():
    return GraphState(current_query=QUERY, use_rag=True, context=CONTEXT)


def test_the_context_is_numbered_and_the_markers_are_always_asked_for():
    # Numbering and markers do not depend on retrieve_citations: they are what
    # lets the answer carry only the sources it used.
    graph = _graph(retrieve_citations=False, answer="Risposta.")

    graph.llm_response_node(_state())

    prompt = _rendered_prompt(graph)
    assert "[1]\nLa garanzia Cristalli" in prompt
    assert "[2]\nLa garanzia Furto" in prompt
    assert "square brackets at the end of the sentence" in prompt


def test_the_flag_adds_the_quotes_block_on_top_of_the_markers():
    graph = _graph(retrieve_citations=True, answer="Risposta.")

    graph.llm_response_node(_state())

    prompt = _rendered_prompt(graph)
    assert "square brackets at the end of the sentence" in prompt
    assert CITATIONS_DELIMITER in prompt
    # The JSON shape in the instruction reaches the model verbatim: it must not
    # be mistaken for template variables while rendering.
    assert '{"2": ["..."], "5": ["..."]}' in prompt


def test_flag_off_asks_for_no_quotes_block():
    graph = _graph(retrieve_citations=False, answer="Risposta.")

    state = graph.llm_response_node(_state())

    assert CITATIONS_DELIMITER not in _rendered_prompt(graph)
    assert state.response == "Risposta."
    # No marker in the answer, so every source is kept.
    assert state.citations is None


def test_the_saved_response_excludes_the_citations_tail():
    graph = _graph(
        retrieve_citations=True,
        answer=(
            "La polizza copre la rottura dei vetri [1].\n"
            f"{CITATIONS_DELIMITER}\n"
            '{"1": ["copre la rottura dei vetri"]}'
        ),
    )

    state = graph.llm_response_node(_state())

    # The tail is cut off; the marker stays, because the client renders it.
    assert state.response == "La polizza copre la rottura dei vetri [1]."
    assert state.citations == {"doc-1": ["copre la rottura dei vetri"]}
    assert state.citation_ordinals == {1: "doc-1"}


def test_a_missing_quotes_block_keeps_the_markers_without_evidence():
    graph = _graph(retrieve_citations=True, answer="Risposta [2].")

    state = graph.llm_response_node(_state())

    assert state.response == "Risposta [2]."
    assert state.citations == {"doc-2": []}


def test_the_direct_branch_is_never_asked_for_citations():
    graph = _graph(retrieve_citations=True, answer="Risposta diretta.")

    state = graph.llm_response_node(GraphState(current_query=QUERY, use_rag=False))

    prompt = _rendered_prompt(graph)
    assert CITATIONS_DELIMITER not in prompt
    assert "square brackets at the end of the sentence" not in prompt
    assert state.response == "Risposta diretta."
    assert state.citations is None


# --- what the flag does and does not gate ---------------------------------


def test_sources_are_filtered_even_with_the_flag_off():
    """The point of the change: a tenant that never asked for citations still
    gets only the documents the answer used, because the markers say which."""
    graph = _graph(retrieve_citations=False, answer="Copre i vetri [1].")

    state = graph.llm_response_node(_state())

    assert state.citations == {"doc-1": []}
    assert state.citation_ordinals == {1: "doc-1"}


def test_the_markers_reach_the_client_with_the_flag_off():
    graph = _graph(retrieve_citations=False, answer="Copre i vetri [1].")

    state = graph.llm_response_node(_state())

    # Not suppressed: the client turns [1] into a footnote, and a client that
    # cannot still shows something readable.
    assert state.response == "Copre i vetri [1]."


def test_quotes_are_absent_with_the_flag_off_even_if_the_model_sends_them():
    # The model was not asked for a tail; if it writes one anyway it is not
    # parsed, and it stays out of the saved answer only because the streaming
    # filter is off here - so the tail would be visible. Worth knowing.
    graph = _graph(retrieve_citations=False, answer="Copre i vetri [1].")

    state = graph.llm_response_node(_state())

    assert all(quotes == [] for quotes in state.citations.values())


def test_with_no_quotes_asked_the_model_is_told_to_append_nothing():
    """Regression guard. With the flag off there is no tail expected, so the
    streaming filter is off too: a source list the model appends on its own
    would reach the reader. The instruction has to forbid it."""
    graph = _graph(retrieve_citations=False, answer="Risposta [1].")

    graph.llm_response_node(_state())

    prompt = _rendered_prompt(graph)
    assert "Write no list of sources at the end" in prompt


def test_with_quotes_asked_the_model_is_not_told_to_append_nothing():
    # The opposite: a tail IS expected, and forbidding it would contradict
    # the quotes instruction.
    graph = _graph(retrieve_citations=True, answer="Risposta [1].")

    graph.llm_response_node(_state())

    assert "Write no list of sources at the end" not in _rendered_prompt(graph)
