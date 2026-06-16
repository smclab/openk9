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

from app.rag.agentic_rag import GraphState, RagGraph


def test_graph_state_persists_retrieve_from_uploaded_documents():
    assert GraphState().retrieve_from_uploaded_documents is None
    assert (
        GraphState(retrieve_from_uploaded_documents=True).retrieve_from_uploaded_documents
        is True
    )


def test_history_handler_writes_flag_into_state_on_first_turn():
    rag_graph = RagGraph.__new__(RagGraph)
    rag_graph.rag_type = "RAG"
    rag_graph.chat_sequence_number = 1
    rag_graph.retrieve_from_uploaded_documents = True
    rag_graph._load_domain_from_checkpoints = MagicMock(return_value=None)

    state = rag_graph.history_handler_node(GraphState())

    # First turn -> the flag is written into the state so it gets checkpointed.
    assert state.retrieve_from_uploaded_documents is True


def test_history_handler_preserves_flag_on_followup_turn():
    rag_graph = RagGraph.__new__(RagGraph)
    rag_graph.rag_type = "RAG"
    rag_graph.chat_sequence_number = 2
    rag_graph.user_id = "user-1"
    rag_graph.chat_id = "chat-1"
    rag_graph._load_messages_from_checkpoints = MagicMock(return_value=[])
    rag_graph._load_domain_from_checkpoints = MagicMock(return_value=None)

    # On follow-up turns the value resumed from the checkpoint must not be lost.
    state = rag_graph.history_handler_node(
        GraphState(retrieve_from_uploaded_documents=True)
    )

    assert state.retrieve_from_uploaded_documents is True
