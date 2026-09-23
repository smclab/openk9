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


"""Tests for the remaining branch points of the graph.

Every conditional edge now leaves a record, and so does the domain the
retrieval is filtered by: a wrong domain returns documents about the wrong
subject, which from outside is indistinguishable from an empty index.
"""

import logging
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

from app.rag.agentic_rag import GraphState, RagGraph

# ``tests/test_conversation_title.py`` replaces langchain_core's Document and
# message classes for the whole session. The fakes below are built from what
# the module under test actually holds, so the node's isinstance checks and
# metadata reads keep working whatever that file did.


def _graph():
    graph = RagGraph.__new__(RagGraph)
    graph.tenant_id = "litwick"
    graph.user_id = None
    graph.chat_id = "abc123"
    return graph


def _messages(caplog, level):
    return [record.getMessage() for record in caplog.records if record.levelno == level]


def _info(caplog, marker):
    records = [m for m in _messages(caplog, logging.INFO) if marker in m]
    assert len(records) == 1, records
    return records[0]


def test_blocked_input_reports_where_it_is_routed(caplog):
    graph = _graph()

    with caplog.at_level(logging.INFO):
        destination = graph.input_guardrail_route_decision(
            GraphState(current_query="q", guardrail_check=True)
        )

    assert destination == "guardrail_violation_response"
    record = _info(caplog, "[input_guardrail_route]")
    assert "guardrail_check=True -> guardrail_violation_response" in record
    assert "tenant_id=litwick" in record


def test_clean_input_reports_where_it_is_routed(caplog):
    graph = _graph()

    with caplog.at_level(logging.INFO):
        destination = graph.input_guardrail_route_decision(
            GraphState(current_query="q", guardrail_check=False)
        )

    assert destination == "history_handler"
    assert "guardrail_check=False -> history_handler" in _info(
        caplog, "[input_guardrail_route]"
    )


def _domain_graph(threshold=0.5):
    graph = _graph()
    graph.opensearch_host = "http://localhost:9200"
    graph.configuration = {
        "domain_threshold": threshold,
        "tenant_id": "litwick",
        "grpc_host_datasource": "datasource:50051",
        "grpc_host_embedding": "embedding:50052",
    }
    return graph


def _run_domain_node(graph, documents, configured_domains):
    """Run the node with the domain retriever mocked: the configured domains
    are the labels the index carries, not a configuration key."""
    state = GraphState(current_query="Quali corsi avete?")

    with (
        patch("app.rag.agentic_rag.get_embedding_model_configuration"),
        patch("app.rag.agentic_rag.OpenSearchDomainDocumentsRetriever") as retriever,
    ):
        retriever.return_value.get_domains.return_value = configured_domains
        retriever.return_value.invoke.return_value = documents
        return graph.input_domain_node(state)


def _document(domain, score):
    return SimpleNamespace(
        page_content="chunk", metadata={"domain": domain, "score": score}
    )


def test_domain_resolved_by_threshold_is_reported(caplog):
    graph = _domain_graph()

    with caplog.at_level(logging.INFO):
        state = _run_domain_node(graph, [_document("corsi", 0.8)], ["corsi", "news"])

    assert state.domain == ["corsi"]
    record = _info(caplog, "[input_domain]")
    assert "domain=['corsi']" in record
    assert "source=threshold" in record


def test_domain_resolved_by_the_model_is_reported(caplog):
    graph = _domain_graph()
    graph._llm_input_domain = MagicMock(return_value=MagicMock(domain=["news"]))

    with caplog.at_level(logging.INFO):
        state = _run_domain_node(graph, [_document("corsi", 0.1)], ["corsi", "news"])

    assert state.domain == ["news"]
    record = _info(caplog, "[input_domain]")
    assert "domain=['news']" in record
    assert "source=llm" in record


def test_no_configured_domain_is_reported(caplog):
    graph = _domain_graph()

    with caplog.at_level(logging.INFO):
        state = _run_domain_node(graph, [], [])

    assert state.domain is None
    record = _info(caplog, "[input_domain]")
    assert "domain=None" in record
    assert "source=not_configured" in record


def test_the_resolved_domain_is_reported_once_at_debug(caplog):
    # Regression guard: the outcome record must not be doubled by a DEBUG line
    # restating it. One event, one record, whatever the level in force.
    graph = _domain_graph()

    with caplog.at_level(logging.DEBUG, logger="app"):
        _run_domain_node(graph, [_document("corsi", 0.8)], ["corsi", "news"])

    outcome_records = [
        record.getMessage()
        for record in caplog.records
        if "[input_domain]" in record.getMessage() and "source=" in record.getMessage()
    ]
    assert len(outcome_records) == 1, outcome_records
