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
"""Intent detection: which domains of the tenant a question belongs to, and what they filter.

Two levels, as in agentic-rag-module: the domain documents at ``domain_threshold`` or more, else
the model among all the domains of the tenant. A tenant without domain documents skips both. The
detected domains become the domain filter of the Discovery; without intent detection there are
no domains at all, so no search is filtered.
"""

import asyncio
import json
from unittest.mock import MagicMock

import pytest
from pydantic_ai.messages import ModelResponse, ToolCallPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.rag.agentic import discovery, entrypoint, guardrails, intent, orchestrator, retrieval
from app.rag.agentic.contracts import OrchestratorOutput
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.retrieval import get_domains


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration={"domain_threshold": 0.8, **rag_configuration},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        orchestrator_model="MODEL",
    )


@pytest.fixture
def tenant(monkeypatch):
    """Domain documents and model under the control of the test."""
    state = {"domains": ["billing", "how-to"], "documents": [], "llm": ["how-to"], "calls": []}

    def fake_get_domains(**kwargs):
        state["calls"].append(("get_domains", kwargs))
        return state["domains"]

    def fake_retrieve(**kwargs):
        state["calls"].append(("retrieve", kwargs))
        return state["documents"]

    async def fake_classify(model, query, possible_domains, usage=None):
        state["calls"].append(("llm", list(possible_domains)))
        return state["llm"]

    monkeypatch.setattr(retrieval, "get_domains", fake_get_domains)
    monkeypatch.setattr(retrieval, "retrieve_domain_documents", fake_retrieve)
    monkeypatch.setattr(intent, "classify_domain_with_llm", fake_classify)
    monkeypatch.setattr(intent, "get_embedding_model_configuration", lambda **_: {"model": "e"})
    return state


def _detect(query="quanto costa?"):
    return asyncio.run(intent.detect_domains(query=query, deps=_deps()))


def _calls(tenant):
    return [name for name, _ in tenant["calls"]]


# --------------------------------------------------------------------------- #
# The two levels                                                               #
# --------------------------------------------------------------------------- #
def test_domains_above_the_threshold_skip_the_model(tenant):
    tenant["documents"] = [
        {"domain": "billing", "score": 0.9},
        {"domain": "billing", "score": 0.85},
        {"domain": "how-to", "score": 0.5},
    ]

    assert _detect() == (["billing", "how-to"], ["billing"])
    assert "llm" not in _calls(tenant)


def test_no_domain_above_the_threshold_asks_the_model_among_all(tenant):
    tenant["documents"] = [{"domain": "billing", "score": 0.3}]

    assert _detect() == (["billing", "how-to"], ["how-to"])
    assert ("llm", ["billing", "how-to"]) in tenant["calls"]


def test_a_tenant_without_domains_skips_the_search_and_the_model(tenant):
    tenant["domains"] = []

    assert _detect() == ([], [])
    assert _calls(tenant) == ["get_domains"]


def test_the_search_runs_on_the_domain_index_of_the_tenant(tenant):
    _detect(query="come faccio?")

    calls = dict(tenant["calls"])
    assert calls["get_domains"]["index"] == "t1-domain-documents-index"
    assert calls["retrieve"]["index"] == "t1-domain-documents-index"
    assert calls["retrieve"]["search_text"] == "come faccio?"
    assert calls["retrieve"]["embedding_model_configuration"] == {"model": "e"}


# --------------------------------------------------------------------------- #
# The model                                                                    #
# --------------------------------------------------------------------------- #
@pytest.mark.parametrize(
    "answer, domains",
    [
        ("billing", ["billing"]),
        ("", []),
        (["billing", "how-to"], ["billing", "how-to"]),
        # a name that is not a domain of the tenant would filter out every document
        ("invoices", []),
        (["billing", "invoices"], ["billing"]),
    ],
)
def test_the_model_answer_becomes_a_list_of_domains(answer, domains):
    prompts = []

    def model(messages, info: AgentInfo) -> ModelResponse:
        prompts.append(messages[0].instructions or messages[0].parts[0].content)
        return ModelResponse(parts=[ToolCallPart(info.output_tools[0].name, {"domain": answer})])

    result = asyncio.run(
        intent.classify_domain_with_llm(
            FunctionModel(model), "quanto costa?", ["billing", "how-to"]
        )
    )

    assert result == domains
    assert "billing" in prompts[0] and "how-to" in prompts[0]


# --------------------------------------------------------------------------- #
# The flag                                                                     #
# --------------------------------------------------------------------------- #
@pytest.mark.parametrize(
    "rag_type, configuration, enabled",
    [
        ("CHAT_RAG", {}, True),
        ("CHAT_RAG", {"enable_intent_detection": False}, False),
        ("CHAT_RAG_TOOL", {"enable_intent_detection": True}, True),
        ("SIMPLE_GENERATE", {}, False),
        ("SIMPLE_GENERATE", {"enable_intent_detection": True}, False),
    ],
)
def test_when_intent_detection_runs(rag_type, configuration, enabled):
    assert intent.intent_detection_enabled(rag_type, configuration) is enabled


