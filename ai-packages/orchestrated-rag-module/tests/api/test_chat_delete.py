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


"""Deleting a chat removes its messages, its writes and its uploaded documents."""

from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

CHAT_ID = "chat-123"
HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}

USER_INDEX = "tenant-1-user-1"
WRITES_INDEX = "tenant-1-user-1-writes"
UPLOADED_DOCUMENTS_INDEX = "tenant-1-uploaded-documents-index"

MESSAGES_QUERY = {"query": {"match": {"thread_id": CHAT_ID}}}


def _opensearch_mock(existing_indices, deleted=3):
    client = MagicMock()
    client.indices.exists.side_effect = lambda index: index in existing_indices
    client.delete_by_query.return_value = {"deleted": deleted}
    return client


def _deletes(open_search_client):
    return {
        call.kwargs["index"]: call.kwargs["body"]
        for call in open_search_client.delete_by_query.call_args_list
    }


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


def test_delete_removes_messages_writes_and_uploaded_documents(client, monkeypatch):
    open_search_client = _opensearch_mock(
        {USER_INDEX, WRITES_INDEX, UPLOADED_DOCUMENTS_INDEX}
    )
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 200
    assert response.json() == {
        "message": "Chat deleted successfully.",
        "status": "success",
    }
    assert _deletes(open_search_client) == {
        USER_INDEX: MESSAGES_QUERY,
        WRITES_INDEX: MESSAGES_QUERY,
        UPLOADED_DOCUMENTS_INDEX: {
            "query": {
                "bool": {
                    "must": [
                        {"match": {"user_id.keyword": "user-1"}},
                        {"match": {"chat_id.keyword": CHAT_ID}},
                    ]
                }
            }
        },
    }


def test_delete_skips_the_indices_that_do_not_exist(client, monkeypatch):
    open_search_client = _opensearch_mock({USER_INDEX})
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 200
    assert _deletes(open_search_client) == {USER_INDEX: MESSAGES_QUERY}


def test_delete_returns_404_when_the_user_has_no_index(client, monkeypatch):
    open_search_client = _opensearch_mock(set())
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
    assert response.json()["detail"] == "Item not found."
    open_search_client.delete_by_query.assert_not_called()


def test_delete_returns_404_when_the_chat_has_no_messages(client, monkeypatch):
    open_search_client = _opensearch_mock(
        {USER_INDEX, WRITES_INDEX, UPLOADED_DOCUMENTS_INDEX}, deleted=0
    )
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
    assert response.json()["detail"] == "Item not found."
    assert _deletes(open_search_client) == {USER_INDEX: MESSAGES_QUERY}
