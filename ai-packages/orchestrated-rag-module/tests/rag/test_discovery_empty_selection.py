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

"""Una selezione senza documenti validi e' un esito "unanswerable", non un "ok" vuoto.

Il post-processing scarta gli id che non stanno tra i candidati. Se il modello ne sceglie solo di
inventati, o nessuno, all'orchestratore arrivava ``status="ok"`` con zero documenti: un successo
da cui comporre la risposta, senza fonti, invece del segnale che quella sotto-domanda non ha
risposta nei documenti.
"""

import asyncio

import pytest


from pydantic_ai.messages import ModelResponse, ToolCallPart, ToolReturnPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.rag.agentic import discovery
from app.rag.agentic.contracts import (
    DiscoveryResult,
    DiscoveryUnanswerable,
    SubAgentRequest,
)
from app.rag.agentic.deps import AgenticDeps


def _run(selected_ids):
    deps = AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )
    deps.retrieve_fn = lambda **_: [{"document_id": "d1", "content": "testo"}]

    def select(messages, info: AgentInfo) -> ModelResponse:
        if isinstance(messages[-1].parts[-1], ToolReturnPart):
            selection = next(t for t in info.output_tools if "Selection" in t.name)
            return ModelResponse(
                parts=[ToolCallPart(selection.name, {"document_ids": selected_ids})]
            )
        return ModelResponse(parts=[ToolCallPart("retriever", {"question": "q"})])

    outcome = asyncio.run(
        discovery.run_discovery(
            request=SubAgentRequest(question="q"), deps=deps, model=FunctionModel(select)
        )
    )
    return outcome, deps


@pytest.mark.parametrize("selected_ids", [[], ["inventato"]], ids=["none", "invented_only"])
def test_selection_without_valid_documents_is_unanswerable(selected_ids):
    outcome, deps = _run(selected_ids)

    assert isinstance(outcome, DiscoveryUnanswerable)
    assert deps.delivered_ids == []


def test_selection_with_a_valid_document_is_kept():
    outcome, deps = _run(["inventato", "d1"])

    assert isinstance(outcome, DiscoveryResult)
    assert [doc.document_id for doc in outcome.documents] == ["d1"]
    assert deps.delivered_ids == ["d1"]


# --------------------------------------------------------------------------- #
# Budget di contesto del turno: somma dei documenti consegnati all'orchestratore #
# --------------------------------------------------------------------------- #
def _deliver(deps, documents, selected_ids):
    deps.retrieve_fn = lambda **_: documents

    def select(messages, info: AgentInfo) -> ModelResponse:
        if isinstance(messages[-1].parts[-1], ToolReturnPart):
            selection = next(t for t in info.output_tools if "Selection" in t.name)
            return ModelResponse(
                parts=[ToolCallPart(selection.name, {"document_ids": selected_ids})]
            )
        return ModelResponse(parts=[ToolCallPart("retriever", {"question": "q"})])

    return asyncio.run(
        discovery.run_discovery(
            request=SubAgentRequest(question="q"), deps=deps, model=FunctionModel(select)
        )
    )


def _budget_deps(context_window):
    return AgenticDeps(
        rag_configuration={},
        llm_configuration={"context_window": context_window},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def test_documents_past_the_turn_budget_are_not_delivered():
    deps = _budget_deps(context_window=100)  # budget: 85 token = 297.5 caratteri
    documents = [{"document_id": f"d{i}", "content": "x" * 140} for i in range(3)]

    outcome = _deliver(deps, documents, ["d0", "d1", "d2"])

    assert [doc.document_id for doc in outcome.documents] == ["d0", "d1"]
    assert deps.delivered_ids == ["d0", "d1"]


def test_the_budget_spans_every_delegation_of_the_turn():
    deps = _budget_deps(context_window=100)
    _deliver(deps, [{"document_id": "d0", "content": "x" * 280}], ["d0"])

    outcome = _deliver(deps, [{"document_id": "d1", "content": "y" * 140}], ["d1"])

    assert isinstance(outcome, DiscoveryUnanswerable)
    assert deps.delivered_ids == ["d0"]


def test_without_a_context_window_nothing_is_cut():
    deps = _budget_deps(context_window=0)
    documents = [{"document_id": f"d{i}", "content": "x" * 10_000} for i in range(3)]

    outcome = _deliver(deps, documents, ["d0", "d1", "d2"])

    assert len(outcome.documents) == 3
