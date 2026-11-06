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
"""Questions with an image (#2500), as in agentic-rag-module.

The image goes on the query token of every search, with the text when there is one. A question
made of an image alone skips the stages that read the written question (input guardrail, intent
detection, orchestrator): the image goes straight to a single search, and the model sums up the
text of what it found.
"""

import asyncio
import json
from unittest.mock import MagicMock

import pytest
from pydantic_ai.messages import (
    ModelRequest,
    ModelResponse,
    SystemPromptPart,
    TextPart,
    ToolCallPart,
    UserPromptPart,
)
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.models import models
from app.rag.agentic import discovery, entrypoint, guardrails, intent, orchestrator, retrieval
from app.rag.agentic.contracts import OrchestratorOutput, SubAgentRequest
from app.rag.agentic.deps import AgenticDeps

IMAGE = models.Media(data="aVZCT1J3MEtHZ28=", contentType="image/png")


# --------------------------------------------------------------------------- #
# The query token                                                              #
# --------------------------------------------------------------------------- #
def _tokens(monkeypatch, query, media=None, search_query=None):
    sent = {}

    def fake_parser(**kwargs):
        sent.update(kwargs)
        return {"query": b"{}", "index_name": [], "query_parameters": {}}

    monkeypatch.setattr(retrieval, "query_parser", fake_parser)
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda host: MagicMock())
    try:
        retrieval.retrieve_documents(
            query=query,
            search_text=query,
            retrieve_type="KNN",
            tenant_id="t1",
            opensearch_host="http://os",
            grpc_host="ds:9000",
            context_window=100_000,
            range_values=[0, 5],
            search_query=search_query,
            media=media,
        )
    except Exception:  # noqa: BLE001 - only the tokens sent matter here
        pass
    return sent["search_query"]


def test_a_text_question_sends_no_media(monkeypatch):
    token = _tokens(monkeypatch, "Che copertura ho per la grandine?")[0]

    assert "media" not in token
    assert token["values"] == ["Che copertura ho per la grandine?"]


def test_an_image_with_text_sends_both(monkeypatch):
    token = _tokens(monkeypatch, "A cosa somiglia questa foto?", media=IMAGE)[0]

    assert token["tokenType"] == "KNN"
    assert token["values"] == ["A cosa somiglia questa foto?"]
    assert token["media"] == IMAGE.model_dump()


def test_an_image_alone_is_searched_with_no_text(monkeypatch):
    token = _tokens(monkeypatch, "", media=IMAGE)[0]

    assert token["values"] == []
    assert token["media"] == IMAGE.model_dump()


def test_the_media_of_a_request_token_is_kept(monkeypatch):
    request_token = models.SearchToken(
        tokenType="KNN",
        keywordKey="",
        values=["foto"],
        filter=True,
        entityType="",
        entityName="",
        extra={},
        media=IMAGE,
    )

    token = _tokens(monkeypatch, "foto", search_query=[request_token])[0]

    assert token["media"] == IMAGE.model_dump()


def _deps(media=None, **rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        media=media,
    )


def test_every_search_of_the_discovery_carries_the_image():
    searches = []
    deps = _deps(media=IMAGE)
    deps.retrieve_fn = lambda **kwargs: searches.append(kwargs) or []

    def selector(messages, info: AgentInfo) -> ModelResponse:
        from pydantic_ai.messages import ToolCallPart

        if len(messages) == 1:
            return ModelResponse(parts=[ToolCallPart("retriever", {"question": "foto"})])
        unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
        return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "nulla"})])

    asyncio.run(
        discovery.run_discovery(
            request=SubAgentRequest(question="foto"), deps=deps, model=FunctionModel(selector)
        )
    )

    assert searches[0]["media"] == IMAGE


# --------------------------------------------------------------------------- #
# A question made of an image alone                                            #
# --------------------------------------------------------------------------- #
def _instructions(messages) -> str:
    """What the model is told to do: any system prompt of the history and the instructions of
    the request, the only ones a model sends."""
    system = [p.content for m in messages for p in m.parts if isinstance(p, SystemPromptPart)]
    return " ".join(system + [messages[-1].instructions or ""])


#: a turn of a chat: the orchestrator answered a written question
TEXT_TURN = [
    ModelRequest(parts=[SystemPromptPart("ORCHESTRATOR PROMPT"), UserPromptPart("domanda")]),
    ModelResponse(parts=[TextPart("risposta")]),
]
#: a turn with an image alone as saved: its instructions stay on its request
IMAGE_TURN = [
    ModelRequest(
        parts=[UserPromptPart("Context:\nCantiere di via Roma")],
        instructions=entrypoint.MEDIA_ONLY_QUERY_INSTRUCTION,
    ),
    ModelResponse(parts=[TextPart("Le immagini mostrano un cantiere.")]),
]
#: the history a client sends for an anonymous user: no system prompt
CLIENT_HISTORY = [
    ModelRequest(parts=[UserPromptPart("domanda")]),
    ModelResponse(parts=[TextPart("risposta")]),
]


