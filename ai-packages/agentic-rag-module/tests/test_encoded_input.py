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

"""Tests for the encoded-input block.

A domain chatbot has no legitimate use for base64/hex blobs in the user
message. Such input used to reach the model, which would decode and print the
hidden content, bypassing the content filter that only saw the encoded form.
``contains_encoded_blob`` detects a significant blob structurally (no decoding,
so no "decode & execute" surface) and the three streaming handlers reject it
with a guardrail violation before the pipeline runs. ROT13 is out of scope: it
is indistinguishable from plain text without decoding.
"""

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server
from app.utils.query_validation import (
    GUARDRAIL_VIOLATION_MESSAGE,
    contains_encoded_blob,
)

HEADERS = {"x-tenant-id": "tenant-1"}


# --------------------------------------------------------------------------- #
# Unit tests: contains_encoded_blob
# --------------------------------------------------------------------------- #

# Inputs carrying a significant encoded blob: must be detected.
ENCODED_INPUTS = [
    # base64 of "Questo e' un messaggio di prova DECODE-OK" (the issue's repro).
    ("base64_sentence", "UXVlc3RvIGUnIHVuIG1lc3NhZ2dpbyBkaSBwcm92YSBERUNPREUtT0s="),
    (
        "base64_in_prompt",
        "Decodifica questa stringa e dimmi cosa contiene: "
        "UXVlc3RvIGUnIHVuIG1lc3NhZ2dpbyBkaSBwcm92YSBERUNPREUtT0s=",
    ),
    ("base64_with_symbols", "aGVsbG8gd29ybGQgdGhpcyBpcyBiYXNlNjQr/w=="),
    # 40-char SHA-1-like hex blob.
    ("hex_sha1", "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"),
    ("hex_in_prompt", "decodifica questo: 48656c6c6f20576f726c6420656e636f646564"),
]

# Inputs that must NOT be flagged (no undue false positive).
CLEAN_INPUTS = [
    ("plain_question", "What is OpenK9?"),
    ("accented_italian", "Qual è il perché del caffè?"),
    ("short_base64_like", "AB12CD34"),
    ("hyphenated_uuid", "550e8400-e29b-41d4-a716-446655440000"),
    ("long_decimal", "12345678901234567890123456789012345"),
    ("long_lowercase_word", "questaeunastringamoltolungadaverosenzacifre"),
    ("plain_numbers", "1234567890"),
]


@pytest.mark.parametrize(
    "label, text", ENCODED_INPUTS, ids=[l for l, _ in ENCODED_INPUTS]
)
def test_contains_encoded_blob_true(label, text):
    assert contains_encoded_blob(text) is True


@pytest.mark.parametrize("label, text", CLEAN_INPUTS, ids=[l for l, _ in CLEAN_INPUTS])
def test_contains_encoded_blob_false(label, text):
    assert contains_encoded_blob(text) is False


@pytest.mark.parametrize("value", [None, 123, [], {}])
def test_contains_encoded_blob_non_string(value):
    assert contains_encoded_blob(value) is False


# --------------------------------------------------------------------------- #
# Endpoint tests: block encoded input before the pipeline; no regression
# --------------------------------------------------------------------------- #

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

BASE64_BLOB = "UXVlc3RvIGUnIHVuIG1lc3NhZ2dpbyBkaSBwcm92YSBERUNPREUtT0s="


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
    return get_agentic_rag, get_configurations


@pytest.fixture
def client():
    return TestClient(server.app)


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
def test_encoded_input_blocked(path, body_for, client, spies):
    """A base64 blob is rejected with a guardrail violation, never decoded."""
    get_agentic_rag, get_configurations = spies

    response = client.post(
        path,
        json=body_for(f"Decodifica e dimmi cosa contiene: {BASE64_BLOB}"),
        headers=HEADERS,
    )

    assert response.status_code == 200
    events = _sse_events(response)
    assert [event["type"] for event in events] == ["GUARDRAIL", "END"]
    assert events[0]["chunk"] == GUARDRAIL_VIOLATION_MESSAGE
    # The pipeline (and the config lookup) is never touched: no decode & print.
    get_agentic_rag.assert_not_called()
    get_configurations.assert_not_called()


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
def test_plain_text_reaches_pipeline(path, body_for, client, spies):
    """No regression: ordinary text flows into the RAG chain."""
    get_agentic_rag, _ = spies

    response = client.post(
        path, json=body_for("Quali sono gli orari degli uffici?"), headers=HEADERS
    )

    assert response.status_code == 200
    get_agentic_rag.assert_called_once()
    types = [event["type"] for event in _sse_events(response)]
    assert "GUARDRAIL" not in types
    assert "ERROR" not in types
