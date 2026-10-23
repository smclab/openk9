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

"""Verifier e ciclo di riparazione dell'orchestratore.

Nella sweep S5 il Verifier bocciava risposte corrette perche' vedeva solo i primi 1.500 caratteri
dei documenti citati, mentre l'orchestratore li aveva letti interi; il motivo del rifiuto tornava
come testo libero, e l'orchestratore riscriveva la stessa risposta senza cercare di nuovo; a
riprove esaurite l'utente riceveva un errore. Qui:

- il Verifier giudica sugli stessi documenti consegnati all'orchestratore, interi;
- il rifiuto elenca le affermazioni non supportate con l'azione di riparazione (cercare/togliere);
- il messaggio di riprova dice all'orchestratore come riparare;
- a riprove esaurite si restituisce l'ultima risposta.
"""

import asyncio

from pydantic_ai.messages import ModelResponse, RetryPromptPart, ToolCallPart
from pydantic_ai.models.function import AgentInfo, FunctionModel
from pydantic_ai.models.test import TestModel

from app.rag.agentic import discovery, orchestrator, verifier
from app.rag.agentic.contracts import (
    SubAgentRequest,
    UnsupportedClaim,
    VerificationFailed,
)
from app.rag.agentic.deps import AgenticDeps

LONG_TEXT = "Sedi a Roma e Milano. " + "x" * 3000 + " La rete copre 23 citta'."


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _final(info: AgentInfo, message: str) -> ModelResponse:
    args = {"status": "ok", "message": message, "used_document_ids": ["d1"]}
    return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, args)])


# --------------------------------------------------------------------------- #
# Cosa vede il Verifier                                                        #
# --------------------------------------------------------------------------- #
def test_discovery_records_the_documents_delivered_to_the_orchestrator():
    deps = _deps()
    deps.retrieve_fn = lambda **_: [
        {"document_id": "d1", "content": LONG_TEXT},
        {"document_id": "d2", "content": "candidato scartato"},
    ]

    def selector(messages, info: AgentInfo) -> ModelResponse:
        if len(messages) == 1:
            return ModelResponse(parts=[ToolCallPart("retriever", {"question": "sedi"})])
        selection = next(t for t in info.output_tools if "Selection" in t.name)
        return ModelResponse(parts=[ToolCallPart(selection.name, {"document_ids": ["d1"]})])

    asyncio.run(
        discovery.run_discovery(
            request=SubAgentRequest(question="sedi"), deps=deps, model=FunctionModel(selector)
        )
    )

    assert deps.delivered_ids == ["d1"]


def test_verifier_sees_the_delivered_documents_in_full():
    deps = _deps()
    deps.discovered = {
        "d1": {"content": LONG_TEXT, "title": "Sedi"},
        "d2": {"content": "candidato scartato", "title": "Altro"},
    }
    deps.delivered_ids = ["d1"]

    rendered = verifier._render_documents(deps)

    assert LONG_TEXT in rendered
    assert "candidato scartato" not in rendered


def test_verifier_runs_at_temperature_zero():
    assert verifier.build_verifier_agent(TestModel()).model_settings["temperature"] == 0


# --------------------------------------------------------------------------- #
# Dal rifiuto alla riparazione                                                 #
# --------------------------------------------------------------------------- #
def test_repair_instructions_say_what_to_do_with_each_claim():
    verdict = VerificationFailed(
        reason="numeri non supportati",
        unsupported_claims=[
            UnsupportedClaim(claim="oltre 30 uffici", action="search"),
            UnsupportedClaim(claim="fondata nel 1990", action="remove"),
        ],
    )

    text = orchestrator.repair_instructions(verdict)

    assert "numeri non supportati" in text
    search_line = next(line for line in text.splitlines() if "oltre 30 uffici" in line)
    remove_line = next(line for line in text.splitlines() if "fondata nel 1990" in line)
    assert "`discover`" in search_line
    assert "remove" in remove_line.lower()


def _run_with_failing_verifier(monkeypatch, attempts):
    async def always_fails(**_):
        return VerificationFailed(
            reason="non supportata",
            unsupported_claims=[UnsupportedClaim(claim="oltre 30 uffici", action="search")],
        )

    monkeypatch.setattr(orchestrator.verifier, "verify", always_fails)
    retry_prompts = []

    def answer(messages, info: AgentInfo) -> ModelResponse:
        for message in messages:
            for part in message.parts:
                if isinstance(part, RetryPromptPart):
                    retry_prompts.append(part.model_response())
        return _final(info, f"risposta {len(retry_prompts) + 1}")

    deps = _deps(enable_verifier=True, max_iterations=attempts - 1)
    output, _, _ = asyncio.run(
        orchestrator.run_orchestrator(question="q", deps=deps, model=FunctionModel(answer))
    )
    return output, retry_prompts


def test_retry_prompt_carries_the_repair_instructions(monkeypatch):
    _, retry_prompts = _run_with_failing_verifier(monkeypatch, attempts=2)

    assert retry_prompts
    assert "oltre 30 uffici" in retry_prompts[0]
    assert "`discover`" in retry_prompts[0]


def test_exhausted_retries_return_the_last_answer(monkeypatch):
    output, _ = _run_with_failing_verifier(monkeypatch, attempts=2)

    assert output.message == "risposta 2"


# --------------------------------------------------------------------------- #
# Budget di richieste                                                          #
# --------------------------------------------------------------------------- #
def test_usage_budget_covers_a_repair_with_a_new_discover():
    """Ogni tentativo puo' delegare di nuovo: 2 richieste dell'orchestratore (delega +
    composizione), fino a MAX_RETRIEVER_CALLS + 1 del Discovery (ricerche + selezione), 1 del
    Verifier."""
    limits = orchestrator.build_usage_limits({"max_iterations": 1, "enable_verifier": True})

    per_discovery = discovery.MAX_RETRIEVER_CALLS + 1
    assert limits.request_limit == 2 * (2 + per_discovery + 1) + 2
