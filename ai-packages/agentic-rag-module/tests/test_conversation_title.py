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


import sys
from types import SimpleNamespace
from unittest.mock import MagicMock

import pytest

# agentic_rag pulls in a large third-party import graph (langgraph, opensearch,
# phoenix, ...). We only need the pure logic of history_saver_node, so stub the
# heavy modules before importing, mirroring the approach in conftest.py.
_HEAVY = [
    "app.external_services",
    "app.external_services.grpc",
    "app.external_services.grpc.grpc_client",
    "app.models",
    "app.models.models",
    "app.rag.retrievers",
    "app.rag.retrievers.domain_documents_retriever",
    "app.rag.retrievers.guardrail_documents_retriever",
    "app.rag.retrievers.retriever",
    "app.rag.retrievers.uploaded_documents_retriever",
    "app.utils.authentication",
    "app.utils.conversation_history",
    "app.utils.guardrails",
    "app.utils.logger",
    "IPython",
    "IPython.display",
    "langchain_core",
    "langchain_core.documents",
    "langchain_core.messages",
    "langchain_core.output_parsers",
    "langchain_core.output_parsers.string",
    "langchain_core.prompts",
    "langgraph",
    "langgraph.checkpoint",
    "langgraph.checkpoint.memory",
    "langgraph.checkpoint.opensearch",
    "langgraph.graph",
    "langgraph.graph.message",
    "opensearchpy",
    "phoenix",
    "phoenix.evals",
]
for _name in _HEAVY:
    sys.modules.setdefault(_name, MagicMock())


class _Msg:
    """Minimal stand-in for langchain HumanMessage / AIMessage."""

    def __init__(self, content):
        self.content = content


sys.modules["langchain_core.messages"].HumanMessage = _Msg
sys.modules["langchain_core.messages"].AIMessage = _Msg
sys.modules["langchain_core.messages"].SystemMessage = _Msg
# Used only as Annotated metadata / as a field type on GraphState.
sys.modules["langgraph.graph.message"].add_messages = object()
sys.modules["langchain_core.documents"].Document = dict

from app.rag.agentic_rag import GraphState, RagGraph  # noqa: E402


def _fake_self(*, sequence_number, generated_title="GENERATED TITLE"):
    """Build a minimal RagGraph-like object for history_saver_node."""
    fake = SimpleNamespace(
        rag_type="AGENTIC",
        configuration={"enable_conversation_title": True},
        chat_sequence_number=sequence_number,
        user_id="user-1",
        chat_id="chat-1",
        llm=MagicMock(),
    )
    # generate_conversation_title is imported into the agentic_rag namespace;
    # patch it there so turn-1 generation is deterministic.
    import app.rag.agentic_rag as mod

    mod.generate_conversation_title = lambda llm, q, r: generated_title
    return fake


def test_title_is_generated_on_first_turn():
    state = GraphState(current_query="Che cos'e' la garanzia?", response="...")
    fake = _fake_self(sequence_number=1, generated_title='"Garanzia legale"')

    result = RagGraph.history_saver_node(fake, state)

    assert result.conversation_title == "Garanzia legale"


def test_title_is_preserved_on_later_turns():
    # Turn 2: the channel was restored from the checkpoint with the turn-1 title.
    state = GraphState(
        current_query="E per i prodotti usati?",
        response="...",
        conversation_title="Garanzia legale",
    )
    fake = _fake_self(sequence_number=2)

    result = RagGraph.history_saver_node(fake, state)

    # Must NOT be reset to "" — this is the bug from issue 2173.
    assert result.conversation_title == "Garanzia legale"


def test_title_stays_preserved_across_three_turns():
    title = "Garanzia legale"
    for seq in (2, 3):
        state = GraphState(
            current_query=f"domanda turno {seq}",
            response="...",
            conversation_title=title,
        )
        result = RagGraph.history_saver_node(_fake_self(sequence_number=seq), state)
        assert result.conversation_title == title


if __name__ == "__main__":
    sys.exit(pytest.main([__file__, "-v"]))
