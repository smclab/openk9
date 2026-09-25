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

"""get_embedding_model_configuration asks the datasource for the tenant's
embedding model and returns it as the dict every embedding call starts from.

The tenant travels with the configuration, and the multimodal flag comes from
the datasource: it decides whether media may be embedded at all, so it must
never be assumed.
"""

from unittest.mock import MagicMock, patch

import grpc
import pytest
from fastapi import HTTPException
from google.protobuf.struct_pb2 import Struct

from app.external_services.grpc import grpc_client
from app.external_services.grpc.searcher import searcher_pb2


def _response(multimodal):
    struct = Struct()
    struct.update({"region": "us-central1", "dimensions": 1408})
    return searcher_pb2.GetEmbeddingModelConfigurationsResponse(
        apiUrl="http://embedder:8080",
        apiKey="sk-embed",
        jsonConfig=struct,
        providerModel=searcher_pb2.ProviderModel(
            provider="vertex", model="multimodalembedding@001"
        ),
        vectorSize=1408,
        multimodal=multimodal,
    )


def _get_embedding_model_configuration(get_embedding_model_configurations):
    """Call get_embedding_model_configuration against a stub, returning the
    configuration and the stub so the request it received can be checked."""
    stub = MagicMock()
    stub.GetEmbeddingModelConfigurations = get_embedding_model_configurations

    with (
        patch.object(grpc_client.grpc, "insecure_channel") as channel,
        patch.object(
            grpc_client.searcher_pb2_grpc, "SearcherStub", return_value=stub
        ) as stub_class,
    ):
        configuration = grpc_client.get_embedding_model_configuration(
            grpc_host="localhost:50051",
            tenant_id="tenant-1",
        )

    channel.assert_called_once_with("localhost:50051")
    stub_class.assert_called_once_with(channel.return_value.__enter__.return_value)
    return configuration, stub


@pytest.mark.parametrize("multimodal", [True, False])
def test_every_field_reaches_its_output_key(multimodal):
    configuration, stub = _get_embedding_model_configuration(
        MagicMock(return_value=_response(multimodal))
    )

    stub.GetEmbeddingModelConfigurations.assert_called_once_with(
        searcher_pb2.GetEmbeddingModelConfigurationsRequest(tenantId="tenant-1")
    )
    assert configuration == {
        "tenant_id": "tenant-1",
        "api_url": "http://embedder:8080",
        "api_key": "sk-embed",
        "model_type": "vertex",
        "model": "multimodalembedding@001",
        "vector_size": 1408,
        "json_config": {"region": "us-central1", "dimensions": 1408},
        "multimodal": multimodal,
    }


class _RpcError(grpc.RpcError):
    def details(self):
        return "unavailable"


@pytest.mark.parametrize("failure", [_RpcError(), ValueError("broken answer")])
def test_any_failure_is_an_opaque_internal_error(failure):
    with pytest.raises(HTTPException) as error:
        _get_embedding_model_configuration(MagicMock(side_effect=failure))

    assert error.value.status_code == 500
    assert error.value.detail == grpc_client.UNEXPECTED_ERROR_MESSAGE
