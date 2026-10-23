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

"""A RAG request goes from the route to the orchestrator and back as a stream
of SSE events. The LLM is a test model that answers without delegating, so
neither OpenSearch nor a provider is reached."""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient
from pydantic_ai.models.test import TestModel

import app.server as server
from app.rag.agentic import entrypoint

HEADERS = {"x-tenant-id": "tenant-1"}
ANSWER = "OpenK9 è un motore di ricerca. " * 4

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
        return_value={"rag_configuration": {}, "llm_configuration": {"context_window": 8192}}
    )
    monkeypatch.setattr(server, "get_configurations", get_configurations)
    model = TestModel(
        call_tools=[],
        custom_output_args={"status": "ok", "message": ANSWER, "used_document_ids": []},
    )
    monkeypatch.setattr(entrypoint, "build_model", lambda _configuration: model)
    return TestClient(server.app)


def _events(response):
    return [
        json.loads(line.strip()[len("data:") :].strip())
        for line in response.text.splitlines()
        if line.strip().startswith("data:")
    ]


@pytest.mark.parametrize("path, body", ROUTES, ids=["generate", "chat", "chat-tool"])
def test_the_answer_is_streamed_as_chunks(path, body, client):
    events = _events(client.post(path, json=body, headers=HEADERS))

    types = [event["type"] for event in events]
    phases = [event["chunk"] for event in events if event["type"] == "STATUS"]
    assert phases == ["planning", "generating"]  # the test model does not search
    assert types[:3] == ["STATUS", "STATUS", "START"]
    assert set(types[3:-1]) == {"CHUNK"}
    assert types[-1] == "END"
    assert "".join(e["chunk"] for e in events if e["type"] == "CHUNK") == ANSWER
