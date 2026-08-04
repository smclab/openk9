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


import base64
from unittest.mock import patch

import grpc
import pytest
from fastapi import HTTPException
from google.protobuf.json_format import ParseDict

from app.external_services.grpc.grpc_client import (
    UNEXPECTED_ERROR_MESSAGE,
    query_parser,
)
from app.external_services.grpc.searcher.searcher_pb2 import SearchTokenRequest

IMAGE_BYTES = b"\x89PNG\r\n\x1a\nfake"
IMAGE_DATA = base64.b64encode(IMAGE_BYTES).decode()


class _RpcError(grpc.RpcError):
    """gRPC error with the code and details the datasource would send back."""

    def __init__(self, code, details):
        self._code = code
        self._details = details

    def code(self):
        return self._code

    def details(self):
        return self._details


def _query_parser(**overrides):
    arguments = {
        "search_query": [{"tokenType": "KNN", "values": []}],
        "range_values": [0, 5],
        "after_key": None,
        "suggest_keyword": None,
        "suggestion_category_id": None,
        "tenant_id": "tenant-1",
        "jwt": None,
        "extra": {},
        "sort": None,
        "sort_after_key": None,
        "language": None,
        "grpc_host": "localhost:50051",
    }
    arguments.update(overrides)
    return query_parser(**arguments)


def test_media_survives_the_proto_conversion():
    # The module keeps its own copy of searcher.proto. When that copy lags
    # behind the core one, ParseDict drops the unknown field without a word and
    # the image never leaves the module.
    token = ParseDict(
        {
            "tokenType": "KNN",
            "values": [],
            "media": {"data": IMAGE_DATA, "contentType": "image/png"},
        },
        SearchTokenRequest(),
    )

    assert token.HasField("media")
    assert token.media.data == IMAGE_BYTES
    assert token.media.contentType == "image/png"


def test_token_without_media_leaves_the_field_unset():
    token = ParseDict({"tokenType": "TEXT", "values": ["ciao"]}, SearchTokenRequest())

    assert not token.HasField("media")


def test_invalid_argument_is_reported_as_a_bad_request():
    # The datasource is the authority on what a query may contain and refuses
    # with INVALID_ARGUMENT. Flattening that into a 500 loses the reason, which
    # is the only thing the caller can act on.
    details = "media.contentType must be image/*, got application/pdf"
    error = _RpcError(grpc.StatusCode.INVALID_ARGUMENT, details)

    with patch("app.external_services.grpc.grpc_client.grpc.insecure_channel"), patch(
        "app.external_services.grpc.grpc_client.searcher_pb2_grpc.SearcherStub"
    ) as mock_stub:
        mock_stub.return_value.QueryParser.side_effect = error

        with pytest.raises(HTTPException) as raised:
            _query_parser()

    assert raised.value.status_code == 400
    assert raised.value.detail == details


def test_other_grpc_failures_stay_opaque():
    error = _RpcError(grpc.StatusCode.UNAVAILABLE, "connection refused")

    with patch("app.external_services.grpc.grpc_client.grpc.insecure_channel"), patch(
        "app.external_services.grpc.grpc_client.searcher_pb2_grpc.SearcherStub"
    ) as mock_stub:
        mock_stub.return_value.QueryParser.side_effect = error

        with pytest.raises(HTTPException) as raised:
            _query_parser()

    # An unreachable searcher is not the caller's fault: the behaviour and the
    # opaque message are unchanged.
    assert raised.value.status_code == 500
    assert raised.value.detail == UNEXPECTED_ERROR_MESSAGE
