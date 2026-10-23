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

"""I parametri di ricerca della richiesta arrivano a ogni ricerca del Discovery.

Senza ``jwt`` ed ``extra`` (che porta l'header ``OPENK9_ACL``) il searcher applica l'ACL di un
anonimo, ``acl.public``: un utente loggato non trovava i documenti riservati ai suoi ruoli, che il
modulo gli restituisce. Dei token ``searchQuery`` dell'endpoint ``generate`` si tengono i soli
filtri (faccette, date: ``filter=True``), aggiunti al token testuale: il testo cercato resta la
sotto-domanda scritta dal Discovery.
"""

import asyncio
import json
from unittest.mock import MagicMock

from pydantic_ai.messages import ModelResponse, ToolCallPart, ToolReturnPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.models.models import SearchToken
from app.rag.agentic import discovery, entrypoint, retrieval
from app.rag.agentic.contracts import SubAgentRequest
from app.rag.agentic.deps import AgenticDeps

FACET = SearchToken(tokenType="TEXT", keywordKey="category", values=["bandi"], filter=True)
TYPED_TEXT = SearchToken(tokenType="HYBRID", values=["orari uffici"], filter=False)
EXTRA = {"OPENK9_ACL": "role-a"}


# --------------------------------------------------------------------------- #
# retrieve_documents: i token filtro si aggiungono al testo                    #
# --------------------------------------------------------------------------- #
def test_filter_tokens_are_appended_to_the_text_token(monkeypatch):
    seen = {}

    def fake_query_parser(**kwargs):
        seen.update(kwargs)
        return {"query": "{}", "index_name": [], "query_parameters": {}}

    monkeypatch.setattr(retrieval, "query_parser", fake_query_parser)

    retrieval.retrieve_documents(
        query="sotto-domanda",
        search_text="sotto-domanda",
        retrieve_type="HYBRID",
        tenant_id="t1",
        opensearch_host="http://os",
        grpc_host="ds:9000",
        context_window=4096,
        range_values=[],
        filter_tokens=[FACET],
        jwt="token",
        extra=EXTRA,
    )

    tokens = seen["search_query"]
    assert tokens[0]["values"] == ["sotto-domanda"]
    assert tokens[1]["keywordKey"] == "category"
    assert tokens[1]["values"] == ["bandi"]
    assert seen["jwt"] == "token"
    assert seen["extra"] == EXTRA


# --------------------------------------------------------------------------- #
# get_agentic_rag: i parametri della richiesta finiscono nei deps              #
# --------------------------------------------------------------------------- #
def test_request_search_args_reach_the_deps(monkeypatch):
    captured = {}

    async def fake_run_turn(*, deps, **_):
        captured["deps"] = deps
        yield json.dumps({"chunk": "", "type": "END"})

    monkeypatch.setattr(entrypoint, "run_turn", fake_run_turn)
    monkeypatch.setattr(entrypoint, "build_model", lambda _: MagicMock())

    async def drive():
        async for _ in entrypoint.get_agentic_rag(
            rag_type="SIMPLE_GENERATE",
            search_query=[TYPED_TEXT, FACET],
            datasource_ids=None,
            after_key=None,
            suggest_keyword=None,
            suggestion_category_id=None,
            jwt="token",
            extra=EXTRA,
            sort=[{"date": "desc"}],
            sort_after_key="k",
            language="it_IT",
            search_text="orari uffici",
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
        ):
            pass

    asyncio.run(drive())

    deps = captured["deps"]
    assert deps.request_search_args == {
        "jwt": "token",
        "extra": EXTRA,
        "sort": [{"date": "desc"}],
        "sort_after_key": "k",
        "language": "it_IT",
    }
    assert deps.filter_tokens == [FACET]


# --------------------------------------------------------------------------- #
# Discovery: ogni ricerca riceve i parametri della richiesta                   #
# --------------------------------------------------------------------------- #
def test_every_search_carries_the_request_search_args():
    calls = []
    deps = AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
        request_search_args={"jwt": "token", "extra": EXTRA},
        filter_tokens=[FACET],
    )
    deps.retrieve_fn = lambda **kwargs: calls.append(kwargs) or []

    def search_once(messages, info: AgentInfo) -> ModelResponse:
        if isinstance(messages[-1].parts[-1], ToolReturnPart):
            unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
            return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "nulla"})])
        return ModelResponse(parts=[ToolCallPart("retriever", {"question": "orari"})])

    asyncio.run(
        discovery.run_discovery(
            request=SubAgentRequest(question="orari"),
            deps=deps,
            model=FunctionModel(search_once),
        )
    )

    (call,) = calls
    assert call["jwt"] == "token"
    assert call["extra"] == EXTRA
    assert call["filter_tokens"] == [FACET]
