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


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


def test_delete_chat_returns_404_when_the_index_is_gone(client, monkeypatch):
    """A NotFoundError from delete_by_query must surface as a 404.

    The handler caught OpenSearch.exceptions.NotFoundError while OpenSearch was
    no longer imported in the module, so the clause raised NameError and the
    intended 404 was unreachable.
    """
    open_search_client = MagicMock()
    open_search_client.indices.exists.return_value = True
    open_search_client.delete_by_query.side_effect = NotFoundError(
        404, "index_not_found_exception", {}
    )
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    response = client.delete(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
