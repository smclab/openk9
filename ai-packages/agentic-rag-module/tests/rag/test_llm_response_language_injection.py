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

from app.rag.agentic_rag import GraphState, RagGraph

DIRECTIVE = "Write your entire answer in"


def _graph(resolved_language="French"):
    """RagGraph stub exercising llm_response_node with a spy LLM and a fixed
    resolved target language, so we can assert the language directive reaches
    the prompt sent to the model."""
    graph = RagGraph.__new__(RagGraph)
    graph.answer_only_with_context = False
    graph.configuration = {"prompt_template": "SYSTEM", "prompt_no_rag": "SYSTEM"}
    graph.llm = MagicMock()
    graph.llm.return_value = SimpleNamespace(content="ANSWER")
    graph._resolve_target_language = MagicMock(return_value=resolved_language)
    return graph


def _rendered_prompt(graph):
    return graph.llm.call_args.args[0].to_string()


def test_rag_branch_injects_resolved_language():
    graph = _graph(resolved_language="French")

    state = graph.llm_response_node(
        GraphState(
            current_query="Parlez-moi de DGS",
            use_rag=True,
            context=[Document(page_content="DGS est une societe de conseil.")],
        )
    )

    graph._resolve_target_language.assert_called_once()
    assert state.target_lang == "French"
    rendered = _rendered_prompt(graph)
    assert DIRECTIVE in rendered
    assert "French" in rendered


def test_direct_branch_injects_resolved_language():
    graph = _graph(resolved_language="French")

    state = graph.llm_response_node(
        GraphState(current_query="Bonjour", use_rag=False, context=[])
    )

    assert state.target_lang == "French"
    rendered = _rendered_prompt(graph)
    assert DIRECTIVE in rendered
    assert "French" in rendered


def test_resolution_prefers_original_query():
    # target language must be resolved from the user's ORIGINAL words, not the
    # (possibly rewritten) current_query.
    graph = _graph()

    graph.llm_response_node(
        GraphState(
            current_query="rewritten query",
            original_query="testo originale",
            use_rag=False,
            context=[],
        )
    )

    graph._resolve_target_language.assert_called_once_with("testo originale")


def test_italian_query_has_no_regression():
    graph = _graph(resolved_language="Italian")

    state = graph.llm_response_node(
        GraphState(
            current_query="Parlami di DGS",
            use_rag=True,
            context=[Document(page_content="DGS e una societa di consulenza.")],
        )
    )

    graph.llm.assert_called()
    assert state.target_lang == "Italian"
    assert "Italian" in _rendered_prompt(graph)
