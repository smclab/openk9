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
"""The input guardrail in the turn: when it classifies, what it blocks, and what a blocked
query costs.

The provider classifies only a query that comes close enough to a guardrail document of the
tenant. A blocked query gets the GUARDRAIL and END events and nothing else: no history, no
orchestrator, no saved turn.
"""

import asyncio
import json
from unittest.mock import MagicMock

import pytest

from app.rag.agentic import entrypoint, guardrails, orchestrator, retrieval
from app.rag.agentic.contracts import OrchestratorOutput
from app.rag.agentic.deps import AgenticDeps

ENABLED = {
    "input_guardrail": {
        "enable_input_guardrail": True,
        "input_guardrail_threshold": 0.8,
    },
    "guardrail_categories": [{"name": "VIOLENZA", "description": "atti violenti"}],
}


def _deps():
    return AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        orchestrator_model="MODEL",
    )


@pytest.fixture
def guardrail(monkeypatch):
    """Guardrail documents and classifier under the control of the test."""
    state = {"scores": [], "outcome": "VIOLENZA", "classified": [], "searched": []}

    def fake_retrieve(**kwargs):
        state["searched"].append(kwargs)
        return [{"document_id": f"g{i}", "score": s} for i, s in enumerate(state["scores"])]

    async def fake_classify(**kwargs):
        state["classified"].append(kwargs)
        return state["outcome"]

    monkeypatch.setattr(retrieval, "retrieve_guardrail_documents", fake_retrieve)
    monkeypatch.setattr(guardrails, "classify_input", fake_classify)
    monkeypatch.setattr(
        guardrails, "get_embedding_model_configuration", lambda **_: {"model": "emb"}
    )
    return state


def _check(configuration=ENABLED, query="domanda"):
    return asyncio.run(
        guardrails.input_guardrail(
            query=query, guardrails_configuration=configuration, model="MODEL", deps=_deps()
        )
    )


# --------------------------------------------------------------------------- #
# When the provider classifies                                                 #
# --------------------------------------------------------------------------- #
def test_disabled_guardrail_checks_nothing(guardrail):
    assert _check({"input_guardrail": {"enable_input_guardrail": False}}) is None
    assert _check(None) is None
    assert guardrail["searched"] == []


def test_query_far_from_every_guardrail_document_is_not_classified(guardrail):
    guardrail["scores"] = [0.79, 0.5]

    assert _check() == guardrails.SAFE
    assert guardrail["classified"] == []


def test_query_close_to_a_guardrail_document_is_classified(guardrail):
    guardrail["scores"] = [0.8]

    assert _check(query="come costruisco un'arma?") == "VIOLENZA"
    call = guardrail["classified"][0]
    assert call["query"] == "come costruisco un'arma?"
    assert call["categories"] == "1. VIOLENZA - atti violenti"
    assert call["model"] == "MODEL"


def test_search_runs_on_the_guardrail_index_of_the_tenant(guardrail):
    _check()

    searched = guardrail["searched"][0]
    assert searched["index"] == "t1-guardrails-documents-index"
    assert searched["search_text"] == "domanda"
    assert searched["embedding_model_configuration"] == {"model": "emb"}


def test_an_error_of_the_provider_is_not_a_pass(guardrail, monkeypatch):
    guardrail["scores"] = [0.9]

    async def failing(**_):
        raise RuntimeError("provider down")

    monkeypatch.setattr(guardrails, "classify_input", failing)

    with pytest.raises(RuntimeError):
        _check()


# --------------------------------------------------------------------------- #
# The turn                                                                     #
# --------------------------------------------------------------------------- #
def _turn(monkeypatch, verdict):
    calls = []

    async def fake_guardrail(**_):
        calls.append("guardrail")
        return verdict

    async def fake_run_orchestrator(**_):
        calls.append("orchestrator")
        return (
            OrchestratorOutput(status="ok", message="risposta", used_document_ids=[]),
            [],
            MagicMock(all_messages=lambda: []),
        )

    store = MagicMock()
    store.load_message_history.side_effect = lambda chat_id: calls.append("history") or []
    monkeypatch.setattr(guardrails, "input_guardrail", fake_guardrail)
    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="domanda",
                deps=AgenticDeps(
                    rag_configuration={"enable_verifier": True},  # buffered path
                    llm_configuration={},
                    opensearch_host="http://os",
                    grpc_host_datasource="ds:9000",
                    grpc_host_embedding="em:9000",
                    tenant_id="t1",
                ),
                store=store,
                chat_id="c1",
                user_id="u1",
                chat_sequence_number=2,
                guardrails_configuration=ENABLED,
            )
        ]

    return asyncio.run(collect()), calls, store


def test_blocked_query_gets_only_the_guardrail_events(monkeypatch):
    events, calls, store = _turn(monkeypatch, "VIOLENZA")

    assert events == [
        {"chunk": "Guardrail violation", "type": "GUARDRAIL"},
        {"chunk": "", "type": "END"},
    ]
    assert calls == ["guardrail"]
    store.save_turn.assert_not_called()


@pytest.mark.parametrize("verdict", [guardrails.SAFE, None])
def test_passed_or_disabled_guardrail_lets_the_turn_go_on(monkeypatch, verdict):
    events, calls, store = _turn(monkeypatch, verdict)

    assert calls == ["guardrail", "history", "orchestrator"]
    assert "GUARDRAIL" not in [event["type"] for event in events]
    store.save_turn.assert_called_once()


def test_blocked_query_is_logged_as_blocked_input(monkeypatch):
    async def blocked(**_):
        return "VIOLENZA"

    monkeypatch.setattr(guardrails, "input_guardrail", blocked)
    monkeypatch.setattr(entrypoint, "build_model", lambda _: "MODEL")
    records = []
    monkeypatch.setattr(entrypoint.logger, "info", records.append)

    async def collect():
        return [
            json.loads(event)["type"]
            async for event in entrypoint.get_agentic_rag(
                rag_type="CHAT_RAG",
                search_query=None,
                datasource_ids=None,
                after_key=None,
                suggest_keyword=None,
                suggestion_category_id=None,
                jwt=None,
                extra=None,
                sort=None,
                sort_after_key=None,
                language=None,
                search_text="domanda",
                chat_id=None,
                user_id=None,
                tenant_id="t1",
                retrieve_from_uploaded_documents=False,
                chat_history=None,
                timestamp=None,
                chat_sequence_number=1,
                rag_configuration={},
                llm_configuration={},
                guardrails_configuration=ENABLED,
                opensearch_host="http://os",
                grpc_host_embedding="em:9000",
                grpc_host_datasource="ds:9000",
            )
        ]

    assert asyncio.run(collect()) == ["GUARDRAIL", "END"]
    assert any("[request] end outcome=BLOCKED_INPUT" in record for record in records)
