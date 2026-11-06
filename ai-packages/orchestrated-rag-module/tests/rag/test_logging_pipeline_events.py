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
"""What a request leaves in the logs at the default level, and what it spends.

A user who reports "the chatbot did not answer me" must be told apart, at INFO, from a failure
of the model or of the search: every request leaves a start and an end record with its outcome,
its duration and the tokens it spent; every guardrail decision, domain, delegation and search a
record of its own. Above DEBUG neither the question nor the answer is written: the question is
known by its fingerprint.
"""

import asyncio
import json
import logging
from unittest.mock import MagicMock

import pytest
from pydantic_ai.messages import ModelResponse, TextPart, ToolCallPart, ToolReturnPart
from pydantic_ai.exceptions import UsageLimitExceeded
from pydantic_ai.models.function import AgentInfo, DeltaToolCall, FunctionModel
from pydantic_ai.usage import RequestUsage, RunUsage

from app.rag.agentic import discovery, entrypoint, guardrails, intent, orchestrator, retrieval
from app.rag.agentic.contracts import DiscoveryUnanswerable, OrchestratorOutput, SubAgentRequest
from app.rag.agentic.deps import AgenticDeps

QUERY = "Quali sono gli orari degli uffici di Treviso?"
ANSWER = "Gli uffici sono aperti dalle 9 alle 18."


def _deps(model=None, **rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        user_id="u1",
        chat_id="c1",
        orchestrator_model=model,
        discovery_model=model,
    )


def _records(caplog, prefix):
    return [r for r in caplog.records if r.getMessage().startswith(prefix)]


def _no_content_above_debug(caplog):
    for record in caplog.records:
        if record.levelno > logging.DEBUG:
            assert QUERY not in record.getMessage()
            assert ANSWER not in record.getMessage()


# --------------------------------------------------------------------------- #
# The request                                                                  #
# --------------------------------------------------------------------------- #
def _request(monkeypatch, caplog, events, spent=None, level=logging.INFO):
    async def fake_run_turn(*, deps, **_):
        if spent:
            deps.usage.incr(spent)
        for event in events:
            yield json.dumps(event)

    monkeypatch.setattr(entrypoint, "run_turn", fake_run_turn)
    monkeypatch.setattr(entrypoint, "build_model", lambda _: "MODEL")

    async def collect():
        async for _ in entrypoint.get_agentic_rag(
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
            search_text=QUERY,
            chat_id="c1",
            user_id="u1",
            tenant_id="t1",
            retrieve_from_uploaded_documents=False,
            chat_history=None,
            timestamp=None,
            chat_sequence_number=1,
            rag_configuration={},
            llm_configuration={"model": "gpt-4o-mini"},
            guardrails_configuration={},
            opensearch_host="http://os",
            grpc_host_embedding="em:9000",
            grpc_host_datasource="ds:9000",
        ):
            pass

    with caplog.at_level(level, logger="app"):
        asyncio.run(collect())


ANSWERED = [
    {"chunk": "", "type": "START"},
    {"chunk": ANSWER, "type": "CHUNK"},
    {"chunk": "", "type": "END"},
]


def test_the_request_reports_start_and_end_with_outcome_and_tokens(monkeypatch, caplog):
    spent = RunUsage(input_tokens=1200, output_tokens=80, requests=3)
    _request(monkeypatch, caplog, ANSWERED, spent)

    (start,) = _records(caplog, "[request] start")
    (end,) = _records(caplog, "[request] end")
    assert "query_hash=" in start.getMessage()
    assert "outcome=COMPLETED" in end.getMessage()
    assert "duration_ms=" in end.getMessage()
    assert "tokens_in=1200 tokens_out=80 by_model[gpt-4o-mini=1200/80]" in end.getMessage()
    _no_content_above_debug(caplog)


def test_a_request_that_spent_nothing_measurable_says_so(monkeypatch, caplog):
    _request(monkeypatch, caplog, ANSWERED)

    (end,) = _records(caplog, "[request] end")
    assert "tokens=unavailable" in end.getMessage()


def test_the_question_rides_on_the_start_record_only_at_debug(monkeypatch, caplog):
    _request(monkeypatch, caplog, ANSWERED, level=logging.DEBUG)

    (start,) = _records(caplog, "[request] start")
    assert QUERY in start.getMessage()


