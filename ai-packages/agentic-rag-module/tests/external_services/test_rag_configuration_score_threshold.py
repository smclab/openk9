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

from unittest.mock import MagicMock, patch

from app.external_services.grpc import grpc_client


def _get_rag_configuration(json_config):
    """Call get_rag_configuration with the gRPC layer mocked away, so only the
    jsonConfig decoding is exercised."""
    with patch.object(grpc_client, "grpc"), patch.object(
        grpc_client, "searcher_pb2_grpc"
    ), patch.object(grpc_client, "searcher_pb2"), patch.object(
        grpc_client.json_format, "MessageToDict", return_value=json_config
    ):
        return grpc_client.get_rag_configuration(
            grpc_host="localhost:50051",
            tenant_id="tenant-1",
            rag_type="CHAT_RAG",
        )


def test_score_threshold_read_from_json_config():
    configuration = _get_rag_configuration({"score_threshold": 0.7})

    assert configuration["score_threshold"] == 0.7


def test_score_threshold_defaults_when_absent():
    # no score_threshold in jsonConfig -> the retriever still gets the default
    configuration = _get_rag_configuration({})

    assert configuration["score_threshold"] == 0.3


def test_score_threshold_zero_is_preserved():
    # 0 disables the cut and must not be replaced by the default
    configuration = _get_rag_configuration({"score_threshold": 0})

    assert configuration["score_threshold"] == 0