def _turn_deps(tenant, **configuration):
    tenant["documents"] = [{"domain": "billing", "score": 0.9}]
    deps = _deps(**configuration)
    asyncio.run(entrypoint.detect_intent(rag_type="CHAT_RAG", search_text="q", deps=deps))
    return deps


def test_with_intent_detection_the_discovery_gets_the_domains(tenant):
    deps = _turn_deps(tenant)

    assert deps.allowed_domains == ["billing", "how-to"]
    assert deps.detected_domains == ["billing"]
    assert discovery.resolve_filter_domains(deps, ["how-to"]) == ["billing"]


def test_without_intent_detection_no_search_is_filtered(tenant):
    deps = _turn_deps(tenant, enable_intent_detection=False)

    assert deps.allowed_domains == []
    assert deps.detected_domains == []
    assert tenant["calls"] == []
    assert discovery.resolve_filter_domains(deps, None) is None


def test_a_failed_detection_leaves_the_search_unfiltered(tenant, monkeypatch):
    def failing(**_):
        raise ConnectionError("OpenSearch down")

    monkeypatch.setattr(retrieval, "get_domains", failing)

    deps = _turn_deps(tenant)

    assert deps.allowed_domains == []
    assert deps.detected_domains == []


# --------------------------------------------------------------------------- #
# The order of the turn                                                        #
# --------------------------------------------------------------------------- #
def _order(monkeypatch, verdict):
    calls = []

    async def fake_guardrail(**_):
        calls.append("guardrail")
        return verdict

    async def fake_detect(**_):
        calls.append("intent")

    async def fake_run_orchestrator(**_):
        calls.append("orchestrator")
        return (
            OrchestratorOutput(status="ok", message="risposta", used_document_ids=[]),
            [],
            MagicMock(all_messages=lambda: []),
        )

    monkeypatch.setattr(guardrails, "input_guardrail", fake_guardrail)
    monkeypatch.setattr(entrypoint, "detect_intent", fake_detect)
    monkeypatch.setattr(orchestrator, "run_orchestrator", fake_run_orchestrator)

    async def collect():
        return [
            json.loads(event)["type"]
            async for event in entrypoint.run_turn(
                rag_type="CHAT_RAG",
                search_text="domanda",
                deps=_deps(enable_verifier=True),
                store=MagicMock(),
            )
        ]

    return asyncio.run(collect()), calls


def test_safe_question_goes_guardrail_intent_orchestrator(monkeypatch):
    _, calls = _order(monkeypatch, guardrails.SAFE)

    assert calls == ["guardrail", "intent", "orchestrator"]


def test_blocked_question_skips_the_intent_detection(monkeypatch):
    events, calls = _order(monkeypatch, "VIOLENZA")

    assert events == ["GUARDRAIL", "END"]
    assert calls == ["guardrail"]


# --------------------------------------------------------------------------- #
# The domains of the tenant on OpenSearch                                      #
# --------------------------------------------------------------------------- #
def test_get_domains_aggregates_the_domain_field(monkeypatch):
    client = MagicMock()
    client.indices.exists.return_value = True
    client.search.return_value = {
        "aggregations": {"distinct_domains": {"buckets": [{"key": "billing"}, {"key": "how-to"}]}}
    }
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda host: client)

    assert get_domains(opensearch_host="http://os", index="t1-domain-documents-index") == [
        "billing",
        "how-to",
    ]
    body = client.search.call_args.kwargs["body"]
    assert body["aggs"]["distinct_domains"]["terms"]["field"] == "domain"
    assert body["size"] == 0


def test_get_domains_without_the_index_is_empty(monkeypatch):
    client = MagicMock()
    client.indices.exists.return_value = False
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda host: client)

    assert get_domains(opensearch_host="http://os", index="t1-domain-documents-index") == []
    client.search.assert_not_called()


def test_domain_documents_carry_their_domain(monkeypatch):
    client = MagicMock()
    client.indices.exists.return_value = True
    client.search.return_value = {
        "hits": {"hits": [{"_source": {"document_id": "d1", "domain": "billing"}, "_score": 0.9}]}
    }
    monkeypatch.setattr(retrieval, "get_opensearch_client", lambda host: client)
    monkeypatch.setattr(retrieval, "query_embedding", lambda *_: [0.1])

    documents = retrieval.retrieve_domain_documents(
        opensearch_host="http://os",
        grpc_host_embedding="em:9000",
        embedding_model_configuration={},
        index="t1-domain-documents-index",
        search_text="q",
    )

    assert documents[0]["domain"] == "billing"
    assert documents[0]["score"] == 0.9
    assert client.search.call_args.kwargs["body"] == {
        "query": {"knn": {"vector": {"vector": [0.1], "k": 10}}}
    }
