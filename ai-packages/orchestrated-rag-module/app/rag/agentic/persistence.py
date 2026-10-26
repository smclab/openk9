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
Chat history in the documents of agentic-rag-module.

The module saves each turn in the same document shape as the checkpointer of
agentic-rag-module. Four endpoints of ``app/server.py`` (``get_user_chats``, ``get_chat``,
``rename_chat``, ``delete_chat``) and the retention scheduler
(``app/utils/chat_history.py::delete_documents``) read and write that shape directly: keeping it
leaves them unchanged, and a chat goes on from one module to the other.

The shape, as the code that reads it expects:

- index ``{tenant_id}-{user_id}``; ``thread_id`` is the ``chat_id``.
- ``checkpoint`` is a **JSON string** with ``ts`` and ``channel_values.{original_query,
  current_query, response, conversation_title, chat_sequence_number,
  retrieve_from_uploaded_documents, context, messages}``.
- ``context`` is a list of documents ``{"kwargs": {"metadata": {document_id, score, title,
  url, domain?}}}``: ``get_chat`` reads ``kwargs.metadata.*``.
- ``metadata.step`` (>0): ``get_chat`` and ``get_user_chats`` take the document with the highest
  step, per ``chat_sequence_number`` and per thread. ``get_user_chats`` aggregates on
  ``thread_id`` (keyword) and sorts with painless on ``metadata.step.keyword``, hence the
  explicit mapping below.
- ``checkpoint_id``: a unique id per document, which ``delete_documents`` needs to recognize
  the index.

``channel_values.messages`` holds the serialized PydanticAI history (round trip through
``ModelMessagesTypeAdapter``), or, in a chat started with agentic-rag-module, its messages.

``tests/rag/test_chat_persistence.py`` runs the store against OpenSearch when
``OPENSEARCH_TEST_HOST`` is set.
"""

import json
import uuid
from datetime import datetime, timezone
from typing import Optional

from app.rag.agentic.contracts import DocumentRef

#: Explicit mapping: ``thread_id`` as keyword (aggregated as "thread_id"), ``metadata.step`` with
#: a ``.keyword`` subfield (used by the painless of get_user_chats), ``checkpoint`` as text (JSON
#: string).
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


#: Sort by numeric step, descending. ``metadata.step`` is text plus keyword, so sorting or
#: aggregating it as keyword is wrong: OpenSearch rejects the ``max`` aggregation and the sort is
#: lexicographic ("9" > "10"). The same painless script as the 4 endpoints of ``server.py``.
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
    """``conversation_title`` from the ``_source`` of a checkpoint document."""
    checkpoint = json.loads(document.get("checkpoint", "{}"))
    return checkpoint.get("channel_values", {}).get("conversation_title")


def serialize_context(used_document_refs: list[DocumentRef]) -> list[dict]:
    """DocumentRef → the shape ``get_chat`` reads (``kwargs.metadata.*``)."""
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
    """PydanticAI history → JSON-able (for ``channel_values.messages``).

    The instructions of the requests are left out: an agent sends only its own, never those of
    the past turns, so saved they would only repeat the prompt in every request of every turn.
    """
    from dataclasses import replace

    from pydantic_ai.messages import ModelRequest
    from pydantic_core import to_jsonable_python

    messages = [
        replace(m, instructions=None) if isinstance(m, ModelRequest) else m
        for m in message_history or []
    ]
    return to_jsonable_python(messages)


def _is_agentic_rag_message(message) -> bool:
    """A message in the ``{"lc": 1, "id": [...], "kwargs": {...}}`` format, the one
    agentic-rag-module saves in the ``messages`` channel of the checkpoint."""
    return isinstance(message, dict) and "lc" in message and "kwargs" in message


def _text_of(content) -> str:
    """Text of an agentic-rag-module ``content``: a string, or a list of parts (multimodal)."""
    if isinstance(content, str):
        return content
    return "".join(
        part.get("text", "") if isinstance(part, dict) else str(part) for part in content or []
    )


def messages_from_agentic_rag(data) -> list:
    """Messages of agentic-rag-module → PydanticAI ``list[ModelMessage]``.

    A chat started with agentic-rag-module and continued with this module carries in its
    checkpoint the ``HumanMessage``/``AIMessage`` of the graph: they become question and answer
    of each turn, as in :func:`messages_from_chat_history`. The other types (system, tool) are
    not history of the conversation and are skipped.
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
    """JSON-able → PydanticAI ``list[ModelMessage]`` (to resume the conversation).

    Also accepts the messages of a chat saved by agentic-rag-module.
    """
    from pydantic_ai.messages import ModelMessagesTypeAdapter

    if not data:
        return []
    if _is_agentic_rag_message(data[0]):
        return messages_from_agentic_rag(data)
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
    evaluation_fields: Optional[dict] = None,
) -> dict:
    """The checkpoint dictionary, before JSON serialization."""
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
    # un turno con i marcatori salva, come agentic-rag-module, le citazioni per id (che get_chat
    # usa per tenere solo le fonti citate) e il numero di ogni fonte (per i [n] della risposta)
    numbered = [ref for ref in used_document_refs if ref.ordinal is not None]
    if numbered:
        channel_values["citations"] = {ref.document_id: ref.quotes or [] for ref in numbered}
        channel_values["citation_ordinals"] = {
            str(ref.ordinal): ref.document_id for ref in numbered
        }
    # gli esiti della valutazione in tempo reale, nei campi di agentic-rag-module
    channel_values.update(evaluation_fields or {})
    return {
        "ts": ts or _now_iso(),
        "channel_values": channel_values,
    }


