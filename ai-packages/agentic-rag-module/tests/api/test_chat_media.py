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

"""Tests for the optional ``media`` field on the chat request body.

An image is a query in its own right, so a chat that carries one and no text
must reach the pipeline instead of being met with the blank-query courtesy
message. Everything else about the endpoints is unchanged: a chat with no image
behaves exactly as before, and ``generate`` silently ignores a media, since
image-as-query on that path is out of scope.
"""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server
from app.models import models
from app.utils.query_validation import BLANK_QUERY_MESSAGE

HEADERS = {"x-tenant-id": "tenant-1"}

MEDIA = {"data": "aVZCT1J3MEtHZ28=", "contentType": "image/png"}

CHAT_ENDPOINTS = ["/api/rag/chat", "/api/rag/chat-tool"]


def _body(text, media=None):
    body = {"searchText": text, "timestamp": "1", "chatSequenceNumber": 1}
    if media is not None:
        body["media"] = media
    return body


def _sse_events(response):
    events = []
    for line in response.text.splitlines():
        line = line.strip()
        if line.startswith("data:"):
            events.append(json.loads(line[len("data:") :].strip()))
    return events


@pytest.fixture
def spies(monkeypatch):
    def fake_stream(*args, **kwargs):
        return iter(
            [
                json.dumps({"chunk": "", "type": "START"}),
                json.dumps({"chunk": "ok", "type": "CHUNK"}),
                json.dumps({"chunk": "", "type": "END"}),
            ]
        )

    get_agentic_rag = MagicMock(side_effect=fake_stream)
    get_configurations = MagicMock(
        return_value={"rag_configuration": {}, "llm_configuration": {}}
    )
    monkeypatch.setattr(server, "get_agentic_rag", get_agentic_rag)
    monkeypatch.setattr(server, "get_configurations", get_configurations)
    return get_agentic_rag


@pytest.fixture
def client():
    return TestClient(server.app)


@pytest.mark.parametrize("path", CHAT_ENDPOINTS)
def test_image_without_text_reaches_the_pipeline(path, client, spies):
    response = client.post(path, json=_body("", MEDIA), headers=HEADERS)

    assert response.status_code == 200
    spies.assert_called_once()
    events = _sse_events(response)
    assert not any(event["chunk"] == BLANK_QUERY_MESSAGE for event in events)


@pytest.mark.parametrize("path", CHAT_ENDPOINTS)
def test_blank_text_without_an_image_still_short_circuits(path, client, spies):
    # No regression on the blank-query short-circuit: only a media lifts it.
    response = client.post(path, json=_body(""), headers=HEADERS)

    assert response.status_code == 200
    spies.assert_not_called()
    events = _sse_events(response)
    assert events[1]["chunk"] == BLANK_QUERY_MESSAGE


@pytest.mark.parametrize("path", CHAT_ENDPOINTS)
def test_media_forwarded_to_the_pipeline(path, client, spies):
    client.post(path, json=_body("A cosa somiglia?", MEDIA), headers=HEADERS)

    media = spies.call_args.kwargs["media"]
    assert media == models.Media(**MEDIA)


@pytest.mark.parametrize("path", CHAT_ENDPOINTS)
def test_no_media_is_forwarded_as_none(path, client, spies):
    client.post(path, json=_body("A cosa somiglia?"), headers=HEADERS)

    assert spies.call_args.kwargs["media"] is None


@pytest.mark.parametrize("path", CHAT_ENDPOINTS)
def test_media_needs_both_fields(path, client, spies):
    response = client.post(
        path, json=_body("", {"contentType": "image/png"}), headers=HEADERS
    )

    assert response.status_code == 422
    spies.assert_not_called()


def test_generate_ignores_a_media(client, spies):
    # Image-as-query on the generate path is out of scope: the extra field is
    # ignored by the body model and nothing is forwarded.
    response = client.post(
        "/api/rag/generate",
        json={"searchQuery": [], "searchText": "What is OpenK9?", "media": MEDIA},
        headers=HEADERS,
    )

    assert response.status_code == 200
    assert "media" not in spies.call_args.kwargs
