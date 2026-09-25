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

"""Conversation history: where it is loaded from and how it is rendered.

history_handler_node reads the previous turns from the checkpoint of a logged
chat and from the history sent by the frontend for an anonymous one; the first
turn and SIMPLE_GENERATE start empty. The two formatters turn those messages
into the text the analysis and routing prompts read, and history_saver_node
appends the turn that just ended.
"""

from types import SimpleNamespace
from unittest.mock import MagicMock, patch

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage

from app.rag import agentic_rag
from app.rag.agentic_rag import GraphState, RagGraph

MESSAGES = [
    SystemMessage(content="system prompt"),
    HumanMessage(content="Che cos'è la garanzia?"),
    AIMessage(content="È la tutela del consumatore."),
]


def _graph(*, rag_type="AGENTIC", sequence_number=2, user_id=None, chat_id=None):
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = rag_type
    graph.chat_sequence_number = sequence_number
    graph.user_id = user_id
    graph.chat_id = chat_id
    graph.tenant_id = None
    graph.chat_history = [{"question": "q", "answer": "a"}]
    graph.retrieve_from_uploaded_documents = True
    graph.configuration = {}
    graph.config = {}
    graph.graph = MagicMock()
    graph.graph.get_state_history.return_value = []
    graph.llm = MagicMock()
    return graph


def _run_handler(graph):
    with patch.object(
        agentic_rag, "load_messages_from_snapshot", return_value=["snapshot"]
    ) as snapshot, patch.object(
        agentic_rag, "load_messages_from_frontend", return_value=["frontend"]
    ) as frontend:
        state = graph.history_handler_node(GraphState())

    return state, snapshot, frontend


def test_logged_follow_up_reads_the_history_from_the_checkpoint():
    graph = _graph(user_id="user-1", chat_id="chat-1")

    state, snapshot, frontend = _run_handler(graph)

    snapshot.assert_called_once_with(graph.graph, graph.config)
    frontend.assert_not_called()
    assert state.messages == ["snapshot"]


def test_anonymous_follow_up_reads_the_history_sent_by_the_frontend():
    graph = _graph()

    state, snapshot, frontend = _run_handler(graph)

    frontend.assert_called_once_with(graph.chat_history)
    snapshot.assert_not_called()
    assert state.messages == ["frontend"]


def test_handler_restores_the_domain_from_the_checkpoint():
    graph = _graph(user_id="user-1", chat_id="chat-1")
    graph.graph.get_state_history.return_value = [
        SimpleNamespace(values={"domain": ["insurance"]})
    ]

    state, _, _ = _run_handler(graph)

    assert state.domain == ["insurance"]


def test_first_turn_starts_without_history():
    graph = _graph(sequence_number=1, user_id="user-1", chat_id="chat-1")

    state, snapshot, frontend = _run_handler(graph)

    snapshot.assert_not_called()
    frontend.assert_not_called()
    assert state.messages == []
    assert state.retrieve_from_uploaded_documents is True


def test_simple_generate_ignores_the_history():
    graph = _graph(rag_type="SIMPLE_GENERATE", user_id="user-1", chat_id="chat-1")

    state, snapshot, frontend = _run_handler(graph)

    snapshot.assert_not_called()
    frontend.assert_not_called()
    assert state.messages == []


def test_format_conversation_history_keeps_only_user_and_assistant_turns():
    graph = _graph()

    history = graph._format_conversation_history(MESSAGES)

    assert history == (
        "User: Che cos'è la garanzia?\nAssistant: È la tutela del consumatore."
    )


def test_format_conversation_history_without_turns():
    graph = _graph()

    assert graph._format_conversation_history([]) == "No previous conversation"
    assert (
        graph._format_conversation_history([SystemMessage(content="system")])
        == "No previous conversation"
    )


def test_conversation_context_keeps_only_user_and_assistant_turns():
    graph = _graph()

    context = graph._get_conversation_context(MESSAGES)

    assert context == (
        "User: Che cos'è la garanzia?\nAssistant: È la tutela del consumatore."
    )


def test_conversation_context_without_turns():
    graph = _graph()

    assert graph._get_conversation_context([]) == "No previous context"


def test_domain_is_read_from_the_checkpoint():
    graph = _graph()
    graph.graph.get_state_history.return_value = [
        SimpleNamespace(values={"domain": ["insurance"]})
    ]

    assert graph._load_domain_from_checkpoints() == ["insurance"]


def test_domain_is_none_without_checkpoints():
    graph = _graph()

    assert graph._load_domain_from_checkpoints() is None


def test_checkpoint_failure_leaves_the_domain_empty():
    graph = _graph()
    graph.graph.get_state_history.side_effect = RuntimeError("opensearch down")

    assert graph._load_domain_from_checkpoints() is None


def test_history_saver_appends_the_turn():
    graph = _graph()
    state = GraphState(
        current_query="E per i prodotti usati?", response="Vale un anno.", messages=[]
    )

    result = graph.history_saver_node(state)

    assert [type(message) for message in result.messages] == [HumanMessage, AIMessage]
    assert result.messages[0].content == "E per i prodotti usati?"
    assert result.messages[1].content == "Vale un anno."


def test_history_saver_skips_simple_generate():
    graph = _graph(rag_type="SIMPLE_GENERATE")
    state = GraphState(current_query="domanda", response="risposta", messages=[])

    result = graph.history_saver_node(state)

    assert result.messages == []


def test_first_turn_without_chat_uses_the_query_as_title():
    # No chat to save the title against: even with titles enabled there is no
    # LLM call, the query is the title.
    graph = _graph(sequence_number=1, user_id="user-1")
    graph.configuration = {"enable_conversation_title": True}
    state = GraphState(current_query='"Garanzia legale"', response="...", messages=[])

    with patch.object(agentic_rag, "generate_conversation_title") as generate:
        result = graph.history_saver_node(state)

    generate.assert_not_called()
    assert result.conversation_title == "Garanzia legale"
