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

"""Test di ``datasourceIds`` della request nella query di retrieval.

Port di ``opensearch_retriever_node`` di ``agentic-rag-module``: gli id diventano un token
``tokenType="DATASOURCE"`` con ``filter=True``, **aggiunto** ai token esistenti come quello dominio.

``query_parser`` (gRPC) e il client OpenSearch sono stubbati: nessun servizio esterno.
"""

import asyncio


import pytest
from pydantic_ai.models.test import TestModel

from app.models import models
from app.rag.agentic import retrieval
from app.rag.agentic.contracts import SubAgentRequest
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.discovery import run_discovery


class _StopHere(Exception):
    """Interrompe retrieve_documents subito dopo la costruzione dei token."""


@pytest.fixture
def captured(monkeypatch):
    captured = {}

    def fake_query_parser(**kwargs):
        captured.update(kwargs)
        raise _StopHere

    monkeypatch.setattr(retrieval, "query_parser", fake_query_parser)
    return captured


def _retrieve(**overrides):
    kwargs = {
        "query": "chi e' il RUP?",
        "search_text": "chi e' il RUP?",
        "retrieve_type": "HYBRID",
        "tenant_id": "t1",
        "opensearch_host": "http://os",
        "grpc_host": "ds:9000",
        "context_window": 4096,
        "range_values": [],
    }
    kwargs.update(overrides)
    with pytest.raises(_StopHere):
        retrieval.retrieve_documents(**kwargs)


def test_datasource_token_is_appended(captured):
    _retrieve(datasource_ids=[1, 2], domains=["legal"])

    tokens = captured["search_query"]
    assert [t["tokenType"] for t in tokens] == ["HYBRID", "TEXT", "DATASOURCE"]
    assert tokens[-1] == {
        "entityType": "",
        "entityName": "",
        "tokenType": "DATASOURCE",
        "keywordKey": "",
        "values": ["1", "2"],
        "extra": {},
        "filter": True,
    }


@pytest.mark.parametrize("datasource_ids", [None, []])
def test_without_datasource_ids_the_query_is_unchanged(captured, datasource_ids):
    _retrieve(datasource_ids=datasource_ids)

    assert [t["tokenType"] for t in captured["search_query"]] == ["HYBRID"]


def test_discovery_forwards_the_request_datasource_ids():
    """Il tool ``retriever`` del Discovery passa gli id della request alla funzione di retrieval."""
    seen = {}

    def fake_retrieve(**kwargs):
        seen.update(kwargs)
        return []

    deps = AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        datasource_ids=[7],
        retrieve_fn=fake_retrieve,
    )

    asyncio.run(
        run_discovery(
            request=SubAgentRequest(question="chi e' il RUP?"), deps=deps, model=TestModel()
        )
    )

    assert seen["datasource_ids"] == [7]


@pytest.mark.parametrize("model_class", [models.SearchQuery, models.SearchQueryChat])
def test_request_models_accept_datasource_ids(model_class):
    payload = {
        "searchQuery": [],
        "searchText": "chi e' il RUP?",
        "timestamp": "1731928126578",
        "chatSequenceNumber": 1,
        "datasourceIds": [1, 2],
    }

    assert model_class(**payload).datasourceIds == [1, 2]
