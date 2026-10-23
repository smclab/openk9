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
SSE events of an answer, with the protocol of agentic-rag-module: ``SOURCES``,
``START``, ``CHUNK``, ``DOCUMENT``, ``CANCEL``, ``ERROR`` and ``END``, plus
``STATUS``, the phase of the turn before the answer starts: ``planning``,
``discovering``, ``retrieving``, ``evaluating`` (only with the Verifier),
``generating``.

:func:`stream_answer` consumes an async iterator of text deltas: the deltas of
the provider while the orchestrator writes (:func:`coalesce`), or a complete
message cut in pieces (:func:`deltas_from_message`).
"""

import asyncio
import json
from typing import Awaitable, Callable, Iterable, Optional

from app.utils.logger import get_logger

logger = get_logger(__name__)

CHUNK_SIZE = 40

#: The CANCEL text of an answer blocked by the output guardrail, as in agentic-rag-module.
OUTPUT_GUARDRAIL_CANCEL_MESSAGE = "Inappropriate content"


def _sse(chunk, event_type: str) -> str:
    return json.dumps({"chunk": chunk, "type": event_type})


def status_event(phase: str) -> str:
    """The STATUS event of ``phase``."""
    return _sse(phase, "STATUS")


async def relay_until(queue, producer):
    """
    Relay the SSE events that ``producer`` queues until that task ends.

    The ``discover`` and ``retriever`` tools run inside ``agent.run``, several
    frames below this generator: they cannot yield an event, so they queue it.
    The whole work stays in the ``producer`` task, because ``agent.run_stream``
    opens an anyio cancel scope that must be closed in the task that opened it.

    If the generator is closed early (the client went away) the producer is
    cancelled, rather than left filling a queue nobody reads.
    """
    try:
        while not producer.done():
            getter = asyncio.ensure_future(queue.get())
            done, _ = await asyncio.wait({getter, producer}, return_when=asyncio.FIRST_COMPLETED)
            if getter in done:
                yield getter.result()
            else:
                getter.cancel()
        while not queue.empty():
            yield queue.get_nowait()
    finally:
        if not producer.done():
            producer.cancel()
    await producer  # raise the exception of the producer instead of swallowing it


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
    """One DOCUMENT event per cited document, with the fields it has: the number the answer
    marks it with, and the quotes taken from it, when there are any."""
    seen = set()
    for ref in refs:
        if not ref.document_id or ref.document_id in seen:
            continue
        seen.add(ref.document_id)
        chunk = {} if ref.ordinal is None else {"ordinal": ref.ordinal}
        chunk["citations"] = [{"quote": quote} for quote in ref.quotes or []]
        for field in ("score", "title", "url", "domain"):
            if (value := getattr(ref, field)) is not None:
                chunk[field] = value
        yield _sse(chunk, "DOCUMENT")


async def _guarded_chunks(
    deltas,
    *,
    output_guardrail_type: Optional[int],
    chunk_interval: int,
    output_check: Optional[Callable[[str, str], Awaitable[str]]],
    scope_check: Optional[Callable[[str], Awaitable[str]]],
    scope_gate_prefix_chars: int,
    scope_gate_redirect_message: str,
):
    """
    The CHUNK events of the answer under the output guardrail of the tenant, as
    in agentic-rag-module. A blocked answer ends with a CANCEL event.

    - type 1: the chunks are held back ``chunk_interval`` at a time, and sent only
      after the answer so far passes the check;
    - type 2: the chunks are sent as they come, and every ``chunk_interval``
      chunks the last window of text is checked;
    - type 3: the scope gate holds the first ``scope_gate_prefix_chars``
      characters back for a single check, then the rest flows freely.
    """
    if output_guardrail_type is None:
        async for piece in deltas:
            yield _sse(piece, "CHUNK")
        return

    async def blocked(answer: str, stage: str) -> bool:
        outcome = await output_check(answer, stage)
        return bool(outcome) and outcome != "NONE"

    cancel = _sse(OUTPUT_GUARDRAIL_CANCEL_MESSAGE, "CANCEL")
    answer = ""
    held: list = []
    if output_guardrail_type == 3:
        checked = False
        async for piece in deltas:
            if checked:
                yield _sse(piece, "CHUNK")
                continue
            answer += piece
            held.append(piece)
            if len(answer) >= scope_gate_prefix_chars:
                if await scope_check(answer) == "OFF_SCOPE":
                    yield _sse(scope_gate_redirect_message, "CANCEL")
                    return
                for chunk in held:
                    yield _sse(chunk, "CHUNK")
                held, checked = [], True
        # an answer shorter than the prefix is checked as it is
        if held:
            if await scope_check(answer) == "OFF_SCOPE":
                yield _sse(scope_gate_redirect_message, "CANCEL")
                return
            for chunk in held:
                yield _sse(chunk, "CHUNK")
    elif output_guardrail_type == 2:
        count = 0
        async for piece in deltas:
            count += 1
            answer += piece
            if count == chunk_interval:
                is_blocked = await blocked(answer, "chunk_interval")
                count, answer = 0, ""
                if is_blocked:
                    yield cancel
                    return
            yield _sse(piece, "CHUNK")
        if count and await blocked(answer, "final_tail"):
            yield cancel
            return
    else:
        async for piece in deltas:
            answer += piece
            held.append(piece)
            if len(held) == chunk_interval:
                if await blocked(answer, "chunk_interval"):
                    yield cancel
                    return
                for chunk in held:
                    yield _sse(chunk, "CHUNK")
                held = []
        if held:
            if await blocked(answer, "final_tail"):
                yield cancel
                return
            for chunk in held:
                yield _sse(chunk, "CHUNK")


async def stream_answer(
    *,
    deltas,
    rag_type: str,
    post_stream: Callable[[], Awaitable[tuple]],
    output_guardrail_type: Optional[int] = None,
    chunk_interval: int = 10,
    output_check: Optional[Callable[[str, str], Awaitable[str]]] = None,
    scope_check: Optional[Callable[[str], Awaitable[str]]] = None,
    scope_gate_prefix_chars: int = 250,
    scope_gate_redirect_message: str = "",
    sources: Optional[Callable[[], list]] = None,
):
    """
    Stream the answer: STATUS ``generating``, SOURCES, START, the CHUNK events,
    then TITLE, DOCUMENT and END.

    With the output guardrail the CHUNK events pass its checks first: an answer
    it blocks ends with a CANCEL event, and ``post_stream`` is not called, so
    the answer is not saved.

    :param deltas: The async iterator of the text of the answer.
    :param rag_type: The RAG type of the request; ``SIMPLE_GENERATE`` sends no
        DOCUMENT event.
    :param post_stream: A coroutine called once the text is complete, returning
        ``(title, emit_title, refs)``: with real deltas the title and the cited
        documents are known only at the end.
    :param output_guardrail_type: ``None`` without the output guardrail,
        otherwise its type: 1, 2 or 3 (scope gate). An unknown type is treated
        as 1, which holds the text back until it is checked.
    :param output_check: ``(answer, stage) -> category or NONE``, for types 1 and 2.
    :param scope_check: ``(answer prefix) -> VALID or OFF_SCOPE``, for type 3.
    :param sources: Returns the numbered sources of the turn, read when the
        answer starts: they are all known by then, since the tool round ends
        before the model writes.
    """
    yield status_event("generating")
    if output_guardrail_type is not None and output_guardrail_type not in (1, 2, 3):
        logger.warning(
            f"[streaming] output_guardrail_type={output_guardrail_type!r} unknown "
            "(expected 1, 2 or 3): checked as type 1"
        )
        output_guardrail_type = 1
    start = True
    async for event in _guarded_chunks(
        deltas,
        output_guardrail_type=output_guardrail_type,
        chunk_interval=chunk_interval,
        output_check=output_check,
        scope_check=scope_check,
        scope_gate_prefix_chars=scope_gate_prefix_chars,
        scope_gate_redirect_message=scope_gate_redirect_message,
    ):
        if start:
            if sources is not None and (numbered := sources()):
                yield _sse(numbered, "SOURCES")
            yield _sse("", "START")
            start = False
        yield event
        if json.loads(event)["type"] == "CANCEL":
            return

    title, emit_title, refs = await post_stream()

    if emit_title and title:
        yield _sse(title, "TITLE")
    if rag_type != "SIMPLE_GENERATE":
        for event in document_events(refs):
            yield event
    yield _sse("", "END")
