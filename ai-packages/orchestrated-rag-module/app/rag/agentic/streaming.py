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
SSE events of an answer, with the protocol of agentic-rag-module: ``START``,
``CHUNK``, ``DOCUMENT``, ``ERROR`` and ``END``.

:func:`stream_answer` consumes an async iterator of text deltas: the deltas of
the provider while the orchestrator writes (:func:`coalesce`), or a complete
message cut in pieces (:func:`deltas_from_message`).
"""

import json
from typing import Awaitable, Callable, Iterable

CHUNK_SIZE = 40


def _sse(chunk, event_type: str) -> str:
    return json.dumps({"chunk": chunk, "type": event_type})


async def deltas_from_message(message: str, size: int = CHUNK_SIZE):
    """Cut a complete message in deltas, so that :func:`stream_answer` has a
    single implementation for both sources."""
    for start in range(0, len(message), size):
        yield message[start : start + size]


async def coalesce(deltas, size: int = CHUNK_SIZE):
    """
    Group the deltas of the provider into pieces of ``size`` characters.

    The real deltas are a few characters each: sending them one by one would
    multiply the CHUNK events on the wire, and the checks of the output
    guardrail, which counts chunks rather than characters.
    """
    buffer = ""
    async for delta in deltas:
        buffer += delta
        while len(buffer) >= size:
            yield buffer[:size]
            buffer = buffer[size:]
    if buffer:
        yield buffer


def document_events(refs) -> Iterable[str]:
    """One DOCUMENT event per cited document, with the fields it has."""
    seen = set()
    for ref in refs:
        if not ref.document_id or ref.document_id in seen:
            continue
        seen.add(ref.document_id)
        chunk = {"citations": []}
        for field in ("score", "title", "url", "domain"):
            if (value := getattr(ref, field)) is not None:
                chunk[field] = value
        yield _sse(chunk, "DOCUMENT")


async def stream_answer(
    *,
    deltas,
    rag_type: str,
    post_stream: Callable[[], Awaitable[tuple]],
):
    """
    Stream the answer: START, the CHUNK events, then TITLE, DOCUMENT and END.

    :param deltas: The async iterator of the text of the answer.
    :param rag_type: The RAG type of the request; ``SIMPLE_GENERATE`` sends no
        DOCUMENT event.
    :param post_stream: A coroutine called once the text is complete, returning
        ``(title, emit_title, refs)``: with real deltas the title and the cited
        documents are known only at the end.
    """
    start = True
    async for piece in deltas:
        if start:
            yield _sse("", "START")
            start = False
        yield _sse(piece, "CHUNK")

    title, emit_title, refs = await post_stream()

    if emit_title and title:
        yield _sse(title, "TITLE")
    if rag_type != "SIMPLE_GENERATE":
        for event in document_events(refs):
            yield event
    yield _sse("", "END")
