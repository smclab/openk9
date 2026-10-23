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

"""A query blocked by the input guardrail gets a fixed answer.

input_guardrail_route_decision sends a flagged query to
guardrail_violation_response_node, which answers with the fixed text stream()
relays to the client as the GUARDRAIL event, without reaching the LLM.
"""

from app.rag.agentic_rag import GraphState, RagGraph


def _graph():
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = None
    graph.user_id = None
    graph.chat_id = None
    return graph


def test_flagged_query_is_routed_to_the_violation_response():
    graph = _graph()
    state = GraphState(current_query="query", guardrail_check=True)

    assert graph.input_guardrail_route_decision(state) == (
        "guardrail_violation_response"
    )


def test_clean_query_goes_on_to_the_history():
    graph = _graph()
    state = GraphState(current_query="query", guardrail_check=False)

    assert graph.input_guardrail_route_decision(state) == "history_handler"


def test_violation_response_is_the_fixed_guardrail_text():
    graph = _graph()
    state = GraphState(
        current_query="query", guardrail_check=True, guardrail_category="violence"
    )

    result = graph.guardrail_violation_response_node(state)

    assert result.response == "Guardrail violation"
    assert result.guardrail_category == "violence"
