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
"""Evaluation of the turns: in real time, on the complete answer, and offline, on the spans the
turns leave on Phoenix.

In real time three judges look at the turn, as the evaluation graph of agentic-rag-module did:
the routing, every delivered document, the clarity of the answer. Their verdicts go into the
saved turn, never into the stream, and a failed judge never costs the answer. The turn runs
without input guardrail and intent detection, as that graph did, and announces the phase
``evaluating`` before the answer.

Offline, every turn leaves a root span with the JSON the evaluate route reads back.
"""

import asyncio
import json
from types import SimpleNamespace
from unittest.mock import MagicMock

import pytest
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import SimpleSpanProcessor
from opentelemetry.sdk.trace.export.in_memory_span_exporter import InMemorySpanExporter
from pydantic_ai.messages import ModelResponse, TextPart, ToolCallPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.rag import evaluations
from app.rag.agentic import entrypoint, evaluation, guardrails, intent, orchestrator
from app.rag.agentic.contracts import OrchestratorOutput
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.persistence import build_checkpoint


def _judges(fail=()):
    """A model that answers each judge: the router with text, the others with their output."""
    prompts = []

    def model(messages, info: AgentInfo) -> ModelResponse:
        prompt = messages[-1].parts[-1].content
        prompts.append(prompt)
        if any(word in prompt for word in fail):
            raise RuntimeError("judge down")
        if not info.output_tools:
            return ModelResponse(parts=[TextPart("correct")])
        tool = info.output_tools[0]
        if "chunk_id" in json.dumps(tool.parameters_json_schema):
            chunk_id = prompt.split("with chunk_id ")[1].split()[0]
            args = {"chunk_id": chunk_id, "judgment": "RELEVANT", "explanation": "x", "vote": 8}
        else:
            args = {"judgment": "CLEAR", "explanation": "x", "vote": 9}
        return ModelResponse(parts=[ToolCallPart(tool.name, args)])

    return FunctionModel(model), prompts


def _deps(model=None, **rag_configuration):
    deps = AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        orchestrator_model=model,
    )
    deps.discovered = {"d1": {"content": "Il RUP e' Mario Rossi."}, "d2": {"content": "Altro."}}
    deps.delivered_ids = ["d1"]
    return deps


OUTPUT = OrchestratorOutput(status="ok", message="Il RUP e' Mario Rossi [1].")


# --------------------------------------------------------------------------- #
# The real-time judges                                                         #
# --------------------------------------------------------------------------- #
def test_the_three_judges_fill_the_fields_of_the_saved_turn():
    model, prompts = _judges()
    deps = _deps(model, rag_tool_description="Cerca nei documenti")

    fields = asyncio.run(
        evaluation.run_evaluations(question="Chi e' il RUP?", output=OUTPUT, deps=deps, used_rag=True)
    )

    assert fields == {
        "rag_router_evaluation": "correct",
        "retriever_chunks_evaluation": [
            {"chunk_id": "d1", "judgment": "RELEVANT", "explanation": "x", "vote": 8}
        ],
        "response_evaluation": "CLEAR",
    }
    router = next(p for p in prompts if "tool" in p.lower() and "Cerca nei documenti" in p)
    assert "rag_tool" in router


def test_only_the_delivered_documents_are_judged():
    model, prompts = _judges()

    asyncio.run(
        evaluation.run_evaluations(question="q", output=OUTPUT, deps=_deps(model), used_rag=True)
    )

    assert not any("Altro." in p for p in prompts)


def test_without_a_search_no_document_is_judged():
    model, _ = _judges()

    fields = asyncio.run(
        evaluation.run_evaluations(
            question="ciao", output=OUTPUT, deps=_deps(model), used_rag=False
        )
    )

    assert fields["retriever_chunks_evaluation"] is None
    assert fields["rag_router_evaluation"] == "correct"


def test_a_failed_judge_leaves_only_its_field_empty():
    model, _ = _judges(fail=("clarity",))

    fields = asyncio.run(
        evaluation.run_evaluations(question="q", output=OUTPUT, deps=_deps(model), used_rag=True)
    )

    assert fields["response_evaluation"] is None
    assert fields["rag_router_evaluation"] == "correct"
    assert fields["retriever_chunks_evaluation"]


def test_the_fields_are_saved_with_the_turn():
    values = build_checkpoint(
        original_query="q",
        current_query="q",
        response="r",
        conversation_title=None,
        chat_sequence_number=1,
        retrieve_from_uploaded_documents=False,
        used_document_refs=[],
        message_history=[],
        evaluation_fields={"response_evaluation": "CLEAR", "rag_router_evaluation": "correct"},
    )["channel_values"]

    assert values["response_evaluation"] == "CLEAR"
    assert values["rag_router_evaluation"] == "correct"


