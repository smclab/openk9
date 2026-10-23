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

"""The chats saved by the new module and those saved by agentic-rag-module are
the same documents: get_chat serves the turns the store writes, and a chat
begun with agentic-rag-module goes on with the messages it saved."""

import json

from fastapi.testclient import TestClient
from pydantic_ai.messages import ModelRequest, ModelResponse

import app.server as server
from app.rag.agentic import persistence
from app.rag.agentic.contracts import DocumentRef
from app.rag.agentic.persistence import OpenSearchConversationStore

#: Two messages as agentic-rag-module saves them in the ``messages`` channel
#: of the checkpoint.
AGENTIC_RAG_MESSAGES = [
    {
        "lc": 1,
        "type": "constructor",
        "id": ["langchain", "schema", "messages", "HumanMessage"],
        "kwargs": {"content": "Che cos'è OpenK9?", "type": "human"},
    },
    {
        "lc": 1,
        "type": "constructor",
        "id": ["langchain", "schema", "messages", "AIMessage"],
        "kwargs": {
            "content": "Un motore di ricerca.",
            "type": "ai",
            "tool_calls": [],
            "invalid_tool_calls": [],
        },
    },
]


class FakeIndices:
    def __init__(self, documents):
        self._documents = documents

    def exists(self, index):
        return bool(self._documents) or index == "created"

    def create(self, index, body):
        self._documents.clear()


class FakeOpenSearch:
    """Keeps the indexed documents of one index and returns them all on search,
    the latest step first, as the queries of the store and of get_chat ask."""

    def __init__(self, documents=None):
        self.documents = list(documents or [])
        self.indices = FakeIndices(self.documents)

    def index(self, index, body, refresh=False):
        self.documents.append(body)

    def search(self, index, body):
        hits = sorted(
            self.documents, key=lambda doc: int(doc["metadata"]["step"]), reverse=True
        )
        return {"hits": {"hits": [{"_source": doc} for doc in hits]}}


def _store(monkeypatch, client):
    monkeypatch.setattr(
        "app.utils.opensearch_client.get_opensearch_client", lambda _host: client
    )
    return OpenSearchConversationStore("http://os", "tenant-1", "user-1")


def _save(store, sequence, question, answer, refs=(), title=None):
    store.save_turn(
        chat_id="chat-1",
        chat_sequence_number=sequence,
        original_query=question,
        current_query=question,
        response=answer,
        conversation_title=title,
        retrieve_from_uploaded_documents=False,
        used_document_refs=list(refs),
        message_history=[],
    )


def test_get_chat_serves_the_turns_the_store_saves(monkeypatch):
    client = FakeOpenSearch()
    store = _store(monkeypatch, client)
    _save(
        store,
        1,
        "Che cos'è OpenK9?",
        "Un motore di ricerca.",
        refs=[DocumentRef(document_id="doc-1", title="OpenK9", url="https://openk9.io")],
        title="OpenK9",
    )
    _save(store, 2, "E come si installa?", "Con Helm.")
    monkeypatch.setattr(server, "get_opensearch_client", lambda _host: client)
    monkeypatch.setattr(server, "decode_token", lambda _token: {"sub": "user-1"})

    response = TestClient(server.app).get(
        "/api/rag/chat/chat-1",
        headers={"authorization": "Bearer token", "x-tenant-id": "tenant-1"},
    )

    assert response.status_code == 200
    messages = response.json()["messages"]
    assert [(m["question"], m["answer"]) for m in messages] == [
        ("Che cos'è OpenK9?", "Un motore di ricerca."),
        ("E come si installa?", "Con Helm."),
    ]
    assert [s["document_id"] for s in messages[0]["sources"]] == ["doc-1"]
    assert messages[0]["sources"][0]["url"] == "https://openk9.io"


def test_turns_continue_the_steps_and_keep_the_title(monkeypatch):
    client = FakeOpenSearch()
    store = _store(monkeypatch, client)
    _save(store, 1, "domanda", "risposta", title="Titolo")
    _save(store, 2, "seguito", "altra risposta")

    steps = [int(doc["metadata"]["step"]) for doc in client.documents]
    latest = json.loads(client.documents[-1]["checkpoint"])
    assert steps == [1, 2]
    assert latest["channel_values"]["conversation_title"] == "Titolo"


def test_a_chat_of_agentic_rag_module_goes_on_with_its_messages(monkeypatch):
    legacy_checkpoint = {
        "ts": "2026-10-01T10:00:00+00:00",
        "channel_values": {
            "original_query": "Che cos'è OpenK9?",
            "response": "Un motore di ricerca.",
            "chat_sequence_number": 1,
            "messages": AGENTIC_RAG_MESSAGES,
        },
    }
    client = FakeOpenSearch(
        [
            {
                "thread_id": "chat-1",
                "checkpoint_id": "legacy",
                "checkpoint": json.dumps(legacy_checkpoint),
                "metadata": {"step": "3"},
            }
        ]
    )

    history = _store(monkeypatch, client).load_message_history("chat-1")

    assert [type(message) for message in history] == [ModelRequest, ModelResponse]
    assert history[0].parts[0].content == "Che cos'è OpenK9?"
    assert history[1].parts[0].content == "Un motore di ricerca."


def test_agentic_rag_messages_other_than_the_exchange_are_skipped():
    system = {
        "lc": 1,
        "type": "constructor",
        "id": ["langchain", "schema", "messages", "SystemMessage"],
        "kwargs": {"content": "Sei un assistente.", "type": "system"},
    }

    history = persistence.deserialize_messages([system, *AGENTIC_RAG_MESSAGES])

    assert [type(message) for message in history] == [ModelRequest, ModelResponse]


def test_multimodal_agentic_rag_content_keeps_its_text():
    message = {
        **AGENTIC_RAG_MESSAGES[0],
        "kwargs": {
            "content": [
                {"type": "text", "text": "Cosa mostra "},
                {"type": "image_url", "image_url": {"url": "data:image/png;base64,AA"}},
                {"type": "text", "text": "questa immagine?"},
            ],
            "type": "human",
        },
    }

    (request,) = persistence.deserialize_messages([message])

    assert request.parts[0].content == "Cosa mostra questa immagine?"
