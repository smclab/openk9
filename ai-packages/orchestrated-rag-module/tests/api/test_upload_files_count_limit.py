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

"""The upload endpoint refuses more files than MAX_UPLOAD_FILES_NUMBER.

The whole request is refused before any file is processed, so no partial
upload is left behind.
"""

from unittest.mock import AsyncMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


@pytest.fixture
def process_file(monkeypatch):
    monkeypatch.setattr(server, "decode_token", lambda token: {"sub": "user-1"})
    process_file = AsyncMock(
        side_effect=lambda file, *args: {"status": "success", "filename": file.filename}
    )
    monkeypatch.setattr(server, "process_file", process_file)
    return process_file


def _upload(count):
    return TestClient(server.app).post(
        "/api/rag/upload-files",
        params={"chat_id": "chat-1"},
        headers=HEADERS,
        files=[("files", (f"doc{i}.md", b"contenuto")) for i in range(count)],
    )


def test_more_files_than_allowed_are_refused(process_file):
    response = _upload(server.MAX_UPLOAD_FILES_NUMBER + 1)

    assert response.status_code == 400
    assert response.json()["detail"] == (
        f"You can upload max {server.MAX_UPLOAD_FILES_NUMBER} files"
    )
    process_file.assert_not_called()


def test_files_up_to_the_limit_are_processed(process_file):
    response = _upload(server.MAX_UPLOAD_FILES_NUMBER)

    assert response.status_code == 200
    assert process_file.call_count == server.MAX_UPLOAD_FILES_NUMBER
