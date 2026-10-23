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

"""
Persistence compat store — sostituisce ``OpenSearchSaver`` di agentic-rag-module mantenendone la
**forma documentale** (decisione 1 del piano).

Perche' compat e non schema nuovo: la forma checkpoint e' letta/scritta **direttamente**
da 4 endpoint di ``app/server.py`` (``get_user_chats``, ``get_chat``, ``rename_chat``,
``delete_chat``) e dallo scheduler ``app/utils/chat_history.py::delete_documents``. Riprodurre la
forma tiene quegli endpoint e lo scheduler **intatti**.

Contratto documentale (dedotto dal codice che lo legge):

- indice ``{tenant_id}-{user_id}``; ``thread_id`` = ``chat_id``.
- ``checkpoint`` = **stringa JSON** con:
  ``ts`` e ``channel_values.{original_query, current_query, response, conversation_title,
  chat_sequence_number, retrieve_from_uploaded_documents, context, messages}``.
- ``context`` = lista di documenti ``{"kwargs": {"metadata": {document_id, score, title,
  url, domain?}}}`` (``get_chat`` legge ``kwargs.metadata.*``).
- ``metadata.step`` (>0): ``get_chat``/``get_user_chats`` prendono il doc a step massimo per
  ``chat_sequence_number`` / per thread. ``get_user_chats`` aggrega su ``thread_id`` (keyword) e
  ordina con painless su ``metadata.step.keyword`` → mapping esplicito sotto.
- ``checkpoint_id``: id univoco per doc (richiesto dal riconoscimento indici di ``delete_documents``).

``channel_values.messages`` porta la history PydanticAI serializzata (round-trip via
``ModelMessagesTypeAdapter``).

⚠️ Le chiamate OpenSearch non sono validabili qui (serve un cluster). Il mapping esplicito e'
progettato per soddisfare i 3 endpoint + lo scheduler; va verificato contro un indice reale creato
da agentic-rag-module (potrebbero esserci campi extra tollerabili).
"""

import json
import uuid
from datetime import datetime, timezone
from typing import Optional

from app.rag.agentic.contracts import DocumentRef

WRITES_INDEX_SUFFIX = "-writes"

#: Mapping esplicito: thread_id keyword (aggregabile come "thread_id"), metadata.step con
#: sottocampo .keyword (usato dal painless di get_user_chats), checkpoint come text (JSON string).
CHECKPOINT_INDEX_MAPPING = {
    "mappings": {
        "properties": {
            "thread_id": {"type": "keyword"},
            "checkpoint_id": {"type": "keyword"},
            "checkpoint": {"type": "text"},
            "metadata": {
                "properties": {
                    "step": {
                        "type": "text",
                        "fields": {"keyword": {"type": "keyword"}},
                    }
                }
            },
        }
    }
}


#: Sort per step numerico decrescente. ``metadata.step`` e' text+keyword, quindi
#: ordinarlo/aggregarlo come keyword e' sbagliato: l'agg ``max`` viene rifiutata da OpenSearch e il
#: sort e' lessicografico ("9" > "10"). Stesso script painless usato dai 4 endpoint di ``server.py``.
STEP_DESC_SORT = [
    {
        "_script": {
            "type": "number",
            "script": {
                "source": """
                if (doc['metadata.step.keyword'].size() > 0) {
                    return Long.parseLong(doc['metadata.step.keyword'].value);
                }
                return -1;
            """,
                "lang": "painless",
            },
            "order": "desc",
        }
    }
]


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _title_of(document: dict) -> Optional[str]:
    """``conversation_title`` dal ``_source`` di un doc checkpoint."""
    checkpoint = json.loads(document.get("checkpoint", "{}"))
    return checkpoint.get("channel_values", {}).get("conversation_title")


