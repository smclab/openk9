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

"""intent_detection_decision picks whether domain detection runs.

After query analysis the graph either detects the domain of the question or
goes straight to the RAG router. Detection runs when there is no domain yet or
when query analysis marked the turn as a NEW_QUESTION; a domain already known
for the conversation, a SIMPLE_GENERATE tenant and a media-only turn skip it.
"""

from app.rag.agentic_rag import GraphState, RagGraph

QUERY = "Qual è la copertura per i danni da grandine?"


def _graph(rag_type="AGENTIC"):
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = rag_type
    graph.configuration = {}
    return graph


def test_no_domain_yet_runs_domain_detection():
    graph = _graph()

    decision = graph.intent_detection_decision(GraphState(current_query=QUERY))

    assert decision == "input_domain"


def test_new_question_marker_runs_domain_detection_again():
    graph = _graph()
    state = GraphState(current_query=QUERY, domain=["NEW_QUESTION"])

    decision = graph.intent_detection_decision(state)

    assert decision == "input_domain"


def test_known_domain_goes_straight_to_the_router():
    graph = _graph()
    state = GraphState(current_query=QUERY, domain=["insurance"])

    decision = graph.intent_detection_decision(state)

    assert decision == "rag_router"


def test_simple_generate_never_detects_the_domain():
    graph = _graph(rag_type="SIMPLE_GENERATE")

    decision = graph.intent_detection_decision(GraphState(current_query=QUERY))

    assert decision == "rag_router"
