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

"""The upload endpoint accepts only the configured extensions.

UPLOAD_FILE_EXTENSIONS is deployed as a JSON array. Passed on as the raw
string, the extension check became a substring match, so a legacy ".xls"
slipped through because it is a prefix of the allowed ".xlsx".
"""

import os
import sys
import tempfile
from unittest.mock import MagicMock

# app.server reads env vars and imports the full RAG stack at import time: set
# the mandatory env vars and stub the heavy modules before importing it, as the
# sibling server tests do.
os.environ.setdefault("ORIGINS", "http://localhost")
os.environ.setdefault("OPENSEARCH_HOST", "http://localhost:9200")
os.environ.setdefault("UPLOAD_DIR", tempfile.mkdtemp())
os.environ.setdefault("MAX_UPLOAD_FILE_SIZE", "10")
os.environ.setdefault("MAX_UPLOAD_FILES_NUMBER", "5")

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

for name in _added:
    if name.startswith("app."):
        del sys.modules[name]

from app.utils import file_upload  # noqa: E402

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    # The first test module importing app.server may have bound a stubbed
    # file_upload: route the endpoint through the real process_file, with the
    # extensions read by the real parser from the configured setting.
    monkeypatch.setattr(server, "process_file", file_upload.process_file)
    monkeypatch.setattr(
        server,
        "UPLOAD_FILE_EXTENSIONS",
        file_upload.parse_upload_file_extensions(os.environ["UPLOAD_FILE_EXTENSIONS"]),
    )

    loader = MagicMock()
    loader.return_value.load.return_value = [MagicMock(page_content="testo")]
    monkeypatch.setattr(file_upload, "DoclingLoader", loader)
    monkeypatch.setattr(
        file_upload,
        "get_embedding_model_configuration",
        lambda **kwargs: {"vector_size": 3},
    )
    monkeypatch.setattr(file_upload, "documents_embedding", lambda **kwargs: [])
    monkeypatch.setattr(file_upload, "save_uploaded_documents", MagicMock())

    return TestClient(server.app)


def _upload(client, *filenames):
    return client.post(
        "/api/rag/upload-files",
        params={"chat_id": "chat-1"},
        headers=HEADERS,
        files=[("files", (filename, b"contenuto")) for filename in filenames],
    )


def test_extension_that_is_a_prefix_of_an_allowed_one_is_rejected(client):
    response = _upload(client, "report.xlsx", "legacy.xls")

    assert response.status_code == 207
    body = response.json()
    assert body["processed_files"] == ["report.xlsx"]
    assert body["failed_files"] == [
        {"filename": "legacy.xls", "error": "Invalid document type"}
    ]


def test_file_without_extension_is_rejected(client):
    response = _upload(client, "README")

    assert response.status_code == 400
    assert response.json()["failed_files"] == [
        {"filename": "README", "error": "Invalid document type"}
    ]
