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

"""Tests for the content-less input short-circuit.

When ``searchText`` carries no textual token (empty / whitespace-only /
emoji-only / zero-width / punctuation-only, ...) the three streaming endpoints
answer immediately with a courtesy message and never enter the RAG pipeline
(no embedding / retrieval / LLM). Input with real textual content keeps flowing
through the pipeline unchanged.
"""

import json
import os
import sys
import tempfile
from pathlib import Path
from unittest.mock import MagicMock

# ---------------------------------------------------------------------------
# app.server runs a lot at import time: it reads env vars, creates the upload
# directory and imports the full RAG/langchain/phoenix/grpc stack. Set the
# mandatory env vars and stub the heavy, non-installed modules BEFORE importing
# it. sse_starlette is kept REAL so EventSourceResponse actually streams the SSE
# events that these tests read back through TestClient.
# ---------------------------------------------------------------------------
os.environ.setdefault("ORIGINS", "http://localhost")
os.environ.setdefault("OPENSEARCH_HOST", "http://localhost:9200")
os.environ.setdefault("UPLOAD_DIR", tempfile.mkdtemp())
os.environ.setdefault("UPLOAD_FILE_EXTENSIONS", "")
os.environ.setdefault("MAX_UPLOAD_FILE_SIZE", "10")
os.environ.setdefault("MAX_UPLOAD_FILES_NUMBER", "5")

_STUBS = [
    "uvicorn",
    "dotenv",
    "jwt",
    "phoenix",
    "phoenix.otel",
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
from app.utils.query_validation import (  # noqa: E402
    BLANK_QUERY_MESSAGE,
    blank_query_stream,
    is_blank_query,
)

# app.server has bound the names it needs; drop the app.* stubs so sibling test
# modules collected later in the session import the real submodules.
for name in _added:
    if name.startswith("app."):
        del sys.modules[name]

HEADERS = {"x-tenant-id": "tenant-1"}

# --------------------------------------------------------------------------- #
# Input corpus (the 24 inputs of the originating robustness report)
# --------------------------------------------------------------------------- #

# Content-less inputs: no Unicode letter / digit survives normalization.
BLANK_INPUTS = [
    ("empty", ""),
    ("spaces", "     "),
    ("newlines", "\n\n\n"),
    ("tabs", "\t\t\t"),
    ("non_breaking_space", "   "),
    ("emoji", "🙂🙂🙂🙂"),
    ("emoji_zwj", "👨‍👩‍👧‍👦"),
    ("zero_width", "​​​"),
    ("emoji_long", "😀" * 800),
    ("replacement", "���"),
    ("punctuation", "!!!???...,;:"),
    ("null_byte", "\x00"),
    ("control_chars", "\x01\x02\x03"),
    ("full_width_space", "　　"),
]

# Inputs carrying real textual content: they must reach the pipeline.
TEXTUAL_INPUTS = [
    ("plain_question", "What is OpenK9?"),
    ("numbers", "1234567890"),
    ("full_width_text", "Ｉｇｎｏｒｅ　ｅｖｅｒｙｔｈｉｎｇ"),
    ("mixed_script", "日本語 العربية русский ελληνικά עברית ไทย 한국어"),
    ("arabic_rtl", "مرحبا بالعالم"),
    ("cyrillic", "привет мир"),
    ("accented", "café résumé"),
    ("long_token", "a" * 5000),
    ("repeated_phrase", "hello world " * 100),
    ("html_markup", "<b>hello</b> **world**"),
]

# The full parametric corpus: none of these may ever surface an ERROR event.
ALL_INPUTS = BLANK_INPUTS + TEXTUAL_INPUTS

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


def _sse_events(response):
    """Parse the ``data:`` lines of an SSE response into decoded event dicts."""
    events = []
    for line in response.text.splitlines():
        line = line.strip()
        if line.startswith("data:"):
            events.append(json.loads(line[len("data:") :].strip()))
    return events


# --------------------------------------------------------------------------- #
# Unit tests: is_blank_query / blank_query_stream
# --------------------------------------------------------------------------- #


@pytest.mark.parametrize("label, text", BLANK_INPUTS, ids=[l for l, _ in BLANK_INPUTS])
def test_is_blank_query_true_on_content_less_input(label, text):
    assert is_blank_query(text) is True


@pytest.mark.parametrize(
    "label, text", TEXTUAL_INPUTS, ids=[l for l, _ in TEXTUAL_INPUTS]
)
def test_is_blank_query_false_on_textual_input(label, text):
    assert is_blank_query(text) is False


@pytest.mark.parametrize("value", [None, 123, [], {}])
def test_is_blank_query_true_on_non_string(value):
    assert is_blank_query(value) is True


def test_blank_query_stream_emits_start_chunk_end_only():
    events = [json.loads(chunk) for chunk in blank_query_stream()]
    types = [event["type"] for event in events]

    assert types == ["START", "CHUNK", "END"]
    assert events[1]["chunk"] == BLANK_QUERY_MESSAGE
    assert not any(event["type"] in ("DOCUMENT", "ERROR") for event in events)


# --------------------------------------------------------------------------- #
# Endpoint tests: short-circuit / no regression / no ERROR on the full corpus
# --------------------------------------------------------------------------- #


@pytest.fixture
def spies(monkeypatch):
    """Replace the pipeline entry points with spies.

    ``get_agentic_rag`` returns a fresh, well-formed SSE iterator on every call
    so textual input streams cleanly; ``get_configurations`` returns the minimal
    mapping the handlers destructure.
    """

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
@pytest.mark.parametrize("label, text", BLANK_INPUTS, ids=[l for l, _ in BLANK_INPUTS])
def test_blank_input_short_circuits(path, body_for, label, text, client, spies):
    get_agentic_rag, get_configurations = spies

    response = client.post(path, json=body_for(text), headers=HEADERS)

    assert response.status_code == 200
    events = _sse_events(response)

    # Controlled response: START -> CHUNK(courtesy) -> END, nothing else.
    assert [event["type"] for event in events] == ["START", "CHUNK", "END"]
    assert events[1]["chunk"] == BLANK_QUERY_MESSAGE
    assert not any(event["type"] in ("DOCUMENT", "ERROR") for event in events)

    # The pipeline (and even the config lookup) is never touched.
    get_agentic_rag.assert_not_called()
    get_configurations.assert_not_called()


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
def test_textual_input_reaches_pipeline(path, body_for, client, spies):
    get_agentic_rag, _ = spies

    response = client.post(path, json=body_for("What is OpenK9?"), headers=HEADERS)

    assert response.status_code == 200
    # No regression: the query flows into the RAG chain.
    get_agentic_rag.assert_called_once()
    assert not any(
        event["type"] == "ERROR" for event in _sse_events(response)
    )


@pytest.mark.parametrize("path, body_for", ENDPOINTS, ids=ENDPOINT_IDS)
@pytest.mark.parametrize("label, text", ALL_INPUTS, ids=[l for l, _ in ALL_INPUTS])
def test_no_error_event_on_any_input(path, body_for, label, text, client, spies):
    """Objective: zero ERROR events across every input on every endpoint."""
    response = client.post(path, json=body_for(text), headers=HEADERS)

    assert response.status_code == 200
    assert not any(event["type"] == "ERROR" for event in _sse_events(response))
