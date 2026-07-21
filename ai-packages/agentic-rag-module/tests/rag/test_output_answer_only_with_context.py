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


from unittest.mock import MagicMock

from langchain_core.documents import Document

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"

NO_CONTEXT_MESSAGE = (
    "No information found in the knowledge base to answer your "
    "question. Please try rephrasing it."
)


def _graph(answer_only_with_context):
    """Build a RagGraph stub exercising llm_response_node in isolation: the
    LLM is a spy so we can assert whether free generation was invoked."""
    graph = RagGraph.__new__(RagGraph)
    graph.answer_only_with_context = answer_only_with_context
    graph.configuration = {"prompt_template": "PROMPT", "prompt_no_rag": "PROMPT"}
    graph.llm = MagicMock()
    graph.llm.return_value.content = "LLM ANSWER"
    return graph


def test_empty_context_short_circuits_when_flag_enabled():
    graph = _graph(answer_only_with_context=True)

    state = graph.llm_response_node(
        GraphState(current_query=QUERY, use_rag=True, context=[])
    )

    # No free generation: the LLM must not be called and a deterministic
    # no-context message is returned.
    graph.llm.assert_not_called()
    assert state.response == NO_CONTEXT_MESSAGE
    assert state.no_context_answer is True


def test_empty_context_generates_freely_when_flag_disabled():
    graph = _graph(answer_only_with_context=False)

    state = graph.llm_response_node(
        GraphState(current_query=QUERY, use_rag=True, context=[])
    )

    # Flag off: legacy behaviour, the model answers from parametric knowledge.
    graph.llm.assert_called()
    assert state.no_context_answer is False


def test_direct_path_is_not_gated():
    graph = _graph(answer_only_with_context=True)

    # use_rag=False (e.g. bypass_rag / router -> DIRECT): free generation is
    # the intended behaviour and must not be short-circuited.
    state = graph.llm_response_node(
        GraphState(current_query=QUERY, use_rag=False, context=[])
    )

    graph.llm.assert_called()
    assert state.no_context_answer is False


def test_present_context_uses_rag_path():
    graph = _graph(answer_only_with_context=True)

    state = graph.llm_response_node(
        GraphState(
            current_query=QUERY,
            use_rag=True,
            context=[Document(page_content="La grandine è coperta.")],
        )
    )

    # Documents retrieved: normal grounded RAG answer, no regression.
    graph.llm.assert_called()
    assert state.no_context_answer is False
