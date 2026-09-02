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


from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient
from opensearchpy.exceptions import NotFoundError

import app.server as server

CHAT_ID = "chat-123"
HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


def _opensearch_mock(deleted=1, delete_error=None):
    client = MagicMock()
    client.indices.exists.return_value = True
    if delete_error is not None:
        client.delete_by_query.side_effect = delete_error
    else:
        client.delete_by_query.return_value = {"deleted": deleted}
    return client


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


def test_delete_chat_returns_404_when_the_index_lookup_fails(client, monkeypatch):
    """Regression guard: the handler referenced `OpenSearch.exceptions`, which
    does not exist on the client class. The except clause is only evaluated once
    something is raised, so the broken reference stayed invisible until a real
    NotFoundError arrived - and then surfaced as a 500 instead of a 404."""
    opensearch = _opensearch_mock(delete_error=NotFoundError(404, "index_not_found"))
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: opensearch
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
    assert response.json()["detail"] == "Item not found."


def test_delete_chat_returns_404_when_nothing_was_deleted(client, monkeypatch):
    opensearch = _opensearch_mock(deleted=0)
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: opensearch
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404


def test_delete_chat_returns_404_when_the_index_is_missing(client, monkeypatch):
    opensearch = _opensearch_mock()
    opensearch.indices.exists.return_value = False
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: opensearch
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
    opensearch.delete_by_query.assert_not_called()