# --------------------------------------------------------------------------- #
# The turn with real-time evaluation                                           #
# --------------------------------------------------------------------------- #
def _turn(monkeypatch, *, real_time, judges_fail=False, with_verifier=False):
    calls = []

    async def fake_guardrail(**_):
        calls.append("guardrail")
        return guardrails.SAFE

    async def fake_detect(**_):
        calls.append("intent")
        return [], []

    async def fake_run_orchestrator(*, deps, **_):
        calls.append("orchestrator")
        if with_verifier:
            deps.emit_status("evaluating")  # what the Verifier publishes
        return OUTPUT, [], MagicMock(all_messages=lambda: [])

    async def fake_evaluations(**_):
        calls.append("evaluation")
        if judges_fail:
            raise AssertionError("run_evaluations is best effort and never raises")
        return {"response_evaluation": "CLEAR"}

    monkeypatch.setattr(guardrails, "input_guardrail", fake_guardrail)
    monkeypatch.setattr(intent, "detect_domains", fake_detect)
    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)
    monkeypatch.setattr(evaluation, "run_evaluations", fake_evaluations)
    store = MagicMock()

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="Chi e' il RUP?",
                # the evaluation alone buffers the answer; without it, the Verifier does
                deps=_deps(
                    enable_real_time_evaluation=real_time,
                    enable_verifier=with_verifier or not real_time,
                ),
                store=store,
                chat_id="c1",
                user_id="u1",
                guardrails_configuration={"input_guardrail": {"enable_input_guardrail": True}},
            )
        ]

    return asyncio.run(collect()), calls, store


def test_with_real_time_evaluation_the_turn_runs_like_the_evaluation_graph(monkeypatch):
    events, calls, store = _turn(monkeypatch, real_time=True)

    assert calls == ["orchestrator", "evaluation"]
    phases = [e["chunk"] for e in events if e["type"] == "STATUS"]
    assert phases == ["planning", "evaluating", "generating"]
    types = [e["type"] for e in events]
    assert types.index("STATUS", types.index("STATUS") + 1) < types.index("START")
    assert store.save_turn.call_args.kwargs["evaluation_fields"] == {"response_evaluation": "CLEAR"}


def test_with_the_verifier_too_evaluating_comes_once(monkeypatch):
    events, calls, _ = _turn(monkeypatch, real_time=True, with_verifier=True)

    assert calls == ["orchestrator", "evaluation"]
    phases = [e["chunk"] for e in events if e["type"] == "STATUS"]
    assert phases == ["planning", "evaluating", "generating"]


def test_without_real_time_evaluation_nothing_is_judged(monkeypatch):
    events, calls, store = _turn(monkeypatch, real_time=False)

    assert calls == ["guardrail", "intent", "orchestrator"]
    assert "evaluating" not in [e["chunk"] for e in events if e["type"] == "STATUS"]
    assert store.save_turn.call_args.kwargs["evaluation_fields"] is None


# --------------------------------------------------------------------------- #
# The span of the turn, for the offline evaluation                             #
# --------------------------------------------------------------------------- #
@pytest.fixture
def spans(monkeypatch):
    exporter = InMemorySpanExporter()
    provider = TracerProvider()
    provider.add_span_processor(SimpleSpanProcessor(exporter))
    monkeypatch.setattr(evaluations.trace, "get_tracer", provider.get_tracer)
    return exporter


def test_a_turn_leaves_a_root_span_with_what_the_evaluation_reads(spans):
    deps = _deps()
    deps.discoveries_run = 1

    evaluations.record_turn(
        started_ns=1, question="Chi e' il RUP?", response="Mario Rossi.", deps=deps, used_rag=True
    )

    (span,) = spans.get_finished_spans()
    assert span.name == evaluations.TURN_SPAN_NAME
    assert span.parent is None
    assert json.loads(span.attributes["output.value"]) == {
        "current_query": "Chi e' il RUP?",
        "response": "Mario Rossi.",
        "context": [{"metadata": {"document_id": "d1"}, "page_content": "Il RUP e' Mario Rossi."}],
        "use_rag": True,
    }


# --------------------------------------------------------------------------- #
# The offline evaluation                                                       #
# --------------------------------------------------------------------------- #
def _span(span_id, name=evaluations.TURN_SPAN_NAME, parent_id=None, output=None):
    return {
        "name": name,
        "parent_id": parent_id,
        "context": {"span_id": span_id},
        "attributes": {"output.value": json.dumps(output) if output is not None else None},
    }


TURN = {
    "current_query": "Chi e' il RUP?",
    "response": "Mario Rossi.",
    "context": [{"metadata": {"document_id": "d1"}, "page_content": "Il RUP e' Mario Rossi."}],
    "use_rag": True,
}


def test_the_offline_evaluation_annotates_only_the_turn_spans(monkeypatch):
    model, _ = _judges()
    client = MagicMock()
    client.spans.get_spans.return_value = [
        _span("turn", output=TURN),
        _span("child", parent_id="turn", output=TURN),
        _span("agent", name="orchestrator run", output=None),
    ]
    monkeypatch.setattr(evaluations, "Client", lambda base_url: client)
    monkeypatch.setattr(evaluations, "build_model", lambda configuration: model)

    async def judge_chunks(model, output_type, prompt):
        if output_type is evaluations.RetrieverEvaluationResponseList:
            return evaluations.RetrieverEvaluationResponseList(
                evaluations=[
                    evaluations.RetrieverEvaluationResponse(
                        chunk_id="d1", judgment="RELEVANT", explanation="x", vote=8
                    )
                ]
            )
        return await original_judge(model, output_type, prompt)

    original_judge = evaluations._judge
    monkeypatch.setattr(evaluations, "_judge", judge_chunks)

    evaluated = asyncio.run(
        evaluations.evaluations(
            {}, {}, "project", "http://phoenix", 10, None, None, True, True, True
        )
    )

    assert evaluated == {"turn"}
    annotations = client.annotations.add_span_annotation.call_args_list
    assert sorted(call.kwargs["annotation_name"] for call in annotations) == [
        "evaluate_rag_router",
        "evaluate_response",
        "evaluation for chunk d1",
    ]
    assert {call.kwargs["span_id"] for call in annotations} == {"turn"}
