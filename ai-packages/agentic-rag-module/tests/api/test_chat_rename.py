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


"""Renaming a chat rewrites the title in the checkpoint of its latest step."""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

CHAT_ID = "chat-123"
HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


def _hit():
    checkpoint = {
        "ts": "2026-01-01T00:01:00",
        "channel_values": {"conversation_title": "Titolo vecchio", "response": "ok"},
    }
    return {"_id": "doc-42", "_source": {"checkpoint": json.dumps(checkpoint)}}


def _opensearch_mock(hits, index_exists=True):
    client = MagicMock()
    client.indices.exists.return_value = index_exists
    client.search.return_value = {"hits": {"hits": hits}}
    return client


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


def _rename(client, title="Titolo nuovo"):
    return client.patch(
        f"/api/rag/chat/{CHAT_ID}", json={"newTitle": title}, headers=HEADERS
    )


def test_rename_writes_the_new_title_in_the_latest_checkpoint(client, monkeypatch):
    open_search_client = _opensearch_mock([_hit()])
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = _rename(client)

    assert response.status_code == 200
    assert response.json() == {
        "message": "Title updated successfully.",
        "status": "success",
    }
    search = open_search_client.search.call_args.kwargs
    assert search["index"] == "tenant-1-user-1"
    assert search["body"]["query"] == {
        "bool": {"must": [{"term": {"thread_id": CHAT_ID}}]}
    }
    update = open_search_client.update.call_args.kwargs
    assert update["index"] == "tenant-1-user-1"
    assert update["id"] == "doc-42"
    assert update["refresh"] is True
    checkpoint = json.loads(update["body"]["doc"]["checkpoint"])
    assert checkpoint["channel_values"] == {
        "conversation_title": "Titolo nuovo",
        "response": "ok",
    }


def test_rename_returns_404_when_the_user_has_no_index(client, monkeypatch):
    open_search_client = _opensearch_mock([], index_exists=False)
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = _rename(client)

    assert response.status_code == 404
    open_search_client.search.assert_not_called()


def test_rename_returns_404_when_the_chat_is_missing(client, monkeypatch):
    open_search_client = _opensearch_mock([])
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = _rename(client)

    assert response.status_code == 404
    assert response.json()["detail"] == "Chat document not found."
    open_search_client.update.assert_not_called()


def test_rename_returns_500_when_the_search_fails(client, monkeypatch):
    open_search_client = _opensearch_mock([])
    open_search_client.search.side_effect = RuntimeError("cluster down")
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = _rename(client)

    assert response.status_code == 500
    assert response.json()["detail"] == "OpenSearch search error: cluster down"


def test_rename_returns_500_when_the_update_fails(client, monkeypatch):
    open_search_client = _opensearch_mock([_hit()])
    open_search_client.update.side_effect = RuntimeError("version conflict")
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = _rename(client)

    assert response.status_code == 500
    assert response.json()["detail"] == "OpenSearch update error: version conflict"


@pytest.mark.parametrize("title", ["", "x" * 101], ids=["empty", "too_long"])
def test_rename_refuses_an_invalid_title(title, client, monkeypatch):
    open_search_client = _opensearch_mock([_hit()])
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = _rename(client, title)

    assert response.status_code == 422
    open_search_client.update.assert_not_called()
