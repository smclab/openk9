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

"""Which traced spans the offline evaluation judges, and with what.

evaluations() reads the spans of the Phoenix project and judges only the root
span of each chat turn, whose output carries the final graph state. Each
evaluation runs when it was requested and the state has what it needs: a
response for the response judge, a retrieved context for the retriever judge.
"""

import json
from unittest.mock import MagicMock

import pytest

from app.rag import evaluations

LLM_CONFIGURATION = {"model_type": "openai"}
RAG_CONFIGURATION = {"rag_tool_description": "tool description"}

TURN = {
    "current_query": "Quali sono i massimali?",
    "context": [{"page_content": "100.000 euro", "metadata": {}}],
    "response": "Il massimale è di 100.000 euro.",
    "use_rag": True,
}


def _span(span_id, output_value, parent_id=None):
    return {
        "context": {"span_id": span_id},
        "parent_id": parent_id,
        "attributes": {"output.value": output_value},
    }


@pytest.fixture
def judges(monkeypatch):
    """Replace the three judges and the Phoenix client with spies."""
    spies = {
        "response": MagicMock(),
        "retriever": MagicMock(),
        "rag_router": MagicMock(),
    }
    client = MagicMock()

    monkeypatch.setattr(evaluations, "initialize_language_model", lambda _c: "llm")
    monkeypatch.setattr(evaluations, "Client", lambda base_url: client)
    monkeypatch.setattr(evaluations, "response_evaluation", spies["response"])
    monkeypatch.setattr(evaluations, "retriever_evaluation", spies["retriever"])
    monkeypatch.setattr(evaluations, "rag_router_evaluation", spies["rag_router"])

    spies["client"] = client
    return spies


def _run(judges, spans, **flags):
    judges["client"].spans.get_spans.return_value = spans
    requested = {
        "evaluate_rag_router": True,
        "evaluate_retriever": True,
        "evaluate_response": True,
        **flags,
    }
    return evaluations.evaluations(
        RAG_CONFIGURATION,
        LLM_CONFIGURATION,
        "project",
        "http://localhost:6006",
        10,
        None,
        None,
        **requested,
    )


def test_root_span_is_judged_by_every_requested_evaluation(judges):
    evaluated = _run(judges, [_span("root", json.dumps(TURN))])

    judges["response"].assert_called_once_with(
        "llm",
        judges["client"],
        "root",
        TURN["current_query"],
        TURN["response"],
        "openai",
    )
    judges["retriever"].assert_called_once_with(
        "llm",
        judges["client"],
        "root",
        TURN["current_query"],
        TURN["context"],
        "openai",
    )
    judges["rag_router"].assert_called_once_with(
        "llm",
        judges["client"],
        "root",
        TURN["current_query"],
        True,
        "tool description",
    )
    assert evaluated == {"root"}


def test_child_spans_and_incorrect_outputs_are_skipped(judges):
    evaluated = _run(
        judges,
        [
            _span("child", json.dumps(TURN), parent_id="root"),
            _span("broken", "incorrect"),
        ],
    )

    judges["response"].assert_not_called()
    judges["retriever"].assert_not_called()
    judges["rag_router"].assert_not_called()
    assert evaluated == set()


def test_only_the_requested_evaluations_run(judges):
    evaluated = _run(
        judges,
        [_span("root", json.dumps(TURN))],
        evaluate_rag_router=False,
        evaluate_retriever=False,
    )

    judges["response"].assert_called_once()
    judges["retriever"].assert_not_called()
    judges["rag_router"].assert_not_called()
    assert evaluated == {"root"}


def test_turn_without_context_is_not_judged_on_retrieval(judges):
    turn = {**TURN, "context": [], "use_rag": False}

    _run(judges, [_span("root", json.dumps(turn))])

    judges["retriever"].assert_not_called()
    judges["response"].assert_called_once()


def test_turn_without_response_is_not_judged_on_the_answer(judges):
    turn = {**TURN, "response": ""}

    _run(judges, [_span("root", json.dumps(turn))])

    judges["response"].assert_not_called()
    judges["retriever"].assert_called_once()
