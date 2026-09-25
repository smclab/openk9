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


"""The chat management and upload endpoints are reserved to a known user.

Their data lives in per-user indices, so without a readable token they refuse
the request with a 401 before touching OpenSearch or processing any file.
"""

from unittest.mock import AsyncMock, MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

TENANT = {"x-tenant-id": "tenant-1"}

PROTECTED_REQUESTS = [
    ("POST", "/api/rag/user-chats", {"json": {}}),
    ("GET", "/api/rag/chat/chat-1", {}),
    ("DELETE", "/api/rag/chat/chat-1", {}),
    ("PATCH", "/api/rag/chat/chat-1", {"json": {"newTitle": "Titolo"}}),
    (
        "POST",
        "/api/rag/upload-files",
        {
            "params": {"chat_id": "chat-1"},
            "files": [("files", ("a.md", b"contenuto"))],
        },
    ),
]
REQUEST_IDS = ["user-chats", "get-chat", "delete-chat", "rename-chat", "upload-files"]


@pytest.fixture
def backends(monkeypatch):
    get_opensearch_client = MagicMock()
    process_file = AsyncMock()
    monkeypatch.setattr(server, "get_opensearch_client", get_opensearch_client)
    monkeypatch.setattr(server, "process_file", process_file)
    return get_opensearch_client, process_file


def _send(method, path, kwargs, headers):
    return TestClient(server.app).request(method, path, headers=headers, **kwargs)


@pytest.mark.parametrize("method, path, kwargs", PROTECTED_REQUESTS, ids=REQUEST_IDS)
def test_request_without_a_token_is_refused(method, path, kwargs, backends):
    get_opensearch_client, process_file = backends

    response = _send(method, path, kwargs, TENANT)

    assert response.status_code == 401
    assert response.json()["detail"] == "Invalid token."
    assert response.headers["www-authenticate"] == "Bearer"
    get_opensearch_client.assert_not_called()
    process_file.assert_not_called()


@pytest.mark.parametrize("method, path, kwargs", PROTECTED_REQUESTS, ids=REQUEST_IDS)
def test_request_with_a_malformed_token_is_refused(method, path, kwargs, backends):
    get_opensearch_client, process_file = backends

    response = _send(
        method, path, kwargs, {**TENANT, "authorization": "Bearer not-a-jwt"}
    )

    assert response.status_code == 401
    assert response.headers["www-authenticate"] == "Bearer"
    get_opensearch_client.assert_not_called()
    process_file.assert_not_called()


@pytest.mark.parametrize("method, path, kwargs", PROTECTED_REQUESTS, ids=REQUEST_IDS)
def test_request_without_tenant_is_refused(method, path, kwargs, backends):
    get_opensearch_client, process_file = backends

    response = _send(method, path, kwargs, {"authorization": "Bearer fake-token"})

    assert response.status_code == 400
    assert response.json()["detail"] == "Missing x_tenant_id header."
    get_opensearch_client.assert_not_called()
    process_file.assert_not_called()
