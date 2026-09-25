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


"""The chat endpoints stream a conversation turn as Server-Sent Events.

Both the plain chat and its tool variant attach the turn to a chat: the user
comes from the token (anonymous when there is none), and the history, the chat
id and the sequence number are handed to the pipeline as the client sent them.
"""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}

CHAT_ENDPOINTS = [
    ("/api/rag/chat", "CHAT_RAG"),
    ("/api/rag/chat-tool", "CHAT_RAG_TOOL"),
]
ENDPOINT_IDS = ["chat", "chat-tool"]

HISTORY = [
    {
        "question": "Che cos'è OpenK9?",
        "answer": "Un motore di ricerca.",
        "title": "OpenK9",
        "sources": [],
        "chat_id": "chat-1",
        "timestamp": "1",
        "chat_sequence_number": 1,
    }
]

# Positional slots of get_agentic_rag the handler fills.
RAG_TYPE, SEARCH_QUERY, TOKEN, SEARCH_TEXT = 0, 1, 6, 11
CHAT_ID, USER_ID, TENANT_ID, RETRIEVE_FROM_UPLOADED_DOCUMENTS = 12, 13, 14, 15
CHAT_HISTORY, TIMESTAMP, CHAT_SEQUENCE_NUMBER = 16, 17, 18


def _body(**overrides):
    body = {
        "chatId": "chat-1",
        "searchText": "E come si installa?",
        "timestamp": "2",
        "chatSequenceNumber": 2,
    }
    body.update(overrides)
    return body


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
                json.dumps({"chunk": "Con Helm.", "type": "CHUNK"}),
                json.dumps({"chunk": "", "type": "END"}),
            ]
        )

    get_agentic_rag = MagicMock(side_effect=fake_stream)
    get_configurations = MagicMock(
        return_value={"rag_configuration": {}, "llm_configuration": {}}
    )
    monkeypatch.setattr(server, "get_agentic_rag", get_agentic_rag)
    monkeypatch.setattr(server, "get_configurations", get_configurations)
    return get_agentic_rag, get_configurations


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_streams_the_pipeline_events(path, rag_type, client, pipeline):
    response = client.post(path, json=_body(), headers=HEADERS)

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    assert [event["type"] for event in _sse_events(response)] == [
        "START",
        "CHUNK",
        "END",
    ]


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_runs_the_pipeline_of_its_rag_type(path, rag_type, client, pipeline):
    get_agentic_rag, get_configurations = pipeline

    client.post(path, json=_body(), headers=HEADERS)

    assert get_configurations.call_args.kwargs["rag_type"] == rag_type
    assert get_configurations.call_args.kwargs["tenant_id"] == "tenant-1"
    args = get_agentic_rag.call_args.args
    assert args[RAG_TYPE] == rag_type
    assert args[SEARCH_QUERY] is None
    assert args[SEARCH_TEXT] == "E come si installa?"
    assert args[TENANT_ID] == "tenant-1"


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_attaches_the_turn_to_the_user_of_the_token(
    path, rag_type, client, pipeline
):
    get_agentic_rag, _ = pipeline

    client.post(path, json=_body(), headers=HEADERS)

    args = get_agentic_rag.call_args.args
    assert args[TOKEN] == "fake-token"
    assert args[USER_ID] == "user-1"
    assert args[CHAT_ID] == "chat-1"
    assert args[TIMESTAMP] == "2"
    assert args[CHAT_SEQUENCE_NUMBER] == 2


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_without_a_token_is_anonymous(path, rag_type, client, pipeline):
    get_agentic_rag, _ = pipeline

    response = client.post(path, json=_body(), headers={"x-tenant-id": "tenant-1"})

    assert response.status_code == 200
    args = get_agentic_rag.call_args.args
    assert args[TOKEN] is None
    assert args[USER_ID] is None


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_without_history_starts_a_new_conversation(
    path, rag_type, client, pipeline
):
    get_agentic_rag, _ = pipeline

    client.post(path, json=_body(), headers=HEADERS)

    args = get_agentic_rag.call_args.args
    assert args[CHAT_HISTORY] is None
    assert args[RETRIEVE_FROM_UPLOADED_DOCUMENTS] is False


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_forwards_the_history_as_sent(path, rag_type, client, pipeline):
    get_agentic_rag, _ = pipeline

    client.post(
        path,
        json=_body(chatHistory=HISTORY, retrieveFromUploadedDocuments=True),
        headers=HEADERS,
    )

    args = get_agentic_rag.call_args.args
    assert args[CHAT_HISTORY] == HISTORY
    assert args[RETRIEVE_FROM_UPLOADED_DOCUMENTS] is True


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_without_tenant_is_refused(path, rag_type, client, pipeline):
    get_agentic_rag, _ = pipeline

    response = client.post(
        path, json=_body(), headers={"authorization": "Bearer fake-token"}
    )

    assert response.status_code == 400
    assert response.json()["detail"] == "Missing x_tenant_id header."
    get_agentic_rag.assert_not_called()


@pytest.mark.parametrize("path, rag_type", CHAT_ENDPOINTS, ids=ENDPOINT_IDS)
def test_chat_with_a_malformed_token_is_refused(path, rag_type, pipeline):
    get_agentic_rag, _ = pipeline

    response = TestClient(server.app).post(
        path,
        json=_body(),
        headers={"authorization": "Bearer not-a-jwt", "x-tenant-id": "tenant-1"},
    )

    assert response.status_code == 401
    assert response.headers["www-authenticate"] == "Bearer"
    get_agentic_rag.assert_not_called()
