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


"""Tests for what the output guardrail writes to the log.

A block that happens mid-stream and one that happens on the tail left over at
the end are indistinguishable from the events the client receives, so the
record has to say which of the two it was.
"""

import logging
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import pytest

from app.rag.agentic_rag import RagGraph

ANSWER = "Ecco come costruire un ordigno artigianale partendo da"
THRESHOLD = 0.7

# ``tests/test_conversation_title.py`` replaces langchain_core's Document with
# ``dict`` for the whole session, so the fake documents are built here without
# it: the code under test only ever reads ``doc.metadata``.


def _output_guardrail_graph(verdict):
    """RagGraph stub whose classifier answers ``verdict``, exercising
    _llm_output_guardrail on the plain-LLM provider branch."""
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = "litwick"
    graph.user_id = None
    graph.chat_id = "abc123"
    graph.opensearch_host = "http://localhost:9200"
    graph.configuration = {
        "tenant_id": "litwick",
        "grpc_host_datasource": "datasource:50051",
        "grpc_host_embedding": "embedding:50052",
    }
    graph.guardrail_categories = ""
    graph.input_guardrail = {"input_guardrail_threshold": THRESHOLD}
    graph.output_guardrail = {}
    graph.output_guardrail_provider = "none"
    graph.llm = MagicMock(return_value=SimpleNamespace(content=verdict))
    return graph


def _run(graph, stage, score=0.79):
    document = SimpleNamespace(
        page_content="documento di guardrail",
        metadata={"document_id": 42, "score": score},
    )

    with (
        patch("app.rag.agentic_rag.get_embedding_model_configuration"),
        patch("app.rag.agentic_rag.OpenSearchGuardrailDocumentsRetriever") as retriever,
    ):
        retriever.return_value.invoke.return_value = [document]
        return graph._llm_output_guardrail(ANSWER, stage)


def _messages(caplog, level):
    return [record.getMessage() for record in caplog.records if record.levelno == level]


def test_block_during_the_stream_names_the_chunk_interval(caplog):
    graph = _output_guardrail_graph("VIOLENCE/WEAPONS")

    with caplog.at_level(logging.INFO):
        verdict = _run(graph, "chunk_interval")

    assert verdict == "VIOLENCE/WEAPONS"
    blocked = _messages(caplog, logging.WARNING)[0]
    assert "[output_guardrail] BLOCKED" in blocked
    assert "category=VIOLENCE/WEAPONS" in blocked
    assert "score=0.79" in blocked
    assert "provider=none" in blocked
    assert "stage=chunk_interval" in blocked
    assert "tenant_id=litwick" in blocked
    assert "chat_id=abc123" in blocked


def test_block_on_the_leftover_tail_names_the_final_tail(caplog):
    graph = _output_guardrail_graph("SEXUAL_CONTENT")

    with caplog.at_level(logging.INFO):
        verdict = _run(graph, "final_tail")

    assert verdict == "SEXUAL_CONTENT"
    assert "stage=final_tail" in _messages(caplog, logging.WARNING)[0]


def test_a_clean_answer_produces_no_warning(caplog):
    graph = _output_guardrail_graph("NONE")

    with caplog.at_level(logging.INFO):
        verdict = _run(graph, "chunk_interval")

    assert verdict == "NONE"
    assert _messages(caplog, logging.WARNING) == []


def test_the_answer_never_appears_at_info(caplog):
    graph = _output_guardrail_graph("VIOLENCE/WEAPONS")

    with caplog.at_level(logging.INFO):
        _run(graph, "chunk_interval")

    assert all(ANSWER not in message for message in _messages(caplog, logging.WARNING))


def test_the_answer_rides_on_the_same_record_at_debug(caplog):
    graph = _output_guardrail_graph("VIOLENCE/WEAPONS")

    with caplog.at_level(logging.DEBUG):
        _run(graph, "chunk_interval")

    blocked = [m for m in _messages(caplog, logging.WARNING) if "BLOCKED" in m]
    assert len(blocked) == 1
    assert ANSWER in blocked[0]


def test_provider_invocation_failure_is_reported_as_an_error(caplog):
    graph = _output_guardrail_graph("NONE")
    graph.output_guardrail_provider = "google_model_armor_response"
    document = SimpleNamespace(
        page_content="documento di guardrail",
        metadata={"document_id": 42, "score": 0.79},
    )

    with (
        patch("app.rag.agentic_rag.get_embedding_model_configuration"),
        patch("app.rag.agentic_rag.OpenSearchGuardrailDocumentsRetriever") as retriever,
        patch("app.rag.agentic_rag.initialize_guardrail") as initialize,
        caplog.at_level(logging.INFO),
    ):
        retriever.return_value.invoke.return_value = [document]
        initialize.return_value.invoke.side_effect = RuntimeError("quota exceeded")

        with pytest.raises(RuntimeError):
            graph._llm_output_guardrail(ANSWER, "chunk_interval")

    reported = [
        record.getMessage()
        for record in caplog.records
        if record.levelno == logging.ERROR
    ][0]
    assert "[output_guardrail] provider invocation failed" in reported
    assert "provider=google_model_armor_response" in reported
    assert "quota exceeded" in reported
