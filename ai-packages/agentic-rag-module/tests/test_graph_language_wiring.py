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


"""Where the answer language is resolved in the graph.

In the chat graph it comes right after the input guardrail lets the input
through, so a blocked input never reaches it; the real-time evaluation graph,
which has no guardrail, starts from it.
"""

from unittest.mock import MagicMock, patch

from langgraph.checkpoint.memory import InMemorySaver

from app.rag import agentic_rag
from app.rag.agentic_rag import RagGraph


def _edges(**overrides):
    configuration = {
        "rag_type": "CHAT_RAG",
        "tenant_id": "tenant-1",
        "user_id": None,
        "chat_id": None,
        "chat_sequence_number": 1,
        "opensearch_host": "http://localhost:9200",
        "guardrails_configuration": {
            "input_guardrail": {},
            "output_guardrail": {},
            "guardrail_categories": [],
        },
    }
    configuration.update(overrides)

    with (
        patch.object(agentic_rag, "get_opensearch_client"),
        patch.object(
            agentic_rag, "OpenSearchSaver", MagicMock(return_value=InMemorySaver())
        ),
    ):
        graph = RagGraph(MagicMock(), configuration)

    drawable = graph.graph.get_graph()
    return {(edge.source, edge.target) for edge in drawable.edges}


def test_chat_graph_resolves_the_language_after_the_input_guardrail():
    edges = _edges()

    assert {source for source, target in edges if target == "resolve_language"} == {
        "input_guardrail"
    }
    assert ("resolve_language", "history_handler") in edges
    assert ("input_guardrail", "guardrail_violation_response") in edges
    assert ("input_guardrail", "history_handler") not in edges


def test_real_time_evaluation_graph_starts_from_the_language():
    edges = _edges(enable_real_time_evaluation=True)

    assert {source for source, target in edges if target == "resolve_language"} == {
        "__start__"
    }
    assert ("resolve_language", "history_handler") in edges
    assert ("__start__", "history_handler") not in edges