def serialize_context(used_document_refs: list[DocumentRef]) -> list[dict]:
    """DocumentRef → forma letta da ``get_chat`` (``kwargs.metadata.*``)."""
    serialized = []
    for ref in used_document_refs:
        serialized.append(
            {
                "kwargs": {
                    "metadata": {
                        "document_id": ref.document_id,
                        "score": ref.score,
                        "title": ref.title,
                        "url": ref.url,
                        "domain": ref.domain,
                    }
                }
            }
        )
    return serialized


def serialize_messages(message_history) -> list:
    """History PydanticAI → JSON-able (per ``channel_values.messages``)."""
    from pydantic_core import to_jsonable_python

    return to_jsonable_python(message_history or [])


def _is_langchain_message(message) -> bool:
    """Un messaggio nel formato ``{"lc": 1, "id": [...], "kwargs": {...}}``, quello che
    agentic-rag-module salva nel canale ``messages`` del checkpoint."""
    return isinstance(message, dict) and "lc" in message and "kwargs" in message


def _text_of(content) -> str:
    """Testo di un ``content`` di agentic-rag-module: una stringa, o una lista di parti (multimodale)."""
    if isinstance(content, str):
        return content
    return "".join(
        part.get("text", "") if isinstance(part, dict) else str(part) for part in content or []
    )


def messages_from_langchain(data) -> list:
    """Messaggi di agentic-rag-module → ``list[ModelMessage]`` PydanticAI.

    Una chat cominciata con agentic-rag-module e continuata con questo modulo porta nel checkpoint
    i ``HumanMessage``/``AIMessage`` del grafo: diventano domanda e risposta del turno, come quelli
    di :func:`messages_from_chat_history`. Gli altri tipi (system, tool) non sono storia della
    conversazione e vengono saltati.
    """
    from pydantic_ai.messages import ModelRequest, ModelResponse, TextPart, UserPromptPart

    messages = []
    for message in data:
        kind = (message.get("id") or [""])[-1]
        text = _text_of(message["kwargs"].get("content"))
        if kind == "HumanMessage":
            messages.append(ModelRequest(parts=[UserPromptPart(content=text)]))
        elif kind == "AIMessage":
            messages.append(ModelResponse(parts=[TextPart(content=text)]))
    return messages


def deserialize_messages(data) -> list:
    """JSON-able → ``list[ModelMessage]`` PydanticAI (per riprendere la conversazione).

    Accetta anche i messaggi di una chat salvata da agentic-rag-module.
    """
    from pydantic_ai.messages import ModelMessagesTypeAdapter

    if not data:
        return []
    if _is_langchain_message(data[0]):
        return messages_from_langchain(data)
    return list(ModelMessagesTypeAdapter.validate_python(data))


def build_checkpoint(
    *,
    original_query: str,
    current_query: str,
    response: str,
    conversation_title: Optional[str],
    chat_sequence_number: int,
    retrieve_from_uploaded_documents: Optional[bool],
    used_document_refs: list[DocumentRef],
    message_history,
    ts: Optional[str] = None,
) -> dict:
    """Costruisce il dizionario checkpoint (pre-serializzazione JSON). Puro, testabile."""
    channel_values = {
        "original_query": original_query,
        "current_query": current_query,
        "response": response,
        "conversation_title": conversation_title,
        "chat_sequence_number": chat_sequence_number,
        "retrieve_from_uploaded_documents": retrieve_from_uploaded_documents,
        "context": serialize_context(used_document_refs),
        "messages": serialize_messages(message_history),
    }
    return {
        "ts": ts or _now_iso(),
        "channel_values": channel_values,
    }


