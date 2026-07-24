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


def _graph():
    """RagGraph stub exercising llm_response_node with a spy LLM, so we can
    assert the resolved target language reaches the prompt sent to the model.
    The node reads state.target_lang; it does NOT resolve the language itself
    (resolution happens once in stream()/invoke(), outside the graph)."""
    graph = RagGraph.__new__(RagGraph)
    graph.answer_only_with_context = False
    graph.configuration = {"prompt_template": "SYSTEM", "prompt_no_rag": "SYSTEM"}
    graph.llm = MagicMock()
    graph.llm.return_value = SimpleNamespace(content="ANSWER")
    return graph


def _rendered_prompt(graph):
    return graph.llm.call_args.args[0].to_string()


def test_rag_branch_injects_target_language_from_state():
    graph = _graph()

    state = graph.llm_response_node(
        GraphState(
            current_query="Parlez-moi de DGS",
            target_lang="French",
            use_rag=True,
            context=[Document(page_content="DGS est une societe de conseil.")],
        )
    )

    assert state.target_lang == "French"
    rendered = _rendered_prompt(graph)
    assert DIRECTIVE in rendered
    assert "French" in rendered


def test_direct_branch_injects_target_language_from_state():
    graph = _graph()

    graph.llm_response_node(
        GraphState(current_query="Bonjour", target_lang="French", use_rag=False, context=[])
    )

    rendered = _rendered_prompt(graph)
    assert DIRECTIVE in rendered
    assert "French" in rendered


def test_falls_back_to_italian_when_target_lang_missing():
    # Defensive: if no target_lang was seeded, the directive must not render
    # "None" -- it defaults to Italian.
    graph = _graph()

    graph.llm_response_node(
        GraphState(current_query="Bonjour", use_rag=False, context=[])
    )

    rendered = _rendered_prompt(graph)
    assert "Italian" in rendered
    assert "None" not in rendered.split(DIRECTIVE, 1)[1].splitlines()[0]


def test_italian_query_has_no_regression():
    graph = _graph()

    state = graph.llm_response_node(
        GraphState(
            current_query="Parlami di DGS",
            target_lang="Italian",
            use_rag=True,
            context=[Document(page_content="DGS e una societa di consulenza.")],
        )
    )

    graph.llm.assert_called()
    assert "Italian" in _rendered_prompt(graph)
    assert state.target_lang == "Italian"


def test_invoke_seeds_resolved_target_language():
    # Regression guard: the language must be resolved for the graph seed, not
    # inside the streamed llm_response node.
    graph = RagGraph.__new__(RagGraph)
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.graph = MagicMock()
    graph._resolve_target_language = MagicMock(return_value="Spanish")

    graph.invoke("Hablame de DGS")

    graph._resolve_target_language.assert_called_once_with("Hablame de DGS")
    seed = graph.graph.invoke.call_args.args[0]
    assert seed["target_lang"] == "Spanish"


def test_stream_resolves_language_before_graph_and_seeds_it():
    # Regression guard for the leaked-chunks bug: stream() must resolve the
    # language BEFORE iterating the graph and pass it via the state seed, so
    # the resolver's LLM output never enters the llm_response token stream.
    graph = RagGraph.__new__(RagGraph)
    graph.config = {}
    graph.chat_sequence_number = 1
    graph.output_guardrail = {"enable_output_guardrail": True}
    graph.output_guardrail_type = 3
    graph.scope_gate_prefix_chars = 1000
    graph.graph = MagicMock()
    graph.graph.stream.return_value = iter([])
    graph._resolve_target_language = MagicMock(return_value="German")

    list(graph.stream("Erzaehl mir von DGS"))

    graph._resolve_target_language.assert_called_once_with("Erzaehl mir von DGS")
    seed = graph.graph.stream.call_args.args[0]
    assert seed["target_lang"] == "German"
