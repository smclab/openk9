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
"""The instructions of the tenant for the orchestrator and the Discovery.

``orchestrator_prompt`` and ``discovery_prompt`` of the jsonConfig are added after the default
prompt of each agent: they never replace it, because it carries the output contract and the
delegation rules. Without them the prompts are the defaults. With ``bypass_rag`` the
orchestrator keeps using ``prompt_no_rag``.
"""

import asyncio

import pytest
from pydantic_ai.messages import ModelResponse, ToolCallPart
from pydantic_ai.models.function import AgentInfo, FunctionModel
from pydantic_ai.models.test import TestModel

from app.rag.agentic import discovery, orchestrator
from app.rag.agentic.contracts import SubAgentRequest
from app.rag.agentic.deps import AgenticDeps

ORCHESTRATOR_TEXT = "Rispondi sempre con un elenco puntato"
DISCOVERY_TEXT = "Preferisci i documenti piu' recenti"


def _deps(**rag_configuration):
    return AgenticDeps(
        rag_configuration=rag_configuration,
        llm_configuration={},
        opensearch_host="http://os",
        grpc_host_datasource="ds:9000",
        grpc_host_embedding="em:9000",
        tenant_id="t1",
    )


def _orchestrator_prompt(**rag_configuration):
    agent = orchestrator._orchestrator_for(_deps(**rag_configuration), model=TestModel())
    return "\n".join(agent._instructions)


def _discovery_prompts(**rag_configuration):
    """The system prompt of every Discovery the orchestrator runs in a turn."""
    prompts = []

    def model(messages, info: AgentInfo) -> ModelResponse:
        prompts.append(messages[0].parts[0].content)
        unanswerable = next(t for t in info.output_tools if "Unanswerable" in t.name)
        return ModelResponse(parts=[ToolCallPart(unanswerable.name, {"reason": "nulla"})])

    asyncio.run(
        discovery.run_parallel_discovery(
            requests=[SubAgentRequest(question="A"), SubAgentRequest(question="B")],
            deps=_deps(**rag_configuration),
            model=FunctionModel(model),
        )
    )
    return prompts


def test_the_tenant_adds_an_instruction_to_the_orchestrator():
    prompt = _orchestrator_prompt(orchestrator_prompt=ORCHESTRATOR_TEXT)

    assert prompt.startswith(orchestrator.ORCHESTRATOR_SYSTEM_PROMPT)
    assert prompt.rstrip().endswith(ORCHESTRATOR_TEXT)
    # the default rules of delegation and output stay
    assert "Fill `used_document_ids`" in prompt


def test_the_tenant_adds_an_instruction_to_every_discovery():
    prompts = _discovery_prompts(discovery_prompt=DISCOVERY_TEXT)

    assert len(prompts) == 2
    for prompt in prompts:
        assert prompt.startswith(discovery.DISCOVERY_SYSTEM_PROMPT)
        assert DISCOVERY_TEXT in prompt


@pytest.mark.parametrize("value", [None, "", "   ", 5, True, ["a"]])
def test_without_instructions_the_prompts_are_the_defaults(value):
    configuration = {"orchestrator_prompt": value, "discovery_prompt": value}

    assert "Instructions from the tenant" not in _orchestrator_prompt(**configuration)
    assert not any("Instructions from the tenant" in p for p in _discovery_prompts(**configuration))


def test_each_agent_gets_only_its_own_instruction():
    configuration = {"orchestrator_prompt": ORCHESTRATOR_TEXT, "discovery_prompt": DISCOVERY_TEXT}

    assert DISCOVERY_TEXT not in _orchestrator_prompt(**configuration)
    assert ORCHESTRATOR_TEXT not in _discovery_prompts(**configuration)[0]


def test_with_bypass_rag_the_prompt_no_rag_stays():
    prompt = _orchestrator_prompt(
        bypass_rag=True, prompt_no_rag="Rispondi in breve.", orchestrator_prompt=ORCHESTRATOR_TEXT
    )

    assert prompt.startswith("Rispondi in breve.")
    assert ORCHESTRATOR_TEXT not in prompt


def test_the_prompt_and_tool_description_of_the_tenant_stay_ignored():
    prompt = _orchestrator_prompt(prompt="Sei un assistente comunale.", rag_tool_description="x")

    assert "Sei un assistente comunale." not in prompt
