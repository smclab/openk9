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

"""The batch size of the EmbedContent text resolves on three levels: the
batch_size of the jsonConfig, else the registry entry of the model, else the
global default. An invalid batch_size is ignored with a warning."""

import logging

import pytest

from app import server as server_module

OPENAI = {"model_type": "openai", "model": "text-embedding-3-small"}
OLLAMA = {"model_type": "ollama", "model": "qwen3-embedding:0.6b"}


def test_a_model_with_no_entry_takes_the_default():
    assert server_module.resolve_batch_size(OPENAI) == 256


def test_a_registered_model_takes_the_batch_size_of_its_entry():
    assert server_module.resolve_batch_size(OLLAMA) == 512


@pytest.mark.parametrize(
    "configured",
    [pytest.param(64, id="int"), pytest.param(64.0, id="float from the Struct")],
)
def test_the_json_config_wins_over_the_registry(configured):
    configuration = {**OLLAMA, "batch_size": configured}

    assert server_module.resolve_batch_size(configuration) == 64


@pytest.mark.parametrize(
    "configured",
    [
        pytest.param(0, id="zero"),
        pytest.param(-3, id="negative"),
        pytest.param(2.5, id="not whole"),
        pytest.param("64", id="string"),
        pytest.param(True, id="bool"),
    ],
)
def test_an_invalid_batch_size_is_ignored_with_a_warning(configured, caplog):
    configuration = {**OLLAMA, "batch_size": configured}

    with caplog.at_level(logging.WARNING, logger=server_module.logger.name):
        batch_size = server_module.resolve_batch_size(configuration)

    assert batch_size == 512
    assert [record.levelno for record in caplog.records] == [logging.WARNING]
    assert "batch_size" in caplog.records[0].getMessage()
