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

"""Le deleghe al Discovery hanno un tetto per turno.

Senza ``max_iterations`` nel jsonConfig non c'e' ``UsageLimits``: l'orchestratore poteva chiamare
``discover`` senza limite, e ogni Discovery costa fino a ``MAX_RETRIEVER_CALLS`` ricerche piu' una
selezione.
"""

import asyncio

from pydantic_ai.messages import ModelResponse, ToolCallPart, ToolReturnPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.rag.agentic import orchestrator
from app.rag.agentic.contracts import DiscoveryUnanswerable
from app.rag.agentic.deps import AgenticDeps

FINAL = {"message": "risposta", "used_document_ids": [], "status": "ok"}


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _stub_discovery(monkeypatch):
    searched = []

    async def fake_run_discovery(*, request, deps, usage=None, model=None):
        searched.append(request.question)
        return DiscoveryUnanswerable(reason="stub")

    monkeypatch.setattr(orchestrator.discovery, "run_discovery", fake_run_discovery)
    return searched


def _run(deps, model_fn):
    return asyncio.run(
        orchestrator.run_orchestrator(
            question="domanda", deps=deps, model=FunctionModel(model_fn)
        )
    )


def _final(info: AgentInfo) -> ModelResponse:
    return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, FINAL)])


def test_discover_stops_at_the_turn_budget(monkeypatch):
    searched = _stub_discovery(monkeypatch)
    returns = []

    def keeps_delegating(messages, info: AgentInfo) -> ModelResponse:
        last = messages[-1].parts[-1]
        if isinstance(last, ToolReturnPart):
            returns.append(last.content)
            if last.content == orchestrator.DISCOVERY_LIMIT_REACHED:
                return _final(info)
        return ModelResponse(
            parts=[ToolCallPart("discover", {"question": f"q{len(searched)}"})]
        )

    output, _, _ = _run(_deps(), keeps_delegating)

    assert len(searched) == orchestrator.MAX_DISCOVERIES_PER_TURN
    assert returns[-1] == orchestrator.DISCOVERY_LIMIT_REACHED
    assert output.status == "ok"
