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

from langchain_core.messages import AIMessage, HumanMessage

from app.rag.agentic_rag import GraphState, RagGraph

ORIGINAL_QUERY = "dimmi di più"


def _graph(reformulate, *, classification="FOLLOW_UP", rag_type="CHAT_RAG"):
    """Build a RagGraph stub exercising analyze_and_rewrite_query_node in
    isolation: the analyze chain is forced to a fixed classification and the
    rewrite step is a spy."""
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = rag_type
    graph.chat_sequence_number = 2
    graph.reformulate = reformulate
    graph.configuration = {}
    graph.utility_llm = MagicMock()
    graph.utility_llm.with_structured_output.return_value = (
        lambda _prompt_value: SimpleNamespace(
            response=SimpleNamespace(value=classification)
        )
    )
    graph._rewrite_query = MagicMock(return_value="REWRITTEN QUERY")
    return graph


def _followup_state():
    return GraphState(
        current_query=ORIGINAL_QUERY,
        messages=[
            HumanMessage(content="Che cos'è la garanzia infortuni del conducente?"),
            AIMessage(content="È una copertura assicurativa..."),
        ],
    )


def test_followup_is_rewritten_when_reformulate_enabled():
    graph = _graph(reformulate=True)

    state = graph.analyze_and_rewrite_query_node(_followup_state())

    graph._rewrite_query.assert_called_once()
    assert state.current_query == "REWRITTEN QUERY"


def test_followup_keeps_original_query_when_reformulate_disabled():
    graph = _graph(reformulate=False)

    state = graph.analyze_and_rewrite_query_node(_followup_state())

    # reformulate=false -> the original query reaches the retrieval untouched.
    graph._rewrite_query.assert_not_called()
    assert state.current_query == ORIGINAL_QUERY
    # A genuine follow-up must NOT be downgraded to NEW_QUESTION just because
    # reformulation is off: domain detection stays independent of the flag.
    assert state.domain != ["NEW_QUESTION"]


def test_new_question_sets_domain_regardless_of_reformulate():
    graph = _graph(reformulate=False, classification="NEW_QUESTION")

    state = graph.analyze_and_rewrite_query_node(_followup_state())

    graph._rewrite_query.assert_not_called()
    assert state.domain == ["NEW_QUESTION"]


def test_simple_generate_bypasses_the_node():
    graph = _graph(reformulate=True, rag_type="SIMPLE_GENERATE")

    state = graph.analyze_and_rewrite_query_node(_followup_state())

    # The outer gate short-circuits: no analysis, no rewrite, query untouched.
    graph._rewrite_query.assert_not_called()
    assert state.current_query == ORIGINAL_QUERY
