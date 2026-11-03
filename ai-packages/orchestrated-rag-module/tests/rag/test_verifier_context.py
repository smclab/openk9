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

"""What the Verifier judges on: the conversation of a follow-up, and every chunk
of a document that two delegations of the turn delivered. Without them it
rejected correct answers. And where it runs: on the buffered path only, never
with bypass_rag."""

import asyncio
import json
from contextlib import asynccontextmanager
from unittest.mock import MagicMock

from pydantic_ai.messages import (
    ModelRequest,
    ModelResponse,
    TextPart,
    ToolCallPart,
    UserPromptPart,
)
from pydantic_ai.models.function import AgentInfo, FunctionModel
from pydantic_ai.models.test import TestModel

from app.rag.agentic import discovery, entrypoint, orchestrator, verifier
from app.rag.agentic.contracts import (
    DiscoverySelection,
    OrchestratorOutput,
    SubAgentRequest,
)
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.persistence import FrontendConversationStore


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _verifier_prompt(deps):
    """The prompt the Verifier receives for an answer, with an LLM that approves."""
    prompts = []

    def approve(messages, info: AgentInfo) -> ModelResponse:
        prompts.append(messages[-1].parts[-1].content)
        ok = next(tool for tool in info.output_tools if "VerificationOk" in tool.name)
        return ModelResponse(parts=[ToolCallPart(ok.name, {"status": "ok"})])

    output = OrchestratorOutput(status="ok", message="Ne restano 14.", used_document_ids=[])
    asyncio.run(verifier.verify(output=output, deps=deps, model=FunctionModel(approve)))
    return prompts[0]


def test_the_verifier_reads_the_conversation_of_a_follow_up():
    deps = _deps()
    deps.question = "E quanti me ne restano?"
    deps.message_history = [
        ModelRequest(parts=[UserPromptPart(content="Quanti giorni di ferie ho?")]),
        ModelResponse(
            parts=[
                ToolCallPart(
                    "final_result",
                    {"status": "ok", "message": "Hai 26 giorni.", "used_document_ids": []},
                )
            ]
        ),
    ]

    prompt = _verifier_prompt(deps)

    assert "Conversation so far:" in prompt
    assert "User: Quanti giorni di ferie ho?" in prompt
    assert "Assistant: Hai 26 giorni." in prompt


def test_a_chat_of_agentic_rag_module_reaches_the_verifier_as_text():
    deps = _deps()
    deps.question = "E per la moto?"
    deps.message_history = [
        ModelRequest(parts=[UserPromptPart(content="Rimborso per l'auto?")]),
        ModelResponse(parts=[TextPart(content="0,32 euro al km.")]),
    ]

    assert "Assistant: 0,32 euro al km." in _verifier_prompt(deps)


def test_a_first_turn_has_no_conversation_section():
    deps = _deps()
    deps.question = "Quanti giorni di ferie ho?"

    assert "Conversation so far:" not in _verifier_prompt(deps)


def test_run_orchestrator_hands_the_history_to_the_verifier():
    history = [ModelRequest(parts=[UserPromptPart(content="domanda precedente")])]
    deps = _deps(bypass_rag=True)

    asyncio.run(
        orchestrator.run_orchestrator(
            question="seguito", deps=deps, message_history=history, model=TestModel()
        )
    )

    assert deps.question == "seguito"
    assert deps.message_history == history


def test_a_document_delivered_twice_keeps_both_chunks():
    deps = _deps()
    chunks = iter(["premio n. 041/RA", "14 certificazioni"])
    deps.retrieve_fn = lambda **_: [{"document_id": "doc-1", "content": next(chunks)}]

    def select_doc_1(messages, info: AgentInfo) -> ModelResponse:
        if messages[-1].parts[-1].part_kind == "tool-return":
            selection = next(t for t in info.output_tools if "Selection" in t.name)
            selected = DiscoverySelection(document_ids=["doc-1"]).model_dump()
            return ModelResponse(parts=[ToolCallPart(selection.name, selected)])
        return ModelResponse(parts=[ToolCallPart("retriever", {"question": "premi"})])

    async def two_delegations():
        for question in ("premi", "certificazioni"):
            await discovery.run_discovery(
                request=SubAgentRequest(question=question),
                deps=deps,
                model=FunctionModel(select_doc_1),
            )

    asyncio.run(two_delegations())

    content = deps.discovered["doc-1"]["content"]
    assert "premio n. 041/RA" in content
    assert "14 certificazioni" in content


def test_bypass_rag_disables_the_verifier(monkeypatch):
    async def boom(**_):
        raise AssertionError("without documents the Verifier has no grounding to judge")

    monkeypatch.setattr(orchestrator.verifier, "verify", boom)

    output, _, _ = asyncio.run(
        orchestrator.run_orchestrator(
            question="ciao",
            deps=_deps(bypass_rag=True, enable_verifier=True),
            model=TestModel(),
        )
    )

    assert output.status == "ok"


def test_with_the_verifier_the_answer_is_verified_before_it_streams(monkeypatch):
    order = []
    output = OrchestratorOutput(status="ok", message="risposta verificata", used_document_ids=[])

    async def fake_run_orchestrator(*, question, deps, message_history):
        order.append("run_orchestrator")
        result = MagicMock()
        result.all_messages.return_value = []
        return output, [], result

    @asynccontextmanager
    async def no_stream(**_):
        raise AssertionError("with the Verifier the answer must not stream from the provider")
        yield  # pragma: no cover

    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)
    monkeypatch.setattr(orchestrator, "stream_orchestrator", no_stream)

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="domanda",
                deps=_deps(enable_verifier=True),
                store=FrontendConversationStore(None),
            )
        ]

    events = asyncio.run(collect())

    assert order == ["run_orchestrator"]
    assert [e["type"] for e in events] == ["START", "CHUNK", "END"]
    assert events[1]["chunk"] == "risposta verificata"


def test_with_bypass_rag_the_answer_streams_even_with_the_verifier_on(monkeypatch):
    output = OrchestratorOutput(status="ok", message="risposta libera", used_document_ids=[])

    async def no_buffered_run(**_):
        raise AssertionError("with bypass_rag nobody verifies the answer: it must stream")

    class Stream:
        async def deltas(self):
            yield "risposta libera"

        async def finalize(self):
            result = MagicMock()
            result.all_messages.return_value = []
            return output, [], result

    @asynccontextmanager
    async def stream(**_):
        yield Stream()

    monkeypatch.setattr(orchestrator, "run_orchestrator", no_buffered_run)
    monkeypatch.setattr(orchestrator, "stream_orchestrator", stream)

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="domanda",
                deps=_deps(bypass_rag=True, enable_verifier=True),
                store=FrontendConversationStore(None),
            )
        ]

    events = asyncio.run(collect())

    assert [e["type"] for e in events] == ["START", "CHUNK", "END"]
    assert events[1]["chunk"] == "risposta libera"
