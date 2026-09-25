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


"""The upload endpoint reports the outcome of every file it processed.

Files are processed independently: the status code tells whether all of them,
some of them or none of them made it, and the body lists which ones.
"""

from unittest.mock import AsyncMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


def _outcome(file, *args):
    if file.filename.startswith("bad"):
        return {"status": "error", "filename": file.filename, "error": "Invalid"}
    if file.filename.startswith("crash"):
        raise RuntimeError("conversion crashed")
    return {"status": "success", "filename": file.filename}


@pytest.fixture
def process_file(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    process_file = AsyncMock(side_effect=_outcome)
    monkeypatch.setattr(server, "process_file", process_file)
    return process_file


def _upload(*filenames):
    return TestClient(server.app).post(
        "/api/rag/upload-files",
        params={"chat_id": "chat-1"},
        headers=HEADERS,
        files=[("files", (name, b"contenuto")) for name in filenames],
    )


def test_upload_succeeds_when_every_file_is_processed(process_file):
    response = _upload("a.md", "b.pdf")

    assert response.status_code == 200
    assert response.json() == {
        "message": "All documents uploaded successfully.",
        "status": "success",
        "processed_files": ["a.md", "b.pdf"],
    }


def test_upload_is_a_partial_success_when_some_files_fail(process_file):
    response = _upload("a.md", "bad.exe")

    assert response.status_code == 207
    assert response.json() == {
        "message": "Some files were processed successfully, but others failed.",
        "status": "partial_success",
        "processed_files": ["a.md"],
        "failed_files": [{"filename": "bad.exe", "error": "Invalid"}],
    }


def test_upload_fails_when_every_file_fails(process_file):
    response = _upload("bad.exe", "bad.bin")

    assert response.status_code == 400
    assert response.json() == {
        "message": "All files failed to process.",
        "status": "error",
        "failed_files": [
            {"filename": "bad.exe", "error": "Invalid"},
            {"filename": "bad.bin", "error": "Invalid"},
        ],
    }


def test_upload_reports_a_crashed_file_as_failed(process_file):
    response = _upload("a.md", "crash.pdf")

    assert response.status_code == 207
    assert response.json()["failed_files"] == [
        {"filename": "unknown", "error": "conversion crashed"}
    ]


def test_upload_processes_the_files_for_the_user_chat_and_tenant(process_file):
    _upload("a.md")

    file, user_id, chat_id, tenant_id, *settings = process_file.call_args.args
    assert file.filename == "a.md"
    assert (user_id, chat_id, tenant_id) == ("user-1", "chat-1", "tenant-1")
    assert settings == [
        server.UPLOAD_FILE_EXTENSIONS,
        server.UPLOAD_DIR,
        server.MAX_UPLOAD_FILE_SIZE,
        server.OPENSEARCH_HOST,
        server.GRPC_DATASOURCE_HOST,
        server.GRPC_EMBEDDING_MODULE_HOST,
    ]


def test_upload_without_chat_id_is_invalid(process_file):
    response = TestClient(server.app).post(
        "/api/rag/upload-files",
        headers=HEADERS,
        files=[("files", ("a.md", b"contenuto"))],
    )

    assert response.status_code == 422
    process_file.assert_not_called()
