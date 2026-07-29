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

"""An embedding error on the text path is fail-fast: the RPC ends with
gRPC status INTERNAL (v1 parity), unlike the per-ref skip."""

import grpc
import pytest

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2


def _raise(texts):
    raise RuntimeError("embedding backend down")


def _failing_pipelines(configuration, chunker):
    return Pipelines(
        embed_texts=_raise,
        chunk=lambda text: text.split(),
        fetch=lambda url: (b"", None),
        embed_image=None,
    )


def test_text_embedding_error_aborts_with_internal(make_stub):
    stub = make_stub(_failing_pipelines)
    request = embedding_pb2.EmbedContentRequest(tenantId="mew", text="boom")

    with pytest.raises(grpc.RpcError) as error:
        list(stub.EmbedContent(request))

    assert error.value.code() == grpc.StatusCode.INTERNAL
