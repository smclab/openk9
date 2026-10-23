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

"""Orchestratore e Discovery girano a temperatura 0.

Decidono quali documenti cercare e quali tenere: a temperatura di default la stessa domanda porta a
contesti diversi fra una richiesta e l'altra (Jaccard 0,60 nella run A, contro 1,00 del modulo,
che usa temperatura 0 per gli step di utilita').
"""


from pydantic_ai.models.test import TestModel

from app.rag.agentic import discovery
from app.rag.agentic.deps import AgenticDeps
from app.rag.agentic.orchestrator import build_orchestrator


def test_orchestrator_runs_at_temperature_zero():
    agent = build_orchestrator(TestModel())

    assert agent.model_settings["temperature"] == 0


def test_discovery_runs_at_temperature_zero():
    deps = AgenticDeps(
        rag_configuration={},
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )
    agent = discovery._build_discovery_agent(TestModel(), discovery._CandidateStore(), deps)

    assert agent.model_settings["temperature"] == 0
