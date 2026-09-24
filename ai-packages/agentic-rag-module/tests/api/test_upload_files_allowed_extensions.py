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

from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server
from app.utils import file_upload

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})

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


def test_the_configured_extensions_are_parsed_as_a_list():
    assert server.UPLOAD_FILE_EXTENSIONS == [
        ".pdf",
        ".md",
        ".docx",
        ".xlsx",
        ".pptx",
        ".csv",
    ]


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
