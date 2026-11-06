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

"""Orchestratore con ``parallel_discovery``: il fan-out passa solo da ``discover_many``.

Con il flag attivo il prompt non deve chiedere un solo ``discover`` (contraddirebbe l'addendum
sul fan-out), e le tool call parallele del modello sono disattivate: altrimenti piu' ``discover``
nello stesso turno girano in concorrenza fuori dal tetto ``max_parallel_subtasks``, che vale solo
dentro ``discover_many``. Con il flag spento resta il comportamento di base.
"""

from pydantic_ai.models.test import TestModel

from app.rag.agentic.orchestrator import build_orchestrator

SINGLE_DISCOVER = "call `discover` once per step"


def _orchestrator(parallel_discovery):
    return build_orchestrator(TestModel(), parallel_discovery=parallel_discovery)


def _system_prompt(agent):
    return "\n".join(agent._instructions)


def test_without_parallel_discovery_a_single_discover_is_requested():
    agent = _orchestrator(parallel_discovery=False)

    assert SINGLE_DISCOVER in _system_prompt(agent)
    assert (agent.model_settings or {}).get("parallel_tool_calls") is not False


def test_parallel_discovery_prompt_does_not_forbid_the_fan_out():
    prompt = _system_prompt(_orchestrator(parallel_discovery=True))

    assert SINGLE_DISCOVER not in prompt
    assert "discover_many" in prompt


def test_parallel_discovery_disables_parallel_tool_calls():
    agent = _orchestrator(parallel_discovery=True)

    assert agent.model_settings["parallel_tool_calls"] is False
