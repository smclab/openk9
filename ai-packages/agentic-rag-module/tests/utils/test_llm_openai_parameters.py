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

"""What initialize_language_model hands to ChatOpenAI: reasoning_effort "none"
when the tenant turns reasoning off, so that models reasoning by default accept
the function tools of structured output on chat completions, nothing at all
without a level, since other models reject the parameter, and the Responses API
when the tenant asks for a reasoning level."""

import importlib.util
import pathlib

import pytest
from pydantic import BaseModel, Field

MODULE_ROOT = pathlib.Path(__file__).resolve().parents[2]


class Grade(BaseModel):
    vote: int = Field(ge=0, le=10)


@pytest.fixture(scope="module")
def llm_module():
    """The real app.utils.llm: conftest stubs it session-wide (it pulls the
    cloud SDKs at import), so it is loaded from its file under another name and
    the stub the other suites rely on stays in place."""
    spec = importlib.util.spec_from_file_location(
        "app_utils_llm_real", MODULE_ROOT / "app/utils/llm.py"
    )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)

    return module


def _openai_configuration(**overrides):
    configuration = {
        "model_type": "openai",
        "model": "gpt-5.6-luna",
        "api_url": None,
        "api_key": "sk-test",
    }
    configuration.update(overrides)

    return configuration


def _structured_output_payload(llm):
    """The request a structured output call sends, built without reaching OpenAI."""
    bound = llm.bind_tools([Grade], tool_choice="Grade")

    return llm._get_request_payload("Grade this", **bound.kwargs)


def test_reasoning_off_sends_reasoning_effort_none_on_chat_completions(llm_module):
    llm = llm_module.initialize_language_model(_openai_configuration(reasoning=False))

    payload = _structured_output_payload(llm)

    assert "messages" in payload
    assert payload["reasoning_effort"] == "none"
    assert payload["tools"][0]["function"]["name"] == "Grade"


@pytest.mark.parametrize("reasoning", [None, True])
def test_reasoning_effort_not_sent_unless_reasoning_is_off(llm_module, reasoning):
    # gpt-4o rejects the parameter and gpt-5 rejects "none": without an explicit
    # false the request stays the one every other OpenAI model already accepts
    llm = llm_module.initialize_language_model(
        _openai_configuration(model="gpt-4o", reasoning=reasoning)
    )

    payload = _structured_output_payload(llm)

    assert llm.reasoning_effort is None
    assert "reasoning_effort" not in payload


def test_reasoning_off_keeps_the_utility_temperature(llm_module):
    # gpt-5 models lose a temperature other than 1 unless reasoning is "none"
    llm = llm_module.initialize_language_model(
        _openai_configuration(reasoning=False), temperature=0
    )

    payload = _structured_output_payload(llm)

    assert payload["temperature"] == 0


@pytest.mark.parametrize("model", ["gpt-6-luna", "gpt-5.6-luna"])
def test_reasoning_level_sends_function_tools_to_the_responses_api(llm_module, model):
    # chat completions rejects function tools together with reasoning
    llm = llm_module.initialize_language_model(
        _openai_configuration(model=model, reasoning="medium")
    )

    payload = _structured_output_payload(llm)

    assert "input" in payload
    assert "messages" not in payload
    assert payload["reasoning"] == {"effort": "medium"}
    assert payload["tools"][0]["name"] == "Grade"


def test_reasoning_level_drops_the_utility_temperature(llm_module):
    # langchain drops it only for gpt-5 names, and gpt-6-luna rejects it
    llm = llm_module.initialize_language_model(
        _openai_configuration(model="gpt-6-luna", reasoning="medium"), temperature=0
    )

    payload = _structured_output_payload(llm)

    assert "temperature" not in payload


def test_reasoning_level_keeps_the_answer_text_first_in_content(llm_module):
    # the default Responses output puts the reasoning block before the text,
    # where every node reads content[0]
    llm = llm_module.initialize_language_model(
        _openai_configuration(reasoning="medium")
    )

    assert llm.output_version == "v0"


def test_reasoning_off_stays_on_chat_completions_for_gpt_6(llm_module):
    llm = llm_module.initialize_language_model(
        _openai_configuration(model="gpt-6-luna", reasoning=False), temperature=0
    )

    payload = _structured_output_payload(llm)

    assert "messages" in payload
    assert payload["reasoning_effort"] == "none"
    assert payload["temperature"] == 0
