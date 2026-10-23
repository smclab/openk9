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
Test di ``bypass_rag`` (port di ``rag_router_node`` che forzava ``use_rag=False``).

Nel path PydanticAI il bypass non e' una decisione dell'LLM ma un fatto strutturale: con il flag
attivo l'orchestratore viene costruito **senza** il tool ``discover``, quindi il retrieval e'
irraggiungibile. ``TestModel`` invoca tutti i tool registrati, quindi basta far esplodere
``run_discovery`` per dimostrare che la delega non e' nemmeno esposta al modello.

Nessun OpenSearch, nessun provider reale.
"""

import asyncio

from pydantic_ai.models.test import TestModel

from app.rag.agentic import orchestrator
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


def _run(deps):
    return asyncio.run(
        orchestrator.run_orchestrator(
            question="chi e' il RUP?", deps=deps, model=TestModel()
        )
    )


# --------------------------------------------------------------------------- #
# Il flag rende il retrieval irraggiungibile                                   #
# --------------------------------------------------------------------------- #
def test_bypass_rag_never_reaches_discovery(monkeypatch):
    async def _boom(**kw):
        raise AssertionError("con bypass_rag il Discovery non deve essere raggiungibile")

    monkeypatch.setattr(orchestrator.discovery, "run_discovery", _boom)

    output, refs, _ = _run(_deps(bypass_rag=True))

    assert output.status == "ok"
    assert refs == []  # nessun documento recuperato -> nessuna citazione


def test_without_bypass_rag_discovery_is_delegated(monkeypatch):
    calls = []

    async def _record(*, request, deps, usage=None, model=None):
        calls.append(request.question)
        return orchestrator.DiscoveryUnanswerable(reason="stub")

    monkeypatch.setattr(orchestrator.discovery, "run_discovery", _record)

    _run(_deps(bypass_rag=False))

    assert calls, "senza bypass_rag il tool discover deve essere esposto e invocato"


# --------------------------------------------------------------------------- #
# System prompt: con bypass non si puo' usare quello che istruisce a delegare   #
# --------------------------------------------------------------------------- #
def test_bypass_rag_prompt_uses_tenant_prompt_no_rag():
    prompt = orchestrator._build_system_prompt(
        bypass_rag=True, prompt_no_rag="Rispondi in italiano."
    )

    assert prompt.startswith("Rispondi in italiano.")
    assert "discover" not in prompt


def test_bypass_rag_prompt_falls_back_to_default():
    prompt = orchestrator._build_system_prompt(
        bypass_rag=True, prompt_no_rag=None
    )

    assert prompt.startswith(orchestrator.DEFAULT_BYPASS_RAG_PROMPT)
    assert "discover" not in prompt


def test_without_bypass_rag_prompt_is_the_orchestrator_one():
    prompt = orchestrator._build_system_prompt(
        bypass_rag=False, prompt_no_rag="ignorato"
    )

    assert prompt == orchestrator.ORCHESTRATOR_SYSTEM_PROMPT
