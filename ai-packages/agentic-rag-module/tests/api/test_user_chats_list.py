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


"""The user chats endpoint lists the chats of the authenticated user.

Every chat is an aggregation bucket of the user's checkpoint index; its entry
is read from the checkpoint of the latest step.
"""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


def _bucket(chat_id, channel_values, ts):
    """A threads bucket carrying the checkpoint of its latest step."""
    checkpoint = json.dumps({"ts": ts, "channel_values": channel_values})
    return {
        "key": chat_id,
        "max_step_doc": {"hits": {"hits": [{"_source": {"checkpoint": checkpoint}}]}},
    }


def _opensearch_mock(buckets, index_exists=True):
    client = MagicMock()
    client.indices.exists.return_value = index_exists
    client.search.return_value = {"aggregations": {"threads": {"buckets": buckets}}}
    return client


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


def test_user_chats_lists_one_entry_per_chat(client, monkeypatch):
    buckets = [
        _bucket(
            "chat-1",
            {"conversation_title": "OpenK9", "current_query": "Che cos'è OpenK9?"},
            "2026-01-01T00:01:00",
        ),
        _bucket(
            "chat-2",
            {"conversation_title": "Helm", "current_query": "Come si installa?"},
            "2026-01-02T00:01:00",
        ),
    ]
    open_search_client = _opensearch_mock(buckets)
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.post("/api/rag/user-chats", json={}, headers=HEADERS)

    assert response.status_code == 200
    assert response.json() == {
        "result": [
            {
                "title": "OpenK9",
                "question": "Che cos'è OpenK9?",
                "timestamp": "2026-01-01T00:01:00",
                "chat_id": "chat-1",
            },
            {
                "title": "Helm",
                "question": "Come si installa?",
                "timestamp": "2026-01-02T00:01:00",
                "chat_id": "chat-2",
            },
        ]
    }
    assert open_search_client.search.call_args.kwargs["index"] == "tenant-1-user-1"


def test_user_chats_shows_the_question_the_user_typed(client, monkeypatch):
    buckets = [
        _bucket(
            "chat-1",
            {
                "original_query": "e il secondo?",
                "current_query": "Quali sono i dettagli del secondo prodotto?",
            },
            "2026-01-01T00:01:00",
        )
    ]
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(buckets)
    )

    response = client.post("/api/rag/user-chats", json={}, headers=HEADERS)

    assert response.json()["result"][0]["question"] == "e il secondo?"


def test_user_chats_is_empty_when_the_user_has_no_index(client, monkeypatch):
    open_search_client = _opensearch_mock([], index_exists=False)
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.post("/api/rag/user-chats", json={}, headers=HEADERS)

    assert response.status_code == 200
    assert response.json() == {"result": []}
    open_search_client.search.assert_not_called()


def test_user_chats_pages_the_chats_as_requested(client, monkeypatch):
    open_search_client = _opensearch_mock([])
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    client.post(
        "/api/rag/user-chats",
        json={"paginationFrom": 20, "paginationSize": 5},
        headers=HEADERS,
    )

    body = open_search_client.search.call_args.kwargs["body"]
    bucket_sort = body["aggs"]["threads"]["aggs"]["bucket_sort"]["bucket_sort"]
    assert bucket_sort == {"from": 20, "size": 5}


def test_user_chats_groups_the_checkpoints_by_chat(client, monkeypatch):
    open_search_client = _opensearch_mock([])
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    client.post("/api/rag/user-chats", json={}, headers=HEADERS)

    threads = open_search_client.search.call_args.kwargs["body"]["aggs"]["threads"]
    assert threads["terms"]["field"] == "thread_id"
    latest_step = threads["aggs"]["max_step_doc"]["top_hits"]
    assert latest_step["size"] == 1
    (sort,) = latest_step["sort"]
    assert sort["_script"]["order"] == "desc"
    assert "metadata.step.keyword" in sort["_script"]["script"]["source"]