class OpenSearchConversationStore:
    """Store for logged users: writes and reads the checkpoint shape in ``{tenant}-{user}``."""

    def __init__(self, opensearch_host: str, tenant_id: str, user_id: str):
        from app.utils.opensearch_client import get_opensearch_client

        self._client = get_opensearch_client(opensearch_host)
        self.index = f"{tenant_id}-{user_id}"

    def _ensure_index(self) -> None:
        if not self._client.indices.exists(index=self.index):
            self._client.indices.create(index=self.index, body=CHECKPOINT_INDEX_MAPPING)

    def _latest_doc(self, thread_id: str) -> Optional[dict]:
        """``_source`` of the checkpoint with the highest step of the thread, or ``None``."""
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
        evaluation_fields: Optional[dict] = None,
    ) -> None:
        self._ensure_index()
        latest = self._latest_doc(chat_id)
        step = int(latest.get("metadata", {}).get("step", 0)) + 1 if latest else 1
        # The title is generated only on the first turn, but ``get_user_chats`` and
        # ``rename_chat`` read the document with the highest step: it is carried over at every
        # turn (in agentic-rag-module the ``conversation_title`` state channel did it).
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
            evaluation_fields=evaluation_fields,
        )
        document = {
            "thread_id": chat_id,
            "checkpoint_id": uuid.uuid4().hex,
            "checkpoint": json.dumps(checkpoint),
            "metadata": {"step": str(step)},
        }
        self._client.index(index=self.index, body=document, refresh=True)

    def load_message_history(self, chat_id: str) -> list:
        """The PydanticAI history from the latest checkpoint (highest step) of the thread."""
        latest = self._latest_doc(chat_id)
        if not latest:
            return []
        checkpoint = json.loads(latest.get("checkpoint", "{}"))
        return deserialize_messages(checkpoint.get("channel_values", {}).get("messages"))


def messages_from_chat_history(chat_history) -> list:
    """``chatHistory`` of the frontend → PydanticAI ``list[ModelMessage]``.

    As ``load_messages_from_frontend`` of agentic-rag-module, an incomplete entry is skipped
    instead of failing the turn.
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
    """Store for anonymous users: the client sends the conversation with every request.

    As in agentic-rag-module, the history comes from the payload and nothing is kept on the
    server: anonymous users stay isolated (their ``chat_id`` is ``None``), and the history does
    not depend on which replica answers.
    """

    def __init__(self, chat_history=None):
        self._messages = messages_from_chat_history(chat_history)

    def save_turn(self, **_ignored) -> None:
        """No-op: the client sends the history again with the next turn."""

    def load_message_history(self, chat_id: str) -> list:
        return list(self._messages)
