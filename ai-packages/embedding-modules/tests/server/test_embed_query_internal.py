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

"""Any failure building the model or embedding the query that is neither a
malformed request nor a missing capability ends the RPC with INTERNAL,
carrying the message of the error."""

import grpc
import pytest

from app.embedding.query import QueryCapabilities
from app.external_services.grpc.embedding import embedding_pb2


def _raise(*args):
    raise RuntimeError("provider down")


def _failing_embedder(configuration):
    return QueryCapabilities(embed_text=_raise)


@pytest.mark.parametrize(
    "build_query_capabilities",
    [
        pytest.param(_failing_embedder, id="embedding"),
        pytest.param(_raise, id="model setup"),
    ],
)
def test_a_provider_error_is_internal(make_stub, build_query_capabilities):
    stub = make_stub(build_query_capabilities=build_query_capabilities)

    with pytest.raises(grpc.RpcError) as error:
        stub.EmbedQuery(embedding_pb2.EmbedQueryRequest(tenantId="mew", text="gatto"))

    assert error.value.code() == grpc.StatusCode.INTERNAL
    assert error.value.details() == "provider down"
