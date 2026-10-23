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

"""Until the orchestrated pipeline is wired, every RAG route answers with the
ERROR event of an unexpected failure, so a client stops waiting at once."""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"x-tenant-id": "tenant-1"}

ROUTES = [
    (
        "/api/rag/generate",
        {
            "searchQuery": [{"tokenType": "TEXT", "values": ["openk9"]}],
            "searchText": "Che cos'è OpenK9?",
        },
    ),
    (
        "/api/rag/chat",
        {
            "chatId": "chat-1",
            "searchText": "Che cos'è OpenK9?",
            "timestamp": "1",
            "chatSequenceNumber": 1,
        },
    ),
    (
        "/api/rag/chat-tool",
        {
            "chatId": "chat-1",
            "searchText": "Che cos'è OpenK9?",
            "timestamp": "1",
            "chatSequenceNumber": 1,
        },
    ),
]


@pytest.fixture
def client(monkeypatch):
    get_configurations = MagicMock(
        return_value={"rag_configuration": {}, "llm_configuration": {}}
    )
    monkeypatch.setattr(server, "get_configurations", get_configurations)
    return TestClient(server.app)


def _sse_events(response):
    return [
        json.loads(line.strip()[len("data:") :].strip())
        for line in response.text.splitlines()
        if line.strip().startswith("data:")
    ]


@pytest.mark.parametrize("path, body", ROUTES, ids=["generate", "chat", "chat-tool"])
def test_rag_route_answers_with_a_single_error_event(path, body, client):
    response = client.post(path, json=body, headers=HEADERS)

    assert response.status_code == 200
    assert _sse_events(response) == [
        {"chunk": "RAG pipeline not available yet", "type": "ERROR"}
    ]
