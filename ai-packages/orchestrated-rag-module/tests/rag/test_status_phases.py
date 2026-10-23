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
Test delle fasi SSE ``STATUS`` (planning / discovering / retrieving / generating).

Il punto delicato non e' *quali* fasi esistono ma **quando escono**: ``discover`` e ``retriever``
girano dentro ``agent.run``, parecchi frame sotto il generator che produce gli eventi SSE, quindi
consegnano la fase a ``deps.status_sink`` e l'entrypoint la relaia mentre la run procede. Se il
relay non fosse concorrente le fasi uscirebbero tutte insieme a cose gia' fatte — indistinguibili
da nessuna fase. I test qui sotto verificano proprio quello.

Nessun OpenSearch, nessun provider reale.
"""

import asyncio
import json
from unittest.mock import MagicMock

from pydantic_ai.models.test import TestModel

from app.rag.agentic import entrypoint, orchestrator, streaming
from app.rag.agentic.deps import AgenticDeps


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="emb:9000",
        tenant_id="t1",
    )


# --------------------------------------------------------------------------- #
# emit_status: canale opzionale, una fase per run                             #
# --------------------------------------------------------------------------- #
def test_emit_status_is_noop_without_sink():
    """Senza sink (test offline, deps costruite a mano) pubblicare non deve esplodere."""
    deps = _deps()

    deps.emit_status("discovering")  # nessun sink: no-op

    assert deps.status_sink is None


def test_emit_status_publishes_each_phase_once():
    """Il fan-out invoca i tool N volte: la stessa fase non deve uscire N volte."""
    deps = _deps()
    fasi: list = []
    deps.status_sink = fasi.append

    deps.emit_status("discovering")
    deps.emit_status("retrieving")
    deps.emit_status("discovering")  # secondo agente del fan-out
    deps.emit_status("retrieving")

    assert fasi == ["discovering", "retrieving"]


# --------------------------------------------------------------------------- #
# relay_until: il relay e' concorrente, non posticipato                        #
# --------------------------------------------------------------------------- #
def test_status_events_are_relayed_while_the_run_is_still_going():
    """Una fase pubblicata a meta' run deve uscire PRIMA che la run finisca.

    E' la proprieta' che distingue un indicatore di avanzamento da un riepilogo a posteriori.
    """
    coda: asyncio.Queue = asyncio.Queue()
    ordine: list = []

    # sleep reali, non sleep(0): i tool sono I/O-bound (LLM + OpenSearch) e la proprieta' da
    # verificare e' che il drain si inserisca in quelle attese. Con sleep(0) la run finirebbe
    # prima che il drain riesca a piazzare la sua get, misurando lo scheduler e non il codice.
    async def run():
        await asyncio.sleep(0.02)
        coda.put_nowait(streaming.status_event("discovering"))
        await asyncio.sleep(0.02)
        coda.put_nowait(streaming.status_event("retrieving"))
        await asyncio.sleep(0.02)
        ordine.append("run-conclusa")

    async def scenario():
        task = asyncio.ensure_future(run())
        async for evento in streaming.relay_until(coda, task):
            ordine.append(json.loads(evento)["chunk"])

    asyncio.run(scenario())

    assert ordine == ["discovering", "retrieving", "run-conclusa"]


def test_status_events_published_at_the_very_end_are_not_lost():
    """Una fase pubblicata a ridosso della conclusione va emessa comunque (drain finale)."""
    coda: asyncio.Queue = asyncio.Queue()

    async def run():
        coda.put_nowait(streaming.status_event("retrieving"))

    async def scenario():
        task = asyncio.ensure_future(run())
        await task  # la run finisce prima ancora che il relay parta
        return [json.loads(e)["chunk"] async for e in streaming.relay_until(coda, task)]

    assert asyncio.run(scenario()) == ["retrieving"]


# --------------------------------------------------------------------------- #
# I tool pubblicano davvero le proprie fasi                                    #
# --------------------------------------------------------------------------- #
def test_discover_and_retriever_publish_their_phases(monkeypatch):
    """``TestModel`` invoca i tool registrati: discover -> discovering, retriever -> retrieving."""
    deps = _deps()
    fasi: list = []
    deps.status_sink = fasi.append
    deps.discovery_model = TestModel()
    monkeypatch.setattr(deps, "retrieve_fn", lambda **kw: [])

    asyncio.run(
        orchestrator.run_orchestrator(
            question="chi e' il RUP?", deps=deps, model=TestModel()
        )
    )

    assert fasi == ["discovering", "retrieving"]


# --------------------------------------------------------------------------- #
# Sequenza completa vista dal frontend                                         #
# --------------------------------------------------------------------------- #
def _status_sequence(monkeypatch, *, tools_run: bool):
    """Guida ``run_turn`` sul path bufferizzato e ritorna le sole fasi STATUS emesse."""
    from app.rag.agentic.contracts import OrchestratorOutput

    async def fake_run_orchestrator(*, question, deps, message_history=None, **kw):
        if tools_run:
            await asyncio.sleep(0.02)
            deps.emit_status("discovering")
            await asyncio.sleep(0.02)
            deps.emit_status("retrieving")
            await asyncio.sleep(0.02)
        return (
            OrchestratorOutput(status="ok", message="risposta", used_document_ids=[]),
            [],
            MagicMock(new_messages=lambda: []),
        )

    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)

    eventi = []

    async def scenario():
        async for raw in entrypoint.run_turn(
            rag_type="CHAT_RAG",
            search_text="domanda",
            # enable_verifier forza il path bufferizzato; run_orchestrator e' comunque stubbato
            deps=_deps(enable_verifier=True),
            store=MagicMock(),
        ):
            eventi.append(json.loads(raw))

    asyncio.run(scenario())
    return [e["chunk"] for e in eventi if e["type"] == "STATUS"]


def test_phase_sequence_seen_by_the_frontend(monkeypatch):
    assert _status_sequence(monkeypatch, tools_run=True) == [
        "planning",
        "discovering",
        "retrieving",
        "generating",
    ]


def test_no_discovery_means_no_discovering_phase(monkeypatch):
    """Small talk: l'orchestratore risponde senza delegare → non si annuncia una ricerca."""
    assert _status_sequence(monkeypatch, tools_run=False) == ["planning", "generating"]


def test_phase_sequence_on_the_streaming_path():
    """Il percorso di default, in streaming: le fasi dei tool escono prima del primo CHUNK."""
    from app.rag.agentic.persistence import FrontendConversationStore

    deps = _deps()
    deps.orchestrator_model = TestModel()
    deps.discovery_model = TestModel()
    deps.retrieve_fn = lambda **_: [{"document_id": "doc-1", "content": "testo"}]

    async def scenario():
        return [
            json.loads(raw)
            async for raw in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="chi e' il RUP?",
                deps=deps,
                store=FrontendConversationStore(None),
            )
        ]

    eventi = asyncio.run(scenario())
    tipi = [e["type"] for e in eventi]

    assert [e["chunk"] for e in eventi if e["type"] == "STATUS"] == [
        "planning",
        "discovering",
        "retrieving",
        "generating",
    ]
    assert tipi.index("START") > max(i for i, t in enumerate(tipi) if t == "STATUS")