# --------------------------------------------------------------------------- #
# The tokens of the turn                                                       #
# --------------------------------------------------------------------------- #
def test_the_orchestrator_counts_its_discovery_too():
    def orchestrator_model(messages, info: AgentInfo) -> ModelResponse:
        if isinstance(messages[-1].parts[-1], ToolReturnPart):
            args = {"status": "unanswerable", "message": "x", "used_document_ids": []}
            return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, args)])
        return ModelResponse(parts=[ToolCallPart("discover", {"question": QUERY})])

    def discovery_model(messages, info: AgentInfo) -> ModelResponse:
        unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
        return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "nulla"})])

    deps = _deps(FunctionModel(orchestrator_model))
    deps.discovery_model = FunctionModel(discovery_model)

    asyncio.run(orchestrator.run_orchestrator(question=QUERY, deps=deps))

    # two requests of the orchestrator, one of the Discovery
    assert deps.usage.requests == 3


def test_an_orchestrator_stopped_by_its_limit_still_counts():
    def orchestrator_model(messages, info: AgentInfo) -> ModelResponse:
        return ModelResponse(parts=[ToolCallPart("discover", {"question": QUERY})])

    def discovery_model(messages, info: AgentInfo) -> ModelResponse:
        unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
        return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "nulla"})])

    deps = _deps(FunctionModel(orchestrator_model), max_iterations=1)
    deps.discovery_model = FunctionModel(discovery_model)

    with pytest.raises(UsageLimitExceeded):
        asyncio.run(orchestrator.run_orchestrator(question=QUERY, deps=deps))

    assert deps.usage.requests > 0


def test_a_cancelled_answer_still_counts_the_orchestrator():
    """The output guardrail can stop the stream before ``finalize``: the record of the request
    must carry what the orchestrator spent anyway."""
    output_args = json.dumps({"status": "ok", "message": ANSWER, "used_document_ids": []})

    async def stream_function(messages, info: AgentInfo):
        yield {0: DeltaToolCall(name=info.output_tools[0].name, json_args=output_args)}

    deps = _deps()

    async def cancelled():
        async with orchestrator.stream_orchestrator(
            question=QUERY, deps=deps, model=FunctionModel(stream_function=stream_function)
        ) as stream:
            async for _ in stream.deltas():
                break

    asyncio.run(cancelled())

    assert deps.usage.requests == 1


def test_the_other_agents_of_the_turn_add_to_the_same_bill():
    deps = _deps()
    deps.usage.incr(RunUsage(requests=3))
    title_model = FunctionModel(lambda messages, info: ModelResponse(parts=[TextPart("Orari")]))

    asyncio.run(entrypoint.generate_title(title_model, QUERY, ANSWER, usage=deps.usage))

    assert deps.usage.requests == 4


def test_the_tokens_of_an_openai_response_are_counted():
    """The usage of a real OpenAI response, as the OpenAI model reads it. genai-prices 0.1 returns
    reasoning tokens that this PydanticAI does not take, and every token of the turn was lost."""
    response = {
        "model": "gpt-4o-mini",
        "usage": {
            "prompt_tokens": 13,
            "completion_tokens": 2,
            "total_tokens": 15,
            "completion_tokens_details": {"reasoning_tokens": 0, "audio_tokens": 0},
            "prompt_tokens_details": {"cached_tokens": 0, "audio_tokens": 0},
        },
    }

    usage = RequestUsage.extract(
        response,
        provider="openai",
        provider_url="https://api.openai.com/v1/",
        provider_fallback="openai",
        api_flavor="chat",
    )

    assert (usage.input_tokens, usage.output_tokens) == (13, 2)


# --------------------------------------------------------------------------- #
# The stages                                                                   #
# --------------------------------------------------------------------------- #
@pytest.fixture
def guardrail_hits(monkeypatch):
    state = {"scores": [0.9], "outcome": "VIOLENZA"}
    monkeypatch.setattr(
        retrieval,
        "retrieve_guardrail_documents",
        lambda **_: [{"document_id": "g1", "score": s} for s in state["scores"]],
    )
    monkeypatch.setattr(guardrails, "get_embedding_model_configuration", lambda **_: {})

    async def fake_classify(**_):
        return state["outcome"]

    monkeypatch.setattr(guardrails, "classify_input", fake_classify)
    return state


INPUT = {"input_guardrail": {"enable_input_guardrail": True, "input_guardrail_threshold": 0.7}}


def _input_guardrail(caplog, configuration=INPUT):
    with caplog.at_level(logging.INFO, logger="app"):
        asyncio.run(
            guardrails.input_guardrail(
                query=QUERY, guardrails_configuration=configuration, model=None, deps=_deps()
            )
        )


