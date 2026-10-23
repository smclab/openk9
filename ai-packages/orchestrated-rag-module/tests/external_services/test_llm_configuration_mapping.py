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

"""get_llm_configuration asks the datasource for the tenant's LLM and flattens
the answer into the configuration dict the model factory reads.

Every response field and every provider key of jsonConfig lands on its own
output key. jsonConfig keys have no default here: an absent key is None, and
the provider client decides what that means.
"""

from unittest.mock import MagicMock, patch

import grpc
import pytest
from fastapi import HTTPException
from google.protobuf.struct_pb2 import Struct

from app.external_services.grpc import grpc_client
from app.external_services.grpc.searcher import searcher_pb2


def _response(json_config):
    struct = Struct()
    struct.update(json_config)
    return searcher_pb2.GetLLMConfigurationsResponse(
        apiUrl="http://llm:11434",
        apiKey="sk-llm",
        jsonConfig=struct,
        retrieveType="HYBRID",
        providerModel=searcher_pb2.ProviderModel(provider="ollama", model="qwen3"),
        contextWindow=32768,
        retrieveCitations=True,
    )


def _get_llm_configuration(get_llm_configurations):
    """Call get_llm_configuration against a stub, returning the configuration
    and the stub so the request it received can be checked."""
    stub = MagicMock()
    stub.GetLLMConfigurations = get_llm_configurations

    with (
        patch.object(grpc_client.grpc, "insecure_channel") as channel,
        patch.object(
            grpc_client.searcher_pb2_grpc, "SearcherStub", return_value=stub
        ) as stub_class,
    ):
        configuration = grpc_client.get_llm_configuration(
            grpc_host="localhost:50051",
            tenant_id="tenant-1",
        )

    channel.assert_called_once_with("localhost:50051")
    stub_class.assert_called_once_with(channel.return_value.__enter__.return_value)
    return configuration, stub


def test_every_field_and_json_key_reaches_its_output_key():
    configuration, stub = _get_llm_configuration(
        MagicMock(
            return_value=_response(
                {
                    "rerank": True,
                    "watsonx_project_id": "watsonx-1",
                    "credentials": {"type": "service_account"},
                    "chat_vertex_ai_model_garden": {"endpoint_id": "garden-1"},
                    "aws_bedrock": {"region": "eu-west-1"},
                    "reasoning": True,
                    "keep_alive": 1800,
                    "num_gpu": 34,
                }
            )
        )
    )

    stub.GetLLMConfigurations.assert_called_once_with(
        searcher_pb2.GetLLMConfigurationsRequest(tenantId="tenant-1")
    )
    assert configuration == {
        "api_url": "http://llm:11434",
        "api_key": "sk-llm",
        "model_type": "ollama",
        "model": "qwen3",
        "context_window": 32768,
        "retrieve_citations": True,
        "rerank": True,
        "retrieve_type": "HYBRID",
        "watsonx_project_id": "watsonx-1",
        "chat_vertex_ai_credentials": {"type": "service_account"},
        "chat_vertex_ai_model_garden": {"endpoint_id": "garden-1"},
        "aws_bedrock": {"region": "eu-west-1"},
        "reasoning": True,
        "keep_alive": 1800,
        "num_gpu": 34,
    }


def test_an_empty_json_config_leaves_the_provider_keys_unset():
    configuration, _ = _get_llm_configuration(MagicMock(return_value=_response({})))

    assert configuration == {
        "api_url": "http://llm:11434",
        "api_key": "sk-llm",
        "model_type": "ollama",
        "model": "qwen3",
        "context_window": 32768,
        "retrieve_citations": True,
        "rerank": None,
        "retrieve_type": "HYBRID",
        "watsonx_project_id": None,
        "chat_vertex_ai_credentials": None,
        "chat_vertex_ai_model_garden": None,
        "aws_bedrock": None,
        "reasoning": None,
        "keep_alive": None,
        "num_gpu": None,
    }


class _RpcError(grpc.RpcError):
    def details(self):
        return "unavailable"


@pytest.mark.parametrize("failure", [_RpcError(), ValueError("broken answer")])
def test_any_failure_is_an_opaque_internal_error(failure):
    with pytest.raises(HTTPException) as error:
        _get_llm_configuration(MagicMock(side_effect=failure))

    assert error.value.status_code == 500
    assert error.value.detail == grpc_client.UNEXPECTED_ERROR_MESSAGE
