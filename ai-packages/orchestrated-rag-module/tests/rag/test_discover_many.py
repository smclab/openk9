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
"""``discover_many``: il fan-out dell'orchestratore con ``parallel_discovery`` attivo.

Le sotto-domande partono insieme, ognuna con il suo Discovery; oltre ``max_parallel_subtasks`` e
oltre il tetto di deleghe del turno vengono scartate, e l'orchestratore lo sa; un Discovery che
fallisce non ferma gli altri.
"""

import asyncio

from pydantic_ai.messages import ModelResponse, ToolCallPart, ToolReturnPart
from pydantic_ai.models.function import AgentInfo, FunctionModel
from pydantic_ai.models.test import TestModel

from app.rag.agentic import discovery, orchestrator
from app.rag.agentic.contracts import (
    DiscoveredDocument,
    DiscoveryResult,
    DiscoveryUnanswerable,
    SubAgentRequest,
)
from app.rag.agentic.deps import AgenticDeps


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _result(document_id):
    return DiscoveryResult(
        documents=[DiscoveredDocument(document_id=document_id, content=f"testo {document_id}")]
    )


def _fan_out(questions):
    """Orchestratore che delega ``questions`` con una sola ``discover_many`` e poi risponde.

    Ritorna il modello e la lista in cui finisce il risultato del tool, per le asserzioni.
    """
    returned = []

    def model(messages, info: AgentInfo) -> ModelResponse:
        returns = [
            part
            for message in messages
            for part in message.parts
            if isinstance(part, ToolReturnPart) and part.tool_name == "discover_many"
        ]
        if not returns:
            requests = [{"question": question} for question in questions]
            return ModelResponse(parts=[ToolCallPart("discover_many", {"requests": requests})])
        returned.append(returns[-1].content)
        args = {"status": "ok", "message": "confronto", "used_document_ids": []}
        return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, args)])

    return FunctionModel(model), returned


def _run(deps, model):
    return asyncio.run(orchestrator.run_orchestrator(question="confronta", deps=deps, model=model))


def test_without_parallel_discovery_there_is_no_discover_many():
    agent = orchestrator.build_orchestrator(TestModel())

    assert "discover_many" not in agent._function_toolset.tools


def test_the_sub_questions_run_together(monkeypatch):
    running, peak = [0], [0]

    async def fake_discovery(*, request, deps, usage=None, model=None):
        running[0] += 1
        peak[0] = max(peak[0], running[0])
        await asyncio.sleep(0.01)
        running[0] -= 1
        return _result(request.question)

    monkeypatch.setattr(discovery, "run_discovery", fake_discovery)
    model, returned = _fan_out(["bando A", "bando B"])

    _run(_deps(parallel_discovery=True), model)

    assert peak[0] == 2
    subtasks = returned[0]["subtasks"]
    assert [s["subtask"] for s in subtasks] == ["bando A", "bando B"]
    assert [s["documents"][0]["document_id"] for s in subtasks] == ["bando A", "bando B"]


def test_sub_questions_over_the_limit_are_dropped_and_reported(monkeypatch):
    async def fake_discovery(*, request, deps, usage=None, model=None):
        return _result(request.question)

    monkeypatch.setattr(discovery, "run_discovery", fake_discovery)
    model, returned = _fan_out(["A", "B", "C"])
    deps = _deps(parallel_discovery=True, max_parallel_subtasks=2)

    _run(deps, model)

    assert [s["subtask"] for s in returned[0]["subtasks"]] == ["A", "B"]
    assert returned[0]["dropped_subtasks"] == ["C"]
    assert returned[0]["note"] == orchestrator.DROPPED_SUBTASKS_NOTE
    assert deps.discoveries_run == 2


def test_the_turn_budget_counts_discover_and_discover_many_together(monkeypatch):
    async def fake_discovery(*, request, deps, usage=None, model=None):
        return _result(request.question)

    monkeypatch.setattr(discovery, "run_discovery", fake_discovery)
    model, returned = _fan_out(["A", "B", "C"])
    deps = _deps(parallel_discovery=True, max_parallel_subtasks=4)
    deps.discoveries_run = orchestrator.MAX_DISCOVERIES_PER_TURN - 1

    _run(deps, model)

    assert [s["subtask"] for s in returned[0]["subtasks"]] == ["A"]
    assert returned[0]["dropped_subtasks"] == ["B", "C"]


def test_a_failed_sub_question_does_not_stop_the_others(monkeypatch):
    async def fake_discovery(*, request, deps, usage=None, model=None):
        if request.question == "B":
            raise RuntimeError("OpenSearch non risponde")
        return _result(request.question)

    monkeypatch.setattr(discovery, "run_discovery", fake_discovery)

    outcomes = asyncio.run(
        discovery.run_parallel_discovery(
            requests=[SubAgentRequest(question="A"), SubAgentRequest(question="B")],
            deps=_deps(),
        )
    )

    assert isinstance(outcomes[0], DiscoveryResult)
    assert isinstance(outcomes[1], DiscoveryUnanswerable)


def test_discover_many_publishes_the_discovering_phase(monkeypatch):
    async def fake_discovery(*, request, deps, usage=None, model=None):
        return _result(request.question)

    monkeypatch.setattr(discovery, "run_discovery", fake_discovery)
    model, _ = _fan_out(["A", "B"])
    deps = _deps(parallel_discovery=True)
    phases = []
    deps.status_sink = phases.append

    _run(deps, model)

    assert phases == ["discovering"]


def test_a_full_fan_out_stays_within_the_request_limit():
    """Con ``max_iterations`` di default il tetto deve reggere un fan-out pieno: ogni Discovery fa
    le sue ricerche e la selezione, sull'``usage`` condiviso con l'orchestratore."""
    questions = ["A", "B", "C", "D"]
    model, returned = _fan_out(questions)
    deps = _deps(parallel_discovery=True, max_parallel_subtasks=4, max_iterations=1)
    deps.retrieve_fn = lambda **_: [{"document_id": "d1", "content": "testo"}]

    def selector(messages, info: AgentInfo) -> ModelResponse:
        searches = sum(
            isinstance(part, ToolReturnPart) for message in messages for part in message.parts
        )
        if searches < discovery.MAX_RETRIEVER_CALLS:
            return ModelResponse(parts=[ToolCallPart("retriever", {"question": "q"})])
        selection = next(t for t in info.output_tools if "Selection" in t.name)
        return ModelResponse(parts=[ToolCallPart(selection.name, {"document_ids": ["d1"]})])

    deps.discovery_model = FunctionModel(selector)

    output, _, _ = _run(deps, model)

    assert output.status == "ok"
    assert len(returned[0]["subtasks"]) == 4