class OpenSearchConversationStore:
    """Store per utenti loggati — scrive/legge la forma checkpoint su ``{tenant}-{user}``."""

    def __init__(self, opensearch_host: str, tenant_id: str, user_id: str):
        from app.utils.opensearch_client import get_opensearch_client

        self._client = get_opensearch_client(opensearch_host)
        self.index = f"{tenant_id}-{user_id}"

    def _ensure_index(self) -> None:
        if not self._client.indices.exists(index=self.index):
            self._client.indices.create(index=self.index, body=CHECKPOINT_INDEX_MAPPING)

    def _latest_doc(self, thread_id: str) -> Optional[dict]:
        """``_source`` del checkpoint a step massimo del thread, o ``None``."""
        if not self._client.indices.exists(index=self.index):
            return None
        response = self._client.search(
            index=self.index,
            body={
                "size": 1,
                "query": {"term": {"thread_id": thread_id}},
                "sort": STEP_DESC_SORT,
            },
        )
        hits = response.get("hits", {}).get("hits", [])
        return hits[0]["_source"] if hits else None

    def save_turn(
        self,
        *,
        chat_id: str,
        chat_sequence_number: int,
        original_query: str,
        current_query: str,
        response: str,
        conversation_title: Optional[str],
        retrieve_from_uploaded_documents: Optional[bool],
        used_document_refs: list[DocumentRef],
        message_history,
        ts: Optional[str] = None,
    ) -> None:
        self._ensure_index()
        latest = self._latest_doc(chat_id)
        step = int(latest.get("metadata", {}).get("step", 0)) + 1 if latest else 1
        # Il titolo e' calcolato solo al primo turno, ma ``get_user_chats``/``rename_chat``
        # leggono il doc a step massimo: va riportato avanti a ogni turno (in
        # agentic-rag-module lo faceva il canale di stato ``conversation_title``).
        if conversation_title is None and latest:
            conversation_title = _title_of(latest)
        checkpoint = build_checkpoint(
            original_query=original_query,
            current_query=current_query,
            response=response,
            conversation_title=conversation_title,
            chat_sequence_number=chat_sequence_number,
            retrieve_from_uploaded_documents=retrieve_from_uploaded_documents,
            used_document_refs=used_document_refs,
            message_history=message_history,
            ts=ts,
        )
        document = {
            "thread_id": chat_id,
            "checkpoint_id": uuid.uuid4().hex,
            "checkpoint": json.dumps(checkpoint),
            "metadata": {"step": str(step)},
        }
        self._client.index(index=self.index, body=document, refresh=True)

    def load_message_history(self, chat_id: str) -> list:
        """Ricostruisce la history PydanticAI dall'ultimo checkpoint (max step) del thread."""
        latest = self._latest_doc(chat_id)
        if not latest:
            return []
        checkpoint = json.loads(latest.get("checkpoint", "{}"))
        return deserialize_messages(checkpoint.get("channel_values", {}).get("messages"))


def messages_from_chat_history(chat_history) -> list:
    """``chatHistory`` del frontend → ``list[ModelMessage]`` PydanticAI.

    Port di ``load_messages_from_frontend``: tollerante come l'originale, una entry incompleta
    viene saltata invece di far fallire il turno.
    """
    from pydantic_ai.messages import ModelRequest, ModelResponse, TextPart, UserPromptPart

    messages = []
    for item in chat_history or []:
        question = item.get("question")
        answer = item.get("answer")
        if question is None or answer is None:
            continue
        messages.append(ModelRequest(parts=[UserPromptPart(content=question)]))
        messages.append(ModelResponse(parts=[TextPart(content=answer)]))
    return messages


class FrontendConversationStore:
    """Store per utenti non loggati: la conversazione la porta il client a ogni richiesta.

    Sostituisce un precedente store in-memory che teneva la history lato server sotto un'unica
    chiave (``chat_id`` e' ``None`` per gli anonimi), e quindi la condivideva fra utenti diversi.
    La sorgente corretta e' il payload, come in agentic-rag-module: nessuno stato lato server, gli
    anonimi restano isolati e la history non dipende da quale replica risponde.
    """

    def __init__(self, chat_history=None):
        self._messages = messages_from_chat_history(chat_history)

    def save_turn(self, **_ignored) -> None:
        """No-op: la history del turno successivo la rimanda il client."""

    def load_message_history(self, chat_id: str) -> list:
        return list(self._messages)
