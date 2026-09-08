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


import logging
from types import SimpleNamespace
from unittest.mock import MagicMock

from app.rag.evaluations import response_evaluation, retriever_evaluation

QUERY = "Quali sono i massimali della polizza?"
RESPONSE = "Il massimale è di 100.000 euro."
CONTEXT = [
    {
        "page_content": RESPONSE,
        "metadata": {"document_id": "chunk-1"},
    }
]


def _llm(structured_response):
    llm = MagicMock()
    llm.with_structured_output.return_value = lambda _prompt_value: structured_response
    return llm


def _method_of(llm):
    return llm.with_structured_output.call_args.kwargs["method"]


def _clarity_verdict():
    return SimpleNamespace(
        judgment=SimpleNamespace(value="CLEAR"),
        explanation="La risposta indica il massimale.",
        vote=9,
    )


def _relevance_verdicts():
    return SimpleNamespace(
        evaluations=[
            SimpleNamespace(
                chunk_id="chunk-1",
                judgment=SimpleNamespace(value="RELEVANT"),
                explanation="Il testo riporta il massimale richiesto.",
                vote=9,
            )
        ]
    )


def _errors(caplog):
    return [
        record.getMessage()
        for record in caplog.records
        if record.levelno == logging.ERROR
    ]


def test_both_evaluation_chains_follow_the_provider_of_their_own_llm():
    # The evaluation path builds its own LLM outside the graph: it must pick
    # the method from the same tenant provider, or the offline run fails where
    # the chat works.
    ollama_response = _llm(_clarity_verdict())
    ollama_retriever = _llm(_relevance_verdicts())
    openai_response = _llm(_clarity_verdict())
    openai_retriever = _llm(_relevance_verdicts())

    response_evaluation(
        ollama_response, MagicMock(), "span-1", QUERY, RESPONSE, "ollama"
    )
    retriever_evaluation(
        ollama_retriever, MagicMock(), "span-1", QUERY, CONTEXT, "ollama"
    )
    response_evaluation(
        openai_response, MagicMock(), "span-1", QUERY, RESPONSE, "openai"
    )
    retriever_evaluation(
        openai_retriever, MagicMock(), "span-1", QUERY, CONTEXT, "openai"
    )

    assert _method_of(ollama_response) == "json_schema"
    assert _method_of(ollama_retriever) == "json_schema"
    assert _method_of(openai_response) == "function_calling"
    assert _method_of(openai_retriever) == "function_calling"


def test_response_evaluation_reports_the_failure_instead_of_annotating(caplog):
    client = MagicMock()

    with caplog.at_level(logging.ERROR):
        response_evaluation(_llm(None), client, "span-1", QUERY, RESPONSE, "ollama")

    client.annotations.add_span_annotation.assert_not_called()
    assert "schema=ClassificationResponse" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]


def test_retriever_evaluation_reports_the_failure_instead_of_annotating(caplog):
    client = MagicMock()

    with caplog.at_level(logging.ERROR):
        retriever_evaluation(_llm(None), client, "span-1", QUERY, CONTEXT, "ollama")

    client.annotations.add_span_annotation.assert_not_called()
    assert "schema=RetrieverEvaluationResponseList" in _errors(caplog)[0]
    assert "method=json_schema" in _errors(caplog)[0]
