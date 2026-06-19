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


import json
import os
import sys
import tempfile
from unittest.mock import MagicMock

# ---------------------------------------------------------------------------
# app.server runs a lot at import time: it reads env vars, creates the upload
# directory and imports the full RAG/langchain/phoenix/grpc stack. Set the
# mandatory env vars and stub the heavy, non-installed modules BEFORE importing
# it, keeping fastapi / opensearchpy real (they back TestClient and the patched
# OpenSearch client).
# ---------------------------------------------------------------------------
os.environ.setdefault("ORIGINS", "http://localhost")
os.environ.setdefault("OPENSEARCH_HOST", "http://localhost:9200")
os.environ.setdefault("UPLOAD_DIR", tempfile.mkdtemp())
os.environ.setdefault("UPLOAD_FILE_EXTENSIONS", "")
os.environ.setdefault("MAX_UPLOAD_FILE_SIZE", "10")
os.environ.setdefault("MAX_UPLOAD_FILES_NUMBER", "5")

# Stub only leaf modules: app.rag / app.external_services are PEP 420 namespace
# packages that sibling tests import for real, so they must not be shadowed.
_STUBS = [
    "uvicorn",
    "dotenv",
    "jwt",
    "phoenix",
    "phoenix.otel",
    "sse_starlette",
    "sse_starlette.sse",
    "app.external_services.grpc.grpc_client",
    "app.rag.chain",
    "app.rag.evaluations",
    "app.utils.embedding",
    "app.utils.file_upload",
    "app.utils.scheduler",
]
_added = [name for name in _STUBS if name not in sys.modules]
for name in _added:
    sys.modules[name] = MagicMock()

import pytest  # noqa: E402
from fastapi.testclient import TestClient  # noqa: E402

import app.server as server  # noqa: E402

# app.server has bound the names it needs; drop the app.* stubs so sibling test
# modules collected later in the session import the real submodules.
for name in _added:
    if name.startswith("app."):
        del sys.modules[name]

CHAT_ID = "chat-123"
HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


def _checkpoint(seq, retrieve):
    """A single OpenSearch hit as get_chat expects to parse it."""
    return {
        "_source": {
            "checkpoint": json.dumps(
                {
                    "ts": f"2026-01-01T00:0{seq}:00",
                    "channel_values": {
                        "current_query": f"question {seq}",
                        "response": f"answer {seq}",
                        "chat_sequence_number": seq,
                        "retrieve_from_uploaded_documents": retrieve,
                        "context": [],
                    },
                }
            ),
            "metadata": {"step": 1},
        }
    }


def _opensearch_mock(hits):
    client = MagicMock()
    client.indices.exists.return_value = True
    client.search.return_value = {"hits": {"hits": hits}}
    return client


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    return TestClient(server.app)


def test_get_chat_returns_object_with_messages(client, monkeypatch):
    # Hits returned out of order to prove the response is sorted by sequence.
    hits = [_checkpoint(2, retrieve=True), _checkpoint(1, retrieve=False)]
    monkeypatch.setattr(server, "OpenSearch", lambda *a, **k: _opensearch_mock(hits))

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 200
    body = response.json()

    # Regression for the bug: the endpoint must return an object, not a bare
    # list. On the buggy code body was a list and `body["messages"]` raised.
    assert isinstance(body, dict)
    assert body["chat_id"] == CHAT_ID
    assert "retrieve_from_uploaded_documents" in body

    messages = body["messages"]
    assert messages, "messages must not be empty"
    assert [m["chat_sequence_number"] for m in messages] == [1, 2]
    assert messages[0]["question"] == "question 1"

    # Surfaced at top level from the latest turn (sequence 2 -> True).
    assert body["retrieve_from_uploaded_documents"] is True


def test_get_chat_returns_404_when_chat_missing(client, monkeypatch):
    empty = MagicMock()
    empty.indices.exists.return_value = False
    monkeypatch.setattr(server, "OpenSearch", lambda *a, **k: empty)

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
