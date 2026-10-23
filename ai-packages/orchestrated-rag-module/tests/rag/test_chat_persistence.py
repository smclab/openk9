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
"""Regression per la persistenza chat su OpenSearch.

I test unitari coprono la logica pura (carry-over del titolo, scelta dello step).
La validita' delle *query* OpenSearch non e' catturabile con un client mockato — una
``MagicMock`` accetta anche un'aggregazione illegale — quindi i test in fondo al file
girano solo con un cluster reale, via ``OPENSEARCH_TEST_HOST``.
"""

import asyncio
import json
import os
import uuid
from unittest.mock import MagicMock

import pytest
from pydantic_ai.messages import ModelRequest, ModelResponse, TextPart, UserPromptPart

from app.rag.agentic.entrypoint import resolve_conversation_title
from app.rag.agentic.persistence import OpenSearchConversationStore

OPENSEARCH_TEST_HOST = os.environ.get("OPENSEARCH_TEST_HOST")


def _history(exchanges):
    messages = []
    for i in range(1, exchanges + 1):
        messages.append(ModelRequest(parts=[UserPromptPart(content=f"domanda {i}")]))
        messages.append(ModelResponse(parts=[TextPart(content=f"risposta {i}")]))
    return messages


def _store_with_mock(latest_source):
    """Store con client mockato; ``latest_source`` e' il doc a step massimo (o ``None``)."""
    store = OpenSearchConversationStore.__new__(OpenSearchConversationStore)
    store.index = "tenant-user"
    store._client = MagicMock()
    store._client.indices.exists.return_value = True
    hits = [{"_source": latest_source}] if latest_source else []
    store._client.search.return_value = {"hits": {"hits": hits}}
    return store


def _indexed_checkpoint(store):
    """Il checkpoint (deserializzato) passato a ``client.index``."""
    body = store._client.index.call_args.kwargs["body"]
    return json.loads(body["checkpoint"]), body


def _save(store, *, seq, title):
    store.save_turn(
        chat_id="chat-1",
        chat_sequence_number=seq,
        original_query=f"domanda {seq}",
        current_query=f"domanda {seq}",
        response=f"risposta {seq}",
        conversation_title=title,
        retrieve_from_uploaded_documents=False,
        used_document_refs=[],
        message_history=_history(seq),
    )


def test_title_is_carried_over_to_later_turns():
    """``get_user_chats`` legge il doc a step massimo: senza carry-over la lista perde il titolo."""
    previous = {
        "metadata": {"step": "1"},
        "checkpoint": json.dumps(
            {"ts": "2026-01-01T00:00:00+00:00", "channel_values": {"conversation_title": "Titolo"}}
        ),
    }
    store = _store_with_mock(previous)

    _save(store, seq=2, title=None)

    checkpoint, _ = _indexed_checkpoint(store)
    assert checkpoint["channel_values"]["conversation_title"] == "Titolo"


def test_explicit_title_wins_over_carried_over_one():
    """Un rename non deve essere sovrascritto dal titolo vecchio."""
    previous = {
        "metadata": {"step": "1"},
        "checkpoint": json.dumps({"channel_values": {"conversation_title": "Vecchio"}}),
    }
    store = _store_with_mock(previous)

    _save(store, seq=2, title="Nuovo")

    checkpoint, _ = _indexed_checkpoint(store)
    assert checkpoint["channel_values"]["conversation_title"] == "Nuovo"


def test_step_increments_from_latest_document():
    store = _store_with_mock({"metadata": {"step": "9"}, "checkpoint": "{}"})

    _save(store, seq=10, title=None)

    _, body = _indexed_checkpoint(store)
    assert body["metadata"]["step"] == "10"


def test_first_turn_starts_at_step_one():
    store = _store_with_mock(None)

    _save(store, seq=1, title="Titolo")

    _, body = _indexed_checkpoint(store)
    assert body["metadata"]["step"] == "1"


def _title(**kwargs):
    kwargs.setdefault("enabled", False)
    kwargs.setdefault("question", "Come funziona?")
    kwargs.setdefault("answer", "risposta")
    return asyncio.run(resolve_conversation_title(None, **kwargs))


def test_title_falls_back_to_first_question_when_generation_disabled():
    """``enable_conversation_title`` off: senza fallback la lista chat resta senza titolo."""
    title, emit = _title(
        chat_sequence_number=1,
        user_id="user-1",
        chat_id="chat-1",
        question='"Come funziona?"',
    )

    assert title == "Come funziona?"
    assert emit is False


def test_no_title_for_later_turns():
    title, emit = _title(chat_sequence_number=2, user_id="user-1", chat_id="chat-1")

    assert title is None
    assert emit is False


def test_no_title_for_anonymous_user():
    title, _ = _title(chat_sequence_number=1, user_id=None, chat_id=None)

    assert title is None


# --- integrazione: richiedono un cluster reale ----------------------------------
# Le query di questo store girano su ``metadata.step``, che e' mappato text+keyword: un
# client mockato non puo' accorgersi ne' che l'agg ``max`` su keyword e' rifiutata, ne'
# che un sort su keyword ordina "9" dopo "10". Servono chiamate vere.

integration = pytest.mark.skipif(
    not OPENSEARCH_TEST_HOST,
    reason="richiede un cluster OpenSearch: impostare OPENSEARCH_TEST_HOST",
)


@pytest.fixture
def real_store():
    store = OpenSearchConversationStore(
        OPENSEARCH_TEST_HOST, "pytest-persistence", uuid.uuid4().hex
    )
    yield store
    store._client.indices.delete(index=store.index, ignore=[404])


@integration
def test_save_turn_persists_and_reloads_history(real_store):
    _save(real_store, seq=1, title="Titolo")
    _save(real_store, seq=2, title=None)

    assert len(real_store.load_message_history("chat-1")) == 4


@integration
def test_history_reload_is_correct_past_step_nine(real_store):
    """Il sort sullo step deve essere numerico: a step 10+ un sort keyword regredisce a "9"."""
    for seq in range(1, 12):
        _save(real_store, seq=seq, title="Titolo" if seq == 1 else None)

    assert len(real_store.load_message_history("chat-1")) == 22
