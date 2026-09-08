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

"""What initialize_language_model hands to ChatOllama: thinking off unless the
tenant asks for it, a keep_alive of the module's own instead of the five
minutes of Ollama, and the layer cap left to Ollama when nobody set one."""

import importlib.util
import pathlib

import pytest

MODULE_ROOT = pathlib.Path(__file__).resolve().parents[1]


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


def _ollama_configuration(**overrides):
    configuration = {
        "model_type": "ollama",
        "model": "qwen3.6:35b-a3b",
        "api_url": "http://localhost:11434",
        "api_key": "",
        "context_window": 8192,
    }
    configuration.update(overrides)

    return configuration


def test_ollama_parameters_from_configuration(llm_module):
    llm = llm_module.initialize_language_model(
        _ollama_configuration(reasoning=True, keep_alive=60, num_gpu=34)
    )

    assert llm.reasoning is True
    assert llm.keep_alive == 60
    assert llm.num_gpu == 34


def test_ollama_defaults_without_configuration(llm_module):
    llm = llm_module.initialize_language_model(_ollama_configuration())

    # thinking off: the graph classifies, it does not reason
    assert llm.reasoning is False
    assert llm.keep_alive == llm_module.DEFAULT_KEEP_ALIVE
    # no cap sent: the split between VRAM and RAM stays with Ollama
    assert llm.num_gpu is None


def test_keep_alive_zero_is_not_replaced_by_the_default(llm_module):
    llm = llm_module.initialize_language_model(_ollama_configuration(keep_alive=0))

    assert llm.keep_alive == 0


def test_json_config_numbers_reach_ollama_as_integers(llm_module):
    # a gRPC Struct decodes numbers to float, and Ollama rejects a float where
    # it expects a count of seconds or of layers
    llm = llm_module.initialize_language_model(
        _ollama_configuration(keep_alive=60.0, num_gpu=34.0)
    )

    assert type(llm.keep_alive) is int
    assert type(llm.num_gpu) is int
