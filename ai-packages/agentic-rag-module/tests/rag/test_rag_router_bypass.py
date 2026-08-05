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

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"


def _graph(bypass_rag, *, routing_decision="RAG"):
    """Build a RagGraph stub exercising rag_router_node in isolation: the
    routing LLM is a spy forced to a fixed decision, so we can assert both the
    decision taken and whether the model was consulted at all."""
    graph = RagGraph.__new__(RagGraph)
    graph.configuration = {
        "bypass_rag": bypass_rag,
        "rag_tool_description": "TOOL DESCRIPTION",
    }
    graph.llm = MagicMock()
    graph.llm.with_structured_output.return_value = (
        lambda _prompt_value: SimpleNamespace(
            response=SimpleNamespace(value=routing_decision)
        )
    )
    return graph


def test_bypass_rag_routes_direct_without_consulting_the_router():
    graph = _graph(bypass_rag=True)

    state = graph.rag_router_node(GraphState(current_query=QUERY, messages=[]))

    # The tenant opted out of retrieval: the node must answer DIRECT on its own.
    # Reading the routing decision outside the branch that produces it raised
    # UnboundLocalError here and made the chat unusable for such tenants.
    graph.llm.with_structured_output.assert_not_called()
    assert state.use_rag is False


def test_router_decides_when_bypass_rag_is_off():
    graph = _graph(bypass_rag=False, routing_decision="RAG")

    state = graph.rag_router_node(GraphState(current_query=QUERY, messages=[]))

    graph.llm.with_structured_output.assert_called_once()
    assert state.use_rag is True


def test_router_direct_answer_skips_retrieval():
    graph = _graph(bypass_rag=False, routing_decision="DIRECT")

    state = graph.rag_router_node(GraphState(current_query=QUERY, messages=[]))

    assert state.use_rag is False
