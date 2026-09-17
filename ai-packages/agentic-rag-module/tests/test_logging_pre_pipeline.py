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


"""Tests for the records the two pre-pipeline rejections leave behind.

A blank query and an encoded blob are both answered before the graph ever
runs, so nothing downstream can report them: without these records the
request would be invisible to whoever reads the logs.
"""

import json
import logging

import pytest
from fastapi.testclient import TestClient

import app.server as server

HEADERS = {"x-tenant-id": "litwick"}
BASE64_BLOB = "aGVsbG8gd29ybGQgdGhpcyBpcyBhIHRlc3QgcGF5bG9hZA=="
HEX_BLOB = "deadbeef" * 8

ENDPOINTS = [
    ("/api/rag/generate", lambda text: {"searchQuery": [], "searchText": text}),
    (
        "/api/rag/chat",
        lambda text: {"searchText": text, "timestamp": "1", "chatSequenceNumber": 1},
    ),
    (
        "/api/rag/chat-tool",
        lambda text: {"searchText": text, "timestamp": "1", "chatSequenceNumber": 1},
    ),
]
ENDPOINT_IDS = [path for path, _ in ENDPOINTS]


@pytest.fixture
def client():
    return TestClient(server.app)


def _sse_types(response):
    return [
        json.loads(line.strip()[len("data:") :].strip())["type"]
        for line in response.text.splitlines()
        if line.strip().startswith("data:")
    ]


def _messages(caplog, level):
    return [record.getMessage() for record in caplog.records if record.levelno == level]


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
def test_blank_query_is_reported_at_info(path, body_for, client, caplog):
    with caplog.at_level(logging.INFO):
        response = client.post(path, json=body_for("   "), headers=HEADERS)

    assert _sse_types(response) == ["START", "CHUNK", "END"]
    blank = [m for m in _messages(caplog, logging.INFO) if "[blank_query]" in m][0]
    assert f"endpoint={path}" in blank
    assert "tenant_id=litwick" in blank
    assert _messages(caplog, logging.WARNING) == []


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
@pytest.mark.parametrize(
    "blob_type, blob", [("base64", BASE64_BLOB), ("hex", HEX_BLOB)]
)
def test_encoded_blob_is_reported_as_a_warning(
    path, body_for, blob_type, blob, client, caplog
):
    with caplog.at_level(logging.INFO):
        response = client.post(path, json=body_for(blob), headers=HEADERS)

    assert _sse_types(response) == ["GUARDRAIL", "END"]
    blocked = _messages(caplog, logging.WARNING)[0]
    assert "[encoded_blob] BLOCKED" in blocked
    assert f"endpoint={path}" in blocked
    assert "tenant_id=litwick" in blocked
    assert f"blob_type={blob_type}" in blocked


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
def test_the_rejected_input_never_appears_at_info(path, body_for, client, caplog):
    with caplog.at_level(logging.INFO):
        client.post(path, json=body_for(BASE64_BLOB), headers=HEADERS)

    assert all(
        BASE64_BLOB not in message for message in _messages(caplog, logging.WARNING)
    )


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
def test_the_rejected_input_appears_at_debug(path, body_for, client, caplog):
    with caplog.at_level(logging.DEBUG):
        client.post(path, json=body_for(BASE64_BLOB), headers=HEADERS)

    blocked = [m for m in _messages(caplog, logging.WARNING) if "BLOCKED" in m]
    assert len(blocked) == 1
    assert BASE64_BLOB in blocked[0]
