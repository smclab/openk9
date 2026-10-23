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
"""Sources numbered with ``[n]`` markers, with the contract of agentic-rag-module (#2329).

A source gets its number when it is delivered to the orchestrator, one per document for the turn,
also across delegations and in a fan-out, where the order of the sub-questions decides. The
markers decide which sources the answer used: SOURCES before the answer lists all the delivered
ones, the DOCUMENT events only the marked ones, with their number and the quotes that are really
in them. The saved turn keeps both, so that a reopened chat renders the same ``[n]``.
"""

import asyncio
import json

import pytest
from fastapi.testclient import TestClient
from pydantic_ai.messages import ModelResponse, ToolCallPart, ToolReturnPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

import app.server as server
from app.rag.agentic import citations, discovery, orchestrator, streaming
from app.rag.agentic.contracts import (
    DiscoveredDocument,
    DiscoveryResult,
    DocumentRef,
    OrchestratorOutput,
)
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.persistence import build_checkpoint
from tests.rag.test_chat_compatibility import FakeOpenSearch, _store


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _delivered(deps, *documents):
    """Documents delivered to the orchestrator, in this order, with their content."""
    for document_id, content in documents:
        deps.discovered[document_id] = {"content": content, "title": f"T-{document_id}"}
        citations.source_number(deps, document_id)


def _output(message, used=(), quotes=None):
    return OrchestratorOutput(
        status="ok", message=message, used_document_ids=list(used), citations=quotes or {}
    )


# --------------------------------------------------------------------------- #
# The numbers                                                                  #
# --------------------------------------------------------------------------- #
def test_a_source_keeps_its_number_for_the_whole_turn():
    deps = _deps()

    assert citations.source_number(deps, "bando-a") == 1
    assert citations.source_number(deps, "faq") == 2
    assert citations.source_number(deps, "bando-b") == 3
    assert citations.source_number(deps, "faq") == 2


def _fan_out_orchestrator(questions):
    returned = []

    def model(messages, info: AgentInfo) -> ModelResponse:
        returns = [
            part.content
            for message in messages
            for part in message.parts
            if isinstance(part, ToolReturnPart) and part.tool_name == "discover_many"
        ]
        if not returns:
            requests = [{"question": question} for question in questions]
            return ModelResponse(parts=[ToolCallPart("discover_many", {"requests": requests})])
        returned.append(returns[0])
        args = {"status": "ok", "message": "confronto", "used_document_ids": []}
        return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, args)])

    return FunctionModel(model), returned


def test_in_a_fan_out_the_order_of_the_sub_questions_decides(monkeypatch):
    """The second sub-question finishes first, and still gets the later numbers."""
    delays = {"bando A": 0.03, "bando B": 0.0}

    async def fake_discovery(*, request, deps, **_):
        await asyncio.sleep(delays[request.question])
        ids = {"bando A": ["a", "faq"], "bando B": ["b", "faq"]}[request.question]
        return DiscoveryResult(
            documents=[DiscoveredDocument(document_id=i, content=i) for i in ids]
        )

    monkeypatch.setattr(discovery, "run_discovery", fake_discovery)
    model, returned = _fan_out_orchestrator(["bando A", "bando B"])
    deps = _deps(parallel_discovery=True)

    asyncio.run(orchestrator.run_orchestrator(question="confronta", deps=deps, model=model))

    numbers = [
        [(d["document_id"], d["source"]) for d in subtask["documents"]]
        for subtask in returned[0]["subtasks"]
    ]
    assert numbers == [[("a", 1), ("faq", 2)], [("b", 3), ("faq", 2)]]


def test_the_prompt_asks_for_the_markers_and_the_quotes_only_when_wanted():
    plain = orchestrator._build_system_prompt(bypass_rag=False, prompt_no_rag=None)
    quoted = orchestrator._build_system_prompt(
        bypass_rag=False, prompt_no_rag=None, retrieve_citations=True
    )
    bypass = orchestrator._build_system_prompt(bypass_rag=True, prompt_no_rag=None)

    assert "SMC opera dal 1995 [2]" in plain
    assert "leave `citations` empty" in plain
    assert "Fill `citations`" not in plain
    assert "Fill `citations`" in quoted
    assert "[2]" not in bypass


def test_the_tenant_flag_reaches_the_prompt():
    agent = orchestrator._orchestrator_for(_deps(retrieve_citations=True), model="test")

    assert "Fill `citations`" in "\n".join(agent._system_prompts)


# --------------------------------------------------------------------------- #
# Which sources the answer cites                                               #
# --------------------------------------------------------------------------- #
def test_the_markers_decide_the_cited_sources():
    deps = _deps()
    _delivered(deps, ("a", "testo a"), ("faq", "testo faq"), ("b", "testo b"))

    refs = citations.cited_refs(
        deps, _output("Il bando B scade il 15 [3]. Il bando A il 30 [1].", used=["faq"])
    )

    assert [(r.document_id, r.ordinal) for r in refs] == [("a", 1), ("b", 3)]


def test_an_invented_number_cites_nothing():
    deps = _deps()
    _delivered(deps, ("a", "testo a"))

    refs = citations.cited_refs(deps, _output("Vero [1]. Inventato [7]."))

    assert [r.document_id for r in refs] == ["a"]


def test_an_answer_without_markers_falls_back_to_the_used_documents():
    deps = _deps()
    _delivered(deps, ("a", "testo a"), ("b", "testo b"))

    refs = citations.cited_refs(deps, _output("Nessun marcatore.", used=["b"]))

    assert [(r.document_id, r.ordinal) for r in refs] == [("b", None)]


