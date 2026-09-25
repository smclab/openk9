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
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

import app.server as server

CHAT_ID = "chat-123"
HEADERS = {"authorization": "Bearer fake-token", "x-tenant-id": "tenant-1"}


def _checkpoint(seq, retrieve, step=1, answer=None, context=()):
    """A single OpenSearch hit as get_chat expects to parse it."""
    return {
        "_source": {
            "checkpoint": json.dumps(
                {
                    "ts": f"2026-01-01T00:0{seq}:00",
                    "channel_values": {
                        "current_query": f"question {seq}",
                        "response": answer or f"answer {seq}",
                        "chat_sequence_number": seq,
                        "retrieve_from_uploaded_documents": retrieve,
                        "context": list(context),
                    },
                }
            ),
            "metadata": {"step": step},
        }
    }


def _context_document(document_id, score, title, url):
    """A retrieved document as the checkpoint serializes it."""
    return {
        "kwargs": {
            "page_content": "...",
            "metadata": {
                "document_id": document_id,
                "score": score,
                "title": title,
                "url": url,
                "chunk_number": 3,
            },
        }
    }


def _rewritten_checkpoint(seq, original, rewritten, answer):
    """A turn whose query was reformulated by analyze_and_rewrite_query.

    The checkpoint keeps the user's original query in `original_query` while
    `current_query` holds the rewritten one used for retrieval.
    """
    return {
        "_source": {
            "checkpoint": json.dumps(
                {
                    "ts": f"2026-01-01T00:0{seq}:00",
                    "channel_values": {
                        "original_query": original,
                        "current_query": rewritten,
                        "response": answer,
                        "chat_sequence_number": seq,
                        "retrieve_from_uploaded_documents": False,
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
    monkeypatch.setattr(server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(hits))

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


def test_get_chat_returns_original_question_not_rewritten(client, monkeypatch):
    """A reformulated turn must surface the query the user typed.

    analyze_and_rewrite_query overwrites `current_query` with the reformulated
    query but preserves the typed one in `original_query`; get_chat must prefer
    `original_query` while still returning the final answer.
    """
    original = "dimmi di piu sul primo prodotto"
    rewritten = "Puoi fornire maggiori dettagli sulla garanzia Infortuni del Conducente?"
    final_answer = "La garanzia Infortuni del Conducente copre..."

    hits = [_rewritten_checkpoint(2, original, rewritten, final_answer)]
    monkeypatch.setattr(server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(hits))

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 200
    turn = response.json()["messages"][0]

    assert turn["question"] == original
    assert turn["answer"] == final_answer


def test_get_chat_falls_back_to_current_query_when_not_rewritten(client, monkeypatch):
    """Turns that were never rewritten have no `original_query`; get_chat must
    fall back to `current_query` (also covers data predating original_query)."""
    hits = [_checkpoint(1, retrieve=False)]
    monkeypatch.setattr(server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(hits))

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 200
    assert response.json()["messages"][0]["question"] == "question 1"


def test_get_chat_returns_404_when_the_user_has_no_index(client, monkeypatch):
    empty = MagicMock()
    empty.indices.exists.return_value = False
    monkeypatch.setattr(server, "get_opensearch_client", lambda *a, **k: empty)

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404


def test_get_chat_searches_the_chat_in_the_user_index(client, monkeypatch):
    open_search_client = _opensearch_mock([_checkpoint(1, retrieve=False)])
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: open_search_client
    )

    client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    open_search_client.search.assert_called_once_with(
        body={"size": 1000, "query": {"match": {"thread_id": CHAT_ID}}},
        index="tenant-1-user-1",
    )


def test_get_chat_keeps_the_latest_step_of_each_turn(client, monkeypatch):
    # Every step of the graph leaves a checkpoint of the same turn: only the
    # one with the highest step holds the final answer, wherever it is listed.
    hits = [
        _checkpoint(1, retrieve=False, step=2, answer="partial"),
        _checkpoint(1, retrieve=False, step=5, answer="final"),
        _checkpoint(1, retrieve=False, step=3, answer="stale"),
    ]
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(hits)
    )

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    messages = response.json()["messages"]
    assert [(m["answer"], m["step"]) for m in messages] == [("final", 5)]


@pytest.mark.parametrize("step", [0, -1])
def test_get_chat_skips_the_checkpoints_before_the_first_step(
    step, client, monkeypatch
):
    hits = [
        _checkpoint(1, retrieve=False),
        _checkpoint(2, retrieve=False, step=step),
    ]
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(hits)
    )

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert [m["chat_sequence_number"] for m in response.json()["messages"]] == [1]


def test_get_chat_lists_the_sources_of_each_answer(client, monkeypatch):
    context = [
        _context_document("doc-1", 0.91, "Guida", "https://example.com/guida"),
        _context_document("doc-2", None, "FAQ", "https://example.com/faq"),
    ]
    hits = [_checkpoint(1, retrieve=False, context=context)]
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: _opensearch_mock(hits)
    )

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.json()["messages"][0]["sources"] == [
        {
            "document_id": "doc-1",
            "score": 0.91,
            "title": "Guida",
            "url": "https://example.com/guida",
        },
        {
            "document_id": "doc-2",
            "score": None,
            "title": "FAQ",
            "url": "https://example.com/faq",
        },
    ]


def test_get_chat_returns_404_when_the_chat_has_no_messages(client, monkeypatch):
    monkeypatch.setattr(
        server, "get_opensearch_client", lambda *a, **k: _opensearch_mock([])
    )

    response = client.get(f"/api/rag/chat/{CHAT_ID}", headers=HEADERS)

    assert response.status_code == 404
    assert response.json()["detail"] == "Item not found."
