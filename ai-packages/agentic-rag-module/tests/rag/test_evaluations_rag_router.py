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

"""Evaluation of the RAG router decision.

The judge LLM reads the question, the tool definitions and the tool the router
called (rag_tool when retrieval ran, none otherwise) and returns a verdict. The
offline evaluation stores it as a span annotation; the real-time graph keeps it
in the state.
"""

import logging
from unittest.mock import MagicMock

from langchain_core.messages import AIMessage
from langchain_core.runnables import RunnableLambda

from app.rag.agentic_rag import GraphState, RagGraph
from app.rag.evaluations import rag_router_evaluation

QUERY = "Quali sono i massimali della polizza?"
TOOL_DESCRIPTION = "rag_tool: searches the insurance documents"


def _judge(verdict="correct"):
    """Judge LLM returning a fixed verdict and recording the prompts it read."""
    prompts = []

    def judge(prompt_value):
        prompts.append(prompt_value.to_string())
        return AIMessage(content=verdict)

    return RunnableLambda(judge), prompts


def test_verdict_is_stored_as_a_span_annotation():
    llm, _ = _judge("correct")
    client = MagicMock()

    rag_router_evaluation(llm, client, "span-1", QUERY, True, TOOL_DESCRIPTION)

    client.annotations.add_span_annotation.assert_called_once_with(
        annotation_name="evaluate_rag_router",
        annotator_kind="HUMAN",
        span_id="span-1",
        label="correct",
    )


def test_judge_reads_the_question_the_tools_and_the_call():
    llm, prompts = _judge()

    rag_router_evaluation(llm, MagicMock(), "span-1", QUERY, True, TOOL_DESCRIPTION)

    assert QUERY in prompts[0]
    assert TOOL_DESCRIPTION in prompts[0]
    assert "rag_tool" in prompts[0].replace(TOOL_DESCRIPTION, "")


def test_no_tool_call_when_retrieval_did_not_run():
    llm, prompts = _judge()

    rag_router_evaluation(llm, MagicMock(), "span-1", QUERY, False, TOOL_DESCRIPTION)

    assert "rag_tool" not in prompts[0].replace(TOOL_DESCRIPTION, "")


def test_judge_failure_is_logged_not_raised(caplog):
    llm = RunnableLambda(lambda _prompt: (_ for _ in ()).throw(RuntimeError("down")))
    client = MagicMock()

    with caplog.at_level(logging.ERROR):
        rag_router_evaluation(llm, client, "span-1", QUERY, True, TOOL_DESCRIPTION)

    client.annotations.add_span_annotation.assert_not_called()
    assert any("down" in record.getMessage() for record in caplog.records)


def test_real_time_router_evaluation_is_kept_in_the_state():
    llm, prompts = _judge("incorrect")
    graph = RagGraph.__new__(RagGraph)
    graph.llm = llm
    graph.configuration = {"rag_tool_description": TOOL_DESCRIPTION}

    state = graph.rag_router_evaluation_node(
        GraphState(current_query=QUERY, use_rag=True)
    )

    assert state.rag_router_evaluation == "incorrect"
    assert QUERY in prompts[0]
