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

from app.rag import agentic_rag
from app.rag.agentic_rag import GraphState, RagGraph


def _fake_self(monkeypatch, *, sequence_number, generated_title="GENERATED TITLE"):
    """Build a minimal RagGraph-like object for history_saver_node; returns it
    with the spy standing in for generate_conversation_title."""
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
    generate = MagicMock(return_value=generated_title)
    monkeypatch.setattr(agentic_rag, "generate_conversation_title", generate)
    return fake, generate


def test_title_is_generated_on_first_turn(monkeypatch):
    state = GraphState(current_query="Che cos'e' la garanzia?", response="...")
    fake, generate = _fake_self(
        monkeypatch, sequence_number=1, generated_title='"Garanzia legale"'
    )

    result = RagGraph.history_saver_node(fake, state)

    generate.assert_called_once_with(fake.llm, "Che cos'e' la garanzia?", "...")
    assert result.conversation_title == "Garanzia legale"


def test_title_is_preserved_on_later_turns(monkeypatch):
    # Turn 2: the channel was restored from the checkpoint with the turn-1 title.
    state = GraphState(
        current_query="E per i prodotti usati?",
        response="...",
        conversation_title="Garanzia legale",
    )
    fake, generate = _fake_self(monkeypatch, sequence_number=2)

    result = RagGraph.history_saver_node(fake, state)

    # Must NOT be reset to "" on a later turn.
    generate.assert_not_called()
    assert result.conversation_title == "Garanzia legale"


if __name__ == "__main__":
    sys.exit(pytest.main([__file__, "-v"]))