def test_a_sentence_removed_in_repair_takes_its_source_away():
    """The Verifier asks to remove a sentence: the final answer decides, not the first one."""
    deps = _deps()
    _delivered(deps, ("a", "testo a"), ("b", "testo b"))

    first = citations.cited_refs(deps, _output("Uno [1]. Due [2]."))
    repaired = citations.cited_refs(deps, _output("Uno [1]."))

    assert [r.document_id for r in first] == ["a", "b"]
    assert [r.document_id for r in repaired] == ["a"]


def test_only_the_quotes_really_in_the_source_are_kept():
    deps = _deps()
    _delivered(
        deps,
        ("a", "L'eccellenza dal 1995. Sede a Treviso. Terzo fatto."),
        ("b", "Altro documento."),
    )
    quotes = {
        "1": ["l’eccellenza dal 1995", "Sede a Roma", "Sede a Treviso", "Terzo fatto"],
        "2": ["Altro documento"],
        "x": ["chiave non numerica"],
    }

    refs = citations.cited_refs(deps, _output("Dal 1995 [1].", quotes=quotes))

    assert [(r.document_id, r.quotes) for r in refs] == [
        ("a", ["l’eccellenza dal 1995", "Sede a Treviso"])
    ]


# --------------------------------------------------------------------------- #
# What the client receives                                                     #
# --------------------------------------------------------------------------- #
def test_document_events_carry_the_number_and_the_quotes():
    refs = [
        DocumentRef(document_id="a", title="A", ordinal=1, quotes=["dal 1995"]),
        DocumentRef(document_id="b", title="B"),
    ]

    chunks = [json.loads(event)["chunk"] for event in streaming.document_events(refs)]

    assert chunks == [
        {"ordinal": 1, "citations": [{"quote": "dal 1995"}], "title": "A"},
        {"citations": [], "title": "B"},
    ]


def test_sources_lists_every_delivered_source():
    deps = _deps()
    _delivered(deps, ("a", "testo a"), ("b", "testo b"))

    assert citations.sources(deps) == [
        {"ordinal": 1, "document_id": "a", "title": "T-a", "url": None},
        {"ordinal": 2, "document_id": "b", "title": "T-b", "url": None},
    ]


def _stream(sources):
    async def post_stream():
        return None, False, []

    async def collect():
        return [
            json.loads(event)
            async for event in streaming.stream_answer(
                deltas=streaming.deltas_from_message("risposta [1]"),
                rag_type="CHAT_RAG",
                post_stream=post_stream,
                sources=lambda: sources,
            )
        ]

    return asyncio.run(collect())


def test_the_sources_come_before_the_answer():
    events = _stream([{"ordinal": 1, "document_id": "a", "title": "A", "url": None}])

    assert [e["type"] for e in events] == ["STATUS", "SOURCES", "START", "CHUNK", "END"]
    assert events[1]["chunk"][0]["ordinal"] == 1


def test_no_sources_no_sources_event():
    assert "SOURCES" not in [e["type"] for e in _stream([])]


# --------------------------------------------------------------------------- #
# The saved turn                                                               #
# --------------------------------------------------------------------------- #
def _checkpoint(refs):
    return build_checkpoint(
        original_query="q",
        current_query="q",
        response="r",
        conversation_title=None,
        chat_sequence_number=1,
        retrieve_from_uploaded_documents=False,
        used_document_refs=refs,
        message_history=[],
    )["channel_values"]


def test_a_turn_with_markers_saves_numbers_and_quotes():
    values = _checkpoint(
        [
            DocumentRef(document_id="a", ordinal=1, quotes=["dal 1995"]),
            DocumentRef(document_id="b", ordinal=3, quotes=[]),
        ]
    )

    assert values["citations"] == {"a": ["dal 1995"], "b": []}
    assert values["citation_ordinals"] == {"1": "a", "3": "b"}


def test_a_turn_without_markers_saves_neither():
    values = _checkpoint([DocumentRef(document_id="a")])

    assert "citations" not in values
    assert "citation_ordinals" not in values


def test_a_reopened_chat_renders_the_same_numbers(monkeypatch):
    client = FakeOpenSearch()
    store = _store(monkeypatch, client)
    store.save_turn(
        chat_id="chat-1",
        chat_sequence_number=1,
        original_query="Scadenze?",
        current_query="Scadenze?",
        response="A il 30 [1]. B il 15 [3].",
        conversation_title="Scadenze",
        retrieve_from_uploaded_documents=False,
        used_document_refs=[
            DocumentRef(document_id="a", title="A", ordinal=1, quotes=["il 30"]),
            DocumentRef(document_id="b", title="B", ordinal=3, quotes=[]),
        ],
        message_history=[],
    )
    monkeypatch.setattr(server, "get_opensearch_client", lambda _host: client)
    monkeypatch.setattr(server, "decode_token", lambda _token: {"sub": "user-1"})

    response = TestClient(server.app).get(
        "/api/rag/chat/chat-1",
        headers={"authorization": "Bearer token", "x-tenant-id": "tenant-1"},
    )

    sources = response.json()["messages"][0]["sources"]
    assert [(s["document_id"], s["ordinal"], s["citations"]) for s in sources] == [
        ("a", 1, [{"quote": "il 30"}]),
        ("b", 3, []),
    ]
