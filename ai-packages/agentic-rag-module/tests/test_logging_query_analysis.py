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


"""Tests for what the query analysis writes to the log.

The node decides whether a turn is a follow-up and, when it is, whether the
query is rewritten before retrieval. Both are invisible from outside: a
follow-up left unrewritten reaches the retriever as the user typed it,
without the entity the previous turn carried, and the answer that comes back
looks like a retrieval problem.
"""

import logging
from types import SimpleNamespace
from unittest.mock import MagicMock

from app.rag.agentic_rag import AIMessage, GraphState, HumanMessage, RagGraph

# ``tests/test_conversation_title.py`` replaces langchain_core's Document and
# message classes for the whole session. The fakes below are built from what
# the module under test actually holds, so the node's isinstance checks and
# metadata reads keep working whatever that file did.

ORIGINAL_QUERY = "e per il secondo anno?"
REWRITTEN_QUERY = "Qual è il massimale della garanzia infortuni per il secondo anno?"


def _graph(reformulate=True, classification="FOLLOW_UP"):
    graph = RagGraph.__new__(RagGraph)
    graph.rag_type = "CHAT_RAG"
    graph.tenant_id = "litwick"
    graph.user_id = None
    graph.chat_id = "abc123"
    graph.chat_sequence_number = 2
    graph.reformulate = reformulate
    graph.configuration = {}
    graph.utility_llm = MagicMock()
    graph.utility_llm.with_structured_output.return_value = lambda _prompt_value: (
        SimpleNamespace(response=SimpleNamespace(value=classification))
    )
    graph._rewrite_query = MagicMock(return_value=REWRITTEN_QUERY)
    return graph


def _state(with_previous_exchange=True):
    messages = (
        [
            HumanMessage(content="Qual è il massimale della garanzia infortuni?"),
            AIMessage(content="Il massimale è di 100.000 euro."),
        ]
        if with_previous_exchange
        else []
    )
    return GraphState(current_query=ORIGINAL_QUERY, messages=messages)


def _messages(caplog, level):
    return [record.getMessage() for record in caplog.records if record.levelno == level]


def _analyze_record(caplog):
    records = [m for m in _messages(caplog, logging.INFO) if "[analyze_query]" in m]
    assert len(records) == 1, records
    return records[0]


def test_rewritten_followup_says_so(caplog):
    graph = _graph(reformulate=True)

    with caplog.at_level(logging.INFO):
        state = graph.analyze_and_rewrite_query_node(_state())

    assert state.current_query == REWRITTEN_QUERY
    record = _analyze_record(caplog)
    assert "decision=FOLLOW_UP" in record
    assert "action=rewritten" in record
    assert "tenant_id=litwick" in record
    assert "chat_id=abc123" in record


def test_followup_left_untouched_says_so(caplog):
    # reformulate disabled: the turn is a follow-up and the query reaches the
    # retriever as typed. Until now this branch wrote nothing at any level.
    graph = _graph(reformulate=False)

    with caplog.at_level(logging.INFO):
        state = graph.analyze_and_rewrite_query_node(_state())

    assert state.current_query == ORIGINAL_QUERY
    record = _analyze_record(caplog)
    assert "decision=FOLLOW_UP" in record
    assert "action=kept_reformulate_disabled" in record


def test_followup_without_previous_exchange_says_it_was_downgraded(caplog):
    graph = _graph(reformulate=True)

    with caplog.at_level(logging.INFO):
        state = graph.analyze_and_rewrite_query_node(
            _state(with_previous_exchange=False)
        )

    assert state.domain == ["NEW_QUESTION"]
    record = _analyze_record(caplog)
    assert "decision=FOLLOW_UP" in record
    assert "action=downgraded_to_new_question" in record


def test_new_question_is_reported_too(caplog):
    graph = _graph(classification="NEW_QUESTION")

    with caplog.at_level(logging.INFO):
        state = graph.analyze_and_rewrite_query_node(_state())

    assert state.domain == ["NEW_QUESTION"]
    record = _analyze_record(caplog)
    assert "decision=NEW_QUESTION" in record
    assert "action=none" in record


def test_no_query_appears_at_info(caplog):
    graph = _graph(reformulate=True)

    with caplog.at_level(logging.INFO):
        graph.analyze_and_rewrite_query_node(_state())

    record = _analyze_record(caplog)
    assert ORIGINAL_QUERY not in record
    assert REWRITTEN_QUERY not in record
    # The fingerprint is of the query that will actually be retrieved on.
    assert f"query_chars={len(REWRITTEN_QUERY)}" in record


def test_both_queries_ride_on_the_same_record_at_debug(caplog):
    graph = _graph(reformulate=True)

    with caplog.at_level(logging.DEBUG, logger="app"):
        graph.analyze_and_rewrite_query_node(_state())

    record = _analyze_record(caplog)
    assert ORIGINAL_QUERY in record
    assert REWRITTEN_QUERY in record
