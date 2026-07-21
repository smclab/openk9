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
from types import ModuleType, SimpleNamespace
from unittest.mock import MagicMock

from app.utils.conversation_history import (
    load_messages_from_frontend,
    load_messages_from_snapshot,
)


class _Message:
    """Minimal stand-in for a langchain message (content + role type)."""

    def __init__(self, type_, content):
        self.type = type_
        self.content = content

    def __eq__(self, other):
        return (
            isinstance(other, _Message)
            and self.type == other.type
            and self.content == other.content
        )


def _graph_with_messages(messages):
    graph = MagicMock()
    graph.get_state.return_value = SimpleNamespace(values={"messages": messages})
    return graph


def test_returns_messages_in_chronological_order():
    messages = [
        _Message("human", "turn 1 question"),
        _Message("ai", "turn 1 answer"),
        _Message("human", "turn 2 question"),
        _Message("ai", "turn 2 answer"),
    ]
    config = {"configurable": {"thread_id": "chat-1"}}
    graph = _graph_with_messages(messages)

    result = load_messages_from_snapshot(graph, config)

    graph.get_state.assert_called_once_with(config)
    # Same list, same order: no reordering, no dropped turn.
    assert result == messages
    # The latest human/ai turn must be the most recent one (used as previous_query).
    last_human = next(m for m in reversed(result) if m.type == "human")
    last_ai = next(m for m in reversed(result) if m.type == "ai")
    assert last_human.content == "turn 2 question"
    assert last_ai.content == "turn 2 answer"


def test_returns_empty_list_when_no_messages_in_snapshot():
    graph = MagicMock()
    graph.get_state.return_value = SimpleNamespace(values={})

    assert load_messages_from_snapshot(graph, {}) == []


def test_returns_empty_list_when_get_state_raises():
    graph = MagicMock()
    graph.get_state.side_effect = RuntimeError("opensearch unavailable")

    assert load_messages_from_snapshot(graph, {}) == []


def _stub_langchain_messages(monkeypatch):
    """Provide a minimal langchain_core.messages so the lazy import resolves."""
    module = ModuleType("langchain_core.messages")
    module.HumanMessage = lambda content: _Message("human", content)
    module.AIMessage = lambda content: _Message("ai", content)
    monkeypatch.setitem(sys.modules, "langchain_core", ModuleType("langchain_core"))
    monkeypatch.setitem(sys.modules, "langchain_core.messages", module)


def test_frontend_history_is_built_in_order(monkeypatch):
    _stub_langchain_messages(monkeypatch)
    chat_history = [
        {"question": "turn 1 question", "answer": "turn 1 answer"},
        {"question": "turn 2 question", "answer": "turn 2 answer"},
    ]

    result = load_messages_from_frontend(chat_history)

    assert result == [
        _Message("human", "turn 1 question"),
        _Message("ai", "turn 1 answer"),
        _Message("human", "turn 2 question"),
        _Message("ai", "turn 2 answer"),
    ]


def test_frontend_history_degrades_on_malformed_item(monkeypatch):
    _stub_langchain_messages(monkeypatch)
    # Missing "answer" key on the second item: the loader logs and returns what
    # it built so far instead of crashing.
    chat_history = [
        {"question": "turn 1 question", "answer": "turn 1 answer"},
        {"question": "turn 2 question"},
    ]

    result = load_messages_from_frontend(chat_history)

    assert result == [
        _Message("human", "turn 1 question"),
        _Message("ai", "turn 1 answer"),
        _Message("human", "turn 2 question"),
    ]
