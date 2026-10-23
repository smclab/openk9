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
"""Regression per la chat degli utenti NON loggati.

Per gli anonimi la conversazione la porta il client a ogni richiesta (``chatHistory``), come in
agentic-rag-module (``load_messages_from_frontend``). Il server non ne tiene stato: due anonimi non
possono vedersi, e il comportamento non dipende da quale replica risponde.

Il test ``test_second_turn_serves_the_requesting_user_history`` sta al seam che conta — piu'
richieste che si alternano — perche' il difetto originale era invisibile con un solo chiamante.
"""

import asyncio
from contextlib import asynccontextmanager
from types import SimpleNamespace
from unittest.mock import MagicMock

from app.rag.agentic import entrypoint, orchestrator
from app.rag.agentic.contracts import OrchestratorOutput
from app.rag.agentic.persistence import (
    FrontendConversationStore,
    messages_from_chat_history,
)


def _texts(messages):
    out = []
    for message in messages or []:
        for part in message.parts:
            content = getattr(part, "content", None)
            if isinstance(content, str):
                out.append(content)
    return out


def _history(question, answer):
    return [{"question": question, "answer": answer, "title": "", "sources": []}]


# --- conversione del payload ----------------------------------------------------


def test_chat_history_becomes_alternating_messages():
    messages = messages_from_chat_history(_history("domanda", "risposta"))

    assert _texts(messages) == ["domanda", "risposta"]
    assert [type(m).__name__ for m in messages] == ["ModelRequest", "ModelResponse"]


def test_empty_or_missing_history_gives_no_messages():
    assert messages_from_chat_history(None) == []
    assert messages_from_chat_history([]) == []


def test_malformed_entries_are_skipped():
    """Tolleranza come ``load_messages_from_frontend``: una entry rotta non fa fallire il turno."""
    history = _history("buona", "ok") + [{"question": "senza risposta"}]

    assert _texts(messages_from_chat_history(history)) == ["buona", "ok"]


# --- store degli anonimi --------------------------------------------------------


def test_history_comes_from_the_payload():
    store = FrontendConversationStore(_history("domanda", "risposta"))

    assert _texts(store.load_message_history(None)) == ["domanda", "risposta"]


def test_two_anonymous_users_are_isolated():
    a = FrontendConversationStore(_history("parola chiave ALFA", "ok"))
    b = FrontendConversationStore(_history("parola chiave BETA", "ok"))

    assert _texts(a.load_message_history(None)) == ["parola chiave ALFA", "ok"]
    assert _texts(b.load_message_history(None)) == ["parola chiave BETA", "ok"]


def test_save_turn_keeps_no_server_state():
    """Il turno successivo lo rimanda il client: salvare non deve accumulare nulla."""
    a = FrontendConversationStore(_history("parola chiave ALFA", "ok"))
    a.save_turn(
        chat_id=None,
        chat_sequence_number=1,
        original_query="q",
        current_query="q",
        response="r",
        conversation_title=None,
        retrieve_from_uploaded_documents=False,
        used_document_refs=[],
        message_history=messages_from_chat_history(_history("parola chiave ALFA", "ok")),
    )

    assert _texts(FrontendConversationStore(None).load_message_history(None)) == []
    assert _texts(a.load_message_history(None)) == ["parola chiave ALFA", "ok"]


# --- il seam che conta: richieste anonime che si alternano -----------------------


def _drive_anonymous_turns(monkeypatch):
    """B fa il suo turno 1, poi A fa il turno 2. Ritorna la history vista dal modello per A."""
    received = []

    @asynccontextmanager
    async def fake_stream_orchestrator(*, question, deps, message_history):
        received.append(list(message_history or []))
        result = MagicMock()
        result.all_messages.return_value = list(message_history or [])
        output = OrchestratorOutput(status="ok", message="ok", used_document_ids=[])

        async def deltas():
            yield output.message

        async def finalize():
            return output, [], result

        yield SimpleNamespace(deltas=deltas, finalize=finalize)

    monkeypatch.setattr(orchestrator, "stream_orchestrator", fake_stream_orchestrator)

    async def turn(store, search_text, seq):
        async for _ in entrypoint.run_turn(
            rag_type="CHAT_RAG",
            search_text=search_text,
            deps=MagicMock(rag_configuration={}),
            store=store,
            chat_id=None,
            user_id=None,
            chat_sequence_number=seq,
            retrieve_from_uploaded_documents=False,
            enable_conversation_title=False,
        ):
            pass

    async def scenario():
        await turn(FrontendConversationStore(_history("parola chiave BETA", "ok")), "b1", seq=1)
        await turn(FrontendConversationStore(_history("parola chiave ALFA", "ok")), "a2", seq=2)

    asyncio.run(scenario())
    return _texts(received[-1])


def test_second_turn_serves_the_requesting_user_history(monkeypatch):
    got = _drive_anonymous_turns(monkeypatch)

    assert "parola chiave ALFA" in got, f"A non ha ricevuto la propria history: {got}"
    assert "parola chiave BETA" not in got, f"contaminazione dalla history di B: {got}"
