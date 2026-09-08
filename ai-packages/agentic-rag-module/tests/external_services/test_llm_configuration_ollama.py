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

"""The Ollama keys of the LLM jsonConfig reach the configuration dict as they
were written. Absent keys stay absent: the defaults belong to the client
construction, so every path through the module gets the same ones."""

from unittest.mock import patch

from app.external_services.grpc import grpc_client


def _get_llm_configuration(json_config):
    """Call get_llm_configuration with the gRPC layer mocked away, so only the
    jsonConfig decoding is exercised."""
    with (
        patch.object(grpc_client, "grpc"),
        patch.object(grpc_client, "searcher_pb2_grpc"),
        patch.object(grpc_client, "searcher_pb2"),
        patch.object(
            grpc_client.json_format, "MessageToDict", return_value=json_config
        ),
    ):
        return grpc_client.get_llm_configuration(
            grpc_host="localhost:50051",
            tenant_id="tenant-1",
        )


def test_ollama_keys_read_from_json_config():
    configuration = _get_llm_configuration(
        {"reasoning": True, "keep_alive": 60, "num_gpu": 34}
    )

    assert configuration["reasoning"] is True
    assert configuration["keep_alive"] == 60
    assert configuration["num_gpu"] == 34


def test_ollama_keys_absent_from_json_config():
    configuration = _get_llm_configuration({})

    assert configuration["reasoning"] is None
    assert configuration["keep_alive"] is None
    assert configuration["num_gpu"] is None


def test_keep_alive_zero_is_preserved():
    # 0 unloads the model at once and must not be read as "key absent"
    configuration = _get_llm_configuration({"keep_alive": 0})

    assert configuration["keep_alive"] == 0