def _turn(monkeypatch, *, search_text, media, history=None):
    calls = []

    async def fake_guardrail(**_):
        calls.append("guardrail")
        return guardrails.SAFE

    async def fake_detect(**_):
        calls.append("intent")
        return [], []

    async def fake_run_orchestrator(**_):
        calls.append("orchestrator")
        return (
            OrchestratorOutput(status="ok", message="risposta", used_document_ids=[]),
            [],
            MagicMock(all_messages=lambda: []),
        )

    monkeypatch.setattr(guardrails, "input_guardrail", fake_guardrail)
    monkeypatch.setattr(intent, "detect_domains", fake_detect)
    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)
    prompts, searches = [], []

    def model(messages, info: AgentInfo) -> ModelResponse:
        prompts.append((_instructions(messages), messages[-1].parts[-1].content))
        return ModelResponse(parts=[TextPart("Le immagini mostrano due cantieri.")])

    deps = _deps(media=media, enable_verifier=True)
    deps.orchestrator_model = FunctionModel(model)
    deps.retrieve_fn = lambda **kwargs: searches.append(kwargs) or [
        {"document_id": "d1", "content": "Cantiere di via Roma", "title": "Foto 1"},
        {"document_id": "d2", "content": "", "title": "Foto 2"},
    ]
    store = MagicMock()
    store.load_message_history.return_value = history or []

    async def collect():
        return [
            json.loads(event)
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text=search_text,
                deps=deps,
                store=store,
                chat_id="c1",
                user_id="u1",
                chat_sequence_number=2 if history else 1,
            )
        ]

    return asyncio.run(collect()), calls, prompts, searches, store


@pytest.mark.parametrize("blank", ["", "   "])
def test_an_image_alone_skips_the_stages_that_read_the_question(monkeypatch, blank):
    events, calls, _, searches, _ = _turn(monkeypatch, search_text=blank, media=IMAGE)

    assert calls == []
    assert len(searches) == 1
    assert searches[0]["media"] == IMAGE
    assert searches[0]["query"] == ""
    assert [e["type"] for e in events][-1] == "END"


def test_an_image_alone_is_answered_from_the_text_of_what_was_found(monkeypatch):
    events, _, prompts, _, store = _turn(monkeypatch, search_text="", media=IMAGE)

    instructions, user_prompt = prompts[0]
    assert entrypoint.MEDIA_ONLY_QUERY_INSTRUCTION in instructions
    assert "Cantiere di via Roma" in user_prompt
    text = "".join(e["chunk"] for e in events if e["type"] == "CHUNK")
    assert text == "Le immagini mostrano due cantieri."
    assert [e["chunk"]["title"] for e in events if e["type"] == "DOCUMENT"] == [
        "Foto 1",
        "Foto 2",
    ]
    store.save_turn.assert_called_once()


def test_an_image_alone_after_a_written_question_gets_its_instruction(monkeypatch):
    _, _, prompts, _, _ = _turn(monkeypatch, search_text="", media=IMAGE, history=TEXT_TURN)

    instructions, _ = prompts[0]
    assert entrypoint.MEDIA_ONLY_QUERY_INSTRUCTION in instructions


@pytest.mark.parametrize("history", [IMAGE_TURN, CLIENT_HISTORY], ids=["image", "client"])
def test_the_orchestrator_gets_its_prompt_after_any_turn(history):
    received = []

    def model(messages, info: AgentInfo) -> ModelResponse:
        received.append(_instructions(messages))
        output = {"status": "ok", "message": "risposta", "used_document_ids": []}
        return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, output)])

    asyncio.run(
        orchestrator.run_orchestrator(
            question="e il cantiere di via Milano?",
            deps=_deps(),
            message_history=history,
            model=FunctionModel(model),
        )
    )

    assert "You are the Orchestrator" in received[0]
    assert entrypoint.MEDIA_ONLY_QUERY_INSTRUCTION not in received[0]


def test_an_image_with_text_takes_the_usual_path(monkeypatch):
    _, calls, _, _, _ = _turn(monkeypatch, search_text="cosa mostra?", media=IMAGE)

    assert calls == ["guardrail", "intent", "orchestrator"]


def test_without_an_image_nothing_changes(monkeypatch):
    _, calls, _, _, _ = _turn(monkeypatch, search_text="domanda", media=None)

    assert calls == ["guardrail", "intent", "orchestrator"]


def test_the_image_of_the_request_reaches_the_deps(monkeypatch):
    captured = {}

    async def fake_run_turn(*, deps, **_):
        captured["media"] = deps.media
        yield json.dumps({"chunk": "", "type": "END"})

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
            search_text="",
            chat_id=None,
            user_id=None,
            tenant_id="t1",
            retrieve_from_uploaded_documents=False,
            chat_history=None,
            timestamp=None,
            chat_sequence_number=1,
            rag_configuration={},
            llm_configuration={},
            guardrails_configuration={},
            opensearch_host="http://os",
            grpc_host_embedding="em:9000",
            grpc_host_datasource="ds:9000",
            media=IMAGE,
        ):
            pass

    asyncio.run(collect())

    assert captured["media"] == IMAGE
