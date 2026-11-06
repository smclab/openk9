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
"""What the answering prompt asks for, and what comes back in the state."""

from types import SimpleNamespace
from unittest.mock import MagicMock

from langchain_core.documents import Document

from app.rag.agentic_rag import GraphState, RagGraph

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


def _graph(answer):
    graph = RagGraph.__new__(RagGraph)
    graph.answer_only_with_context = True
    graph.configuration = {"prompt_template": "SYSTEM", "prompt_no_rag": "SYSTEM"}
    graph.llm = MagicMock()
    graph.llm.return_value = SimpleNamespace(content=answer)
    return graph


def _rendered_prompt(graph):
    return graph.llm.call_args.args[0].to_string()


def _state():
    return GraphState(current_query=QUERY, use_rag=True, context=CONTEXT)


def test_the_context_is_numbered_and_the_markers_are_asked_for():
    graph = _graph("Risposta.")

    graph.llm_response_node(_state())

    prompt = _rendered_prompt(graph)
    assert "[1]\nLa garanzia Cristalli" in prompt
    assert "[2]\nLa garanzia Furto" in prompt
    assert "square brackets at the end of the sentence" in prompt


def test_the_model_is_told_to_append_nothing_after_the_answer():
    # Nothing is expected after the answer, so nothing filters a list of
    # sources the model appends on its own: it would reach the reader.
    graph = _graph("Risposta.")

    graph.llm_response_node(_state())

    assert "Write no list of sources at the end" in _rendered_prompt(graph)


def test_the_marked_sources_come_back_in_the_state():
    graph = _graph("La polizza copre la rottura dei vetri [1].")

    state = graph.llm_response_node(_state())

    # The marker stays in the answer: the client renders it.
    assert state.response == "La polizza copre la rottura dei vetri [1]."
    assert state.citations == {"doc-1": []}
    assert state.citation_ordinals == {1: "doc-1"}


def test_an_unmarked_answer_keeps_every_source():
    graph = _graph("Risposta.")

    state = graph.llm_response_node(_state())

    assert state.citations is None
    assert state.citation_ordinals is None


def test_the_direct_branch_is_never_asked_for_markers():
    graph = _graph("Risposta diretta.")

    state = graph.llm_response_node(GraphState(current_query=QUERY, use_rag=False))

    assert "square brackets at the end of the sentence" not in _rendered_prompt(graph)
    assert state.response == "Risposta diretta."
    assert state.citations is None
