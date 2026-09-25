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


"""The generate endpoint streams the pipeline output as Server-Sent Events.

Generate is a one-shot search: it never decodes the token nor attaches a chat,
it forwards the request to the pipeline and streams whatever the pipeline
yields.
"""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}

BODY = {
    "searchQuery": [{"tokenType": "TEXT", "values": ["openk9"]}],
    "searchText": "Che cos'è OpenK9?",
    "language": "it_IT",
    "datasourceIds": [7],
}

# Positional slots of get_agentic_rag the handler fills.
RAG_TYPE, SEARCH_QUERY, DATASOURCE_IDS = 0, 1, 2
TOKEN, EXTRA, LANGUAGE, SEARCH_TEXT = 6, 7, 10, 11
CHAT_ID, USER_ID, TENANT_ID = 12, 13, 14
RAG_CONFIGURATION, GUARDRAILS_CONFIGURATION = 19, 21


def _sse_events(response):
    events = []
    for line in response.text.splitlines():
        line = line.strip()
        if line.startswith("data:"):
            events.append(json.loads(line[len("data:") :].strip()))
    return events


@pytest.fixture
def pipeline(monkeypatch):
    def fake_stream(*args, **kwargs):
        return iter(
            [
                json.dumps({"chunk": "", "type": "START"}),
                json.dumps({"chunk": "OpenK9 è", "type": "CHUNK"}),
                json.dumps({"chunk": " un motore di ricerca", "type": "CHUNK"}),
                json.dumps({"chunk": "", "type": "END"}),
            ]
        )

    get_agentic_rag = MagicMock(side_effect=fake_stream)
    get_configurations = MagicMock(
        return_value={
            "rag_configuration": {"guardrails_configuration": {"enabled": True}},
            "llm_configuration": {},
        }
    )
    decode_token = MagicMock()
    monkeypatch.setattr(server, "get_agentic_rag", get_agentic_rag)
    monkeypatch.setattr(server, "get_configurations", get_configurations)
    monkeypatch.setattr(server, "decode_token", decode_token)
    return get_agentic_rag, get_configurations, decode_token


@pytest.fixture
def client():
    return TestClient(server.app)


def test_generate_streams_the_pipeline_events(client, pipeline):
    response = client.post("/api/rag/generate", json=BODY, headers=HEADERS)

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    assert [event["type"] for event in _sse_events(response)] == [
        "START",
        "CHUNK",
        "CHUNK",
        "END",
    ]


def test_generate_forwards_the_request_to_the_pipeline(client, pipeline):
    get_agentic_rag, get_configurations, _ = pipeline

    client.post("/api/rag/generate", json=BODY, headers=HEADERS)

    get_configurations.assert_called_once_with(
        rag_type="SIMPLE_GENERATE",
        grpc_host=server.GRPC_DATASOURCE_HOST,
        tenant_id="tenant-1",
    )
    args = get_agentic_rag.call_args.args
    assert args[RAG_TYPE] == "SIMPLE_GENERATE"
    assert args[SEARCH_QUERY][0].values == ["openk9"]
    assert args[DATASOURCE_IDS] == [7]
    assert args[LANGUAGE] == "it_IT"
    assert args[SEARCH_TEXT] == "Che cos'è OpenK9?"
    assert args[TENANT_ID] == "tenant-1"
    assert args[GUARDRAILS_CONFIGURATION] == {"enabled": True}


def test_generate_strips_the_bearer_prefix_and_never_decodes_the_token(
    client, pipeline
):
    get_agentic_rag, _, decode_token = pipeline

    client.post("/api/rag/generate", json=BODY, headers=HEADERS)

    args = get_agentic_rag.call_args.args
    assert args[TOKEN] == "fake-token"
    assert args[CHAT_ID] is None
    assert args[USER_ID] is None
    decode_token.assert_not_called()


def test_generate_without_a_token_is_anonymous(client, pipeline):
    get_agentic_rag, _, _ = pipeline

    response = client.post(
        "/api/rag/generate", json=BODY, headers={"x-tenant-id": "tenant-1"}
    )

    assert response.status_code == 200
    assert get_agentic_rag.call_args.args[TOKEN] is None


def test_generate_adds_the_acl_header_to_the_extra_filters(client, pipeline):
    get_agentic_rag, _, _ = pipeline

    client.post(
        "/api/rag/generate",
        json={**BODY, "extra": {"filter": ["news"]}},
        headers=[
            *HEADERS.items(),
            ("openk9-acl", "group:admins"),
            ("openk9-acl", "project:openk9"),
        ],
    )

    assert get_agentic_rag.call_args.args[EXTRA] == {
        "filter": ["news"],
        "OPENK9_ACL": ["group:admins", "project:openk9"],
    }


def test_generate_without_acl_leaves_the_extra_filters_untouched(client, pipeline):
    get_agentic_rag, _, _ = pipeline

    client.post("/api/rag/generate", json=BODY, headers=HEADERS)

    assert get_agentic_rag.call_args.args[EXTRA] == {}


def test_generate_without_tenant_is_refused(client, pipeline):
    get_agentic_rag, _, _ = pipeline

    response = client.post(
        "/api/rag/generate",
        json=BODY,
        headers={"authorization": "Bearer fake-token"},
    )

    assert response.status_code == 400
    assert response.json()["detail"] == "Missing x_tenant_id header."
    get_agentic_rag.assert_not_called()


def test_generate_without_search_query_is_invalid(client, pipeline):
    get_agentic_rag, _, _ = pipeline

    response = client.post(
        "/api/rag/generate",
        json={"searchText": "Che cos'è OpenK9?"},
        headers=HEADERS,
    )

    assert response.status_code == 422
    get_agentic_rag.assert_not_called()
