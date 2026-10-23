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
Streaming reale: i CHUNK escono mentre il provider genera, non a risposta completa.

Il punto che questi test bloccano e' la proprieta' che prima non esisteva: **il primo CHUNK e'
emesso prima che l'ultimo delta sia stato prodotto**. Con l'affettamento di un messaggio completo
era impossibile per costruzione.

``coalesce`` e' testata a parte perche' regge un invariante che non riguarda solo l'estetica: la
dimensione del payload che vede il frontend resta quella di prima.
"""

import asyncio
import json

from app.rag.agentic.streaming import CHUNK_SIZE, coalesce, stream_answer


async def _collect(deltas, **kwargs):
    events = []
    async for event in stream_answer(
        deltas=deltas,
        rag_type="CHAT_RAG",
        post_stream=kwargs.pop("post_stream", _no_post_stream),
        **kwargs,
    ):
        events.append(json.loads(event))
    return events


async def _no_post_stream():
    return None, False, []


async def _agen(items):
    for item in items:
        yield item


def test_coalesce_groups_small_deltas_to_chunk_size():
    """Delta da 4 caratteri (ordine di grandezza di un provider reale) → fette da CHUNK_SIZE."""

    async def scenario():
        deltas = ["abcd"] * 25  # 100 caratteri
        return [piece async for piece in coalesce(_agen(deltas))]

    pieces = asyncio.run(scenario())

    assert [len(p) for p in pieces] == [CHUNK_SIZE, CHUNK_SIZE, 20]
    assert "".join(pieces) == "abcd" * 25


def test_coalesce_splits_delta_larger_than_chunk_size():
    """Un delta piu' grande di CHUNK_SIZE non deve produrre un CHUNK sovradimensionato."""

    async def scenario():
        return [piece async for piece in coalesce(_agen(["y" * 95]))]

    pieces = asyncio.run(scenario())

    assert [len(p) for p in pieces] == [CHUNK_SIZE, CHUNK_SIZE, 15]


def test_first_chunk_is_emitted_before_generation_ends():
    """La proprieta' centrale dello streaming reale: si emette senza aspettare l'ultimo delta."""
    prodotti: list = []
    primo_chunk_dopo: list = []

    async def deltas():
        for i in range(10):
            frammento = "z" * CHUNK_SIZE
            prodotti.append(i)
            yield frammento

    async def scenario():
        async for event in stream_answer(
            deltas=deltas(),
            rag_type="CHAT_RAG",
            post_stream=_no_post_stream,
        ):
            if json.loads(event)["type"] == "CHUNK" and not primo_chunk_dopo:
                # quanti delta erano stati prodotti quando il primo CHUNK e' uscito
                primo_chunk_dopo.append(len(prodotti))

    asyncio.run(scenario())

    assert primo_chunk_dopo == [1], (
        f"primo CHUNK emesso dopo {primo_chunk_dopo} delta: l'emissione attende la fine"
    )
    assert len(prodotti) == 10


def test_post_stream_runs_after_chunks_and_provides_tail():
    """Titolo e documenti arrivano da post_stream, chiamata a testo completo, e chiudono lo stream."""
    ordine: list = []

    async def deltas():
        for _ in range(3):
            yield "w" * CHUNK_SIZE
        ordine.append("delta-esauriti")

    async def post_stream():
        ordine.append("post_stream")
        return "Titolo", True, []

    async def scenario():
        return [
            json.loads(e)["type"]
            async for e in stream_answer(
                deltas=deltas(), rag_type="CHAT_RAG", post_stream=post_stream
            )
        ]

    tipi = asyncio.run(scenario())

    assert ordine == ["delta-esauriti", "post_stream"]
    assert tipi == ["START", "CHUNK", "CHUNK", "CHUNK", "TITLE", "END"]
