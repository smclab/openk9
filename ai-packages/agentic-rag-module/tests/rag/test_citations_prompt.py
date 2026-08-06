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


def test_flag_on_labels_the_context_and_asks_for_the_citations_block():
    graph = _graph(retrieve_citations=True, answer="Risposta.")

    graph.llm_response_node(_state())

    prompt = _rendered_prompt(graph)
    # The JSON shape in the instruction reaches the model verbatim: it must not
    # be mistaken for template variables while rendering.
    assert "[document_id: doc-1]" in prompt
    assert "[document_id: doc-2]" in prompt
    assert CITATIONS_DELIMITER in prompt
    assert '[{"document_id": "<id>", "quotes": ["..."]}]' in prompt


def test_flag_off_leaves_the_prompt_and_the_context_unlabelled():
    graph = _graph(retrieve_citations=False, answer="Risposta.")

    state = graph.llm_response_node(_state())

    prompt = _rendered_prompt(graph)
    assert "document_id" not in prompt
    assert CITATIONS_DELIMITER not in prompt
    assert state.response == "Risposta."
    assert state.citations is None


def test_the_saved_response_excludes_the_citations_tail():
    graph = _graph(
        retrieve_citations=True,
        answer=(
            "La polizza copre la rottura dei vetri.\n"
            f"{CITATIONS_DELIMITER}\n"
            '[{"document_id": "doc-1", "quotes": ["copre la rottura dei vetri"]}]'
        ),
    )

    state = graph.llm_response_node(_state())

    assert state.response == "La polizza copre la rottura dei vetri."
    assert state.citations == {"doc-1": ["copre la rottura dei vetri"]}


def test_a_missing_citations_block_leaves_the_answer_untouched():
    graph = _graph(retrieve_citations=True, answer="Risposta senza citazioni.")

    state = graph.llm_response_node(_state())

    assert state.response == "Risposta senza citazioni."
    assert state.citations is None


def test_the_direct_branch_is_never_asked_for_citations():
    graph = _graph(retrieve_citations=True, answer="Risposta diretta.")

    state = graph.llm_response_node(GraphState(current_query=QUERY, use_rag=False))

    assert CITATIONS_DELIMITER not in _rendered_prompt(graph)
    assert state.response == "Risposta diretta."
    assert state.citations is None
