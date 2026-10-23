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

"""Il Discovery non puo' cercare all'infinito.

Nella run T0 un Discovery che non trovava la risposta ha chiamato il retriever 45 volte nella
stessa richiesta: ogni risultato resta nella conversazione dell'agente, e il contesto ha superato
i 128k token di gpt-4o-mini (errore all'utente). Lo stesso documento tornato da piu' ricerche
veniva inoltre concatenato a se stesso nel contenuto consegnato all'orchestratore.
"""

import asyncio


from pydantic_ai.messages import ModelResponse, ToolCallPart, ToolReturnPart
from pydantic_ai.models.function import AgentInfo, FunctionModel

from app.rag.agentic import discovery
from app.rag.agentic.contracts import DiscoveryUnanswerable, SubAgentRequest
from app.rag.agentic.deps import AgenticDeps


def _deps(retrieve_fn):
    deps = AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )
    deps.retrieve_fn = retrieve_fn
    return deps


def test_retriever_stops_after_the_search_limit():
    searches = []

    def retrieve(**kwargs):
        searches.append(kwargs["query"])
        return [{"document_id": "d1", "content": "testo"}]

    def never_satisfied(messages, info: AgentInfo) -> ModelResponse:
        # continua a cercare finche' il tool non gli dice di smettere
        last = messages[-1].parts[-1]
        if isinstance(last, ToolReturnPart) and isinstance(last.content, str):
            unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
            return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "nulla"})])
        return ModelResponse(parts=[ToolCallPart("retriever", {"question": f"q{len(searches)}"})])

    outcome = asyncio.run(
        discovery.run_discovery(
            request=SubAgentRequest(question="q"),
            deps=_deps(retrieve),
            model=FunctionModel(never_satisfied),
        )
    )

    assert len(searches) == discovery.MAX_RETRIEVER_CALLS
    assert isinstance(outcome, DiscoveryUnanswerable)


def test_the_same_chunk_found_twice_is_not_duplicated():
    store = discovery._CandidateStore()

    store.add_many([{"document_id": "d1", "content": "chunk A"}])
    store.add_many([{"document_id": "d1", "content": "chunk A"}])
    store.add_many([{"document_id": "d1", "content": "chunk B"}])

    assert store.by_id["d1"]["content"] == "chunk A\nchunk B"