def test_a_blocked_question_is_a_warning_with_its_category(guardrail_hits, caplog):
    _input_guardrail(caplog)

    (blocked,) = _records(caplog, "[input_guardrail] BLOCKED")
    assert blocked.levelno == logging.WARNING
    assert "category=VIOLENZA" in blocked.getMessage()
    _no_content_above_debug(caplog)


def test_a_passed_question_is_reported_at_info(guardrail_hits, caplog):
    guardrail_hits["outcome"] = guardrails.SAFE

    _input_guardrail(caplog)

    (passed,) = _records(caplog, "[input_guardrail] PASSED")
    assert passed.levelno == logging.INFO


def test_a_disabled_guardrail_is_reported(caplog):
    _input_guardrail(caplog, configuration={})

    assert _records(caplog, "[input_guardrail] disabled")


@pytest.mark.parametrize(
    "verdict, level", [("VALID", logging.INFO), ("OFF_SCOPE", logging.WARNING)]
)
def test_the_scope_gate_verdict_is_reported(caplog, verdict, level):
    model = FunctionModel(lambda messages, info: ModelResponse(parts=[TextPart(verdict)]))
    with caplog.at_level(logging.INFO, logger="app"):
        asyncio.run(
            guardrails.scope_gate(
                query=QUERY,
                context_text="contesto",
                answer_prefix=ANSWER,
                output_guardrail={},
                model=model,
                deps=_deps(),
            )
        )

    (record,) = _records(caplog, f"[scope_gate] {verdict}")
    assert record.levelno == level
    _no_content_above_debug(caplog)


def test_the_domain_and_where_it_came_from_are_reported(monkeypatch, caplog):
    monkeypatch.setattr(retrieval, "get_domains", lambda **_: ["orari", "tributi"])
    monkeypatch.setattr(
        retrieval, "retrieve_domain_documents", lambda **_: [{"domain": "orari", "score": 0.9}]
    )
    monkeypatch.setattr(intent, "get_embedding_model_configuration", lambda **_: {})

    with caplog.at_level(logging.INFO, logger="app"):
        asyncio.run(intent.detect_domains(query=QUERY, deps=_deps(domain_threshold=0.8)))

    (record,) = _records(caplog, "[input_domain]")
    assert "domain=['orari'] source=threshold" in record.getMessage()


def test_each_search_reports_how_many_documents_it_retrieved(caplog):
    deps = _deps()
    deps.retrieve_fn = lambda **_: [
        {"document_id": "d1", "content": "uno"},
        {"document_id": "d2", "content": "due"},
    ]

    def search_once(messages, info: AgentInfo) -> ModelResponse:
        if isinstance(messages[-1].parts[-1], ToolReturnPart):
            unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
            return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "basta"})])
        return ModelResponse(parts=[ToolCallPart("retriever", {"question": QUERY})])

    with caplog.at_level(logging.INFO, logger="app"):
        asyncio.run(
            discovery.run_discovery(
                request=SubAgentRequest(question=QUERY),
                deps=deps,
                model=FunctionModel(search_once),
            )
        )

    (record,) = _records(caplog, "[retriever]")
    assert "retrieved 2 docs" in record.getMessage()
    assert "tenant_id=t1" in record.getMessage()
    _no_content_above_debug(caplog)


def test_each_delegation_is_reported_without_its_text(monkeypatch, caplog):
    async def fake_run_discovery(**_):
        return DiscoveryUnanswerable(reason="stub")

    monkeypatch.setattr(orchestrator.discovery, "run_discovery", fake_run_discovery)

    def delegate_once(messages, info: AgentInfo) -> ModelResponse:
        if isinstance(messages[-1].parts[-1], ToolReturnPart):
            args = {"message": "x", "used_document_ids": [], "status": "unanswerable"}
            return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, args)])
        return ModelResponse(
            parts=[ToolCallPart("discover", {"question": QUERY, "hints": "orari estivi"})]
        )

    with caplog.at_level(logging.INFO, logger="app"):
        asyncio.run(
            orchestrator.run_orchestrator(
                question=QUERY, deps=_deps(), model=FunctionModel(delegate_once)
            )
        )

    (record,) = _records(caplog, "[orchestrator] discover")
    assert "discoveries=1/6" in record.getMessage()
    assert "orari estivi" not in record.getMessage()
    _no_content_above_debug(caplog)
