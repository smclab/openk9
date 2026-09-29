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

"""The model failing on the content of a ref ends the RPC with INTERNAL, as
it does on the text: skipping the ref would drop its content from the
index with no error for the caller."""

import grpc
import pytest

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

VECTOR = [3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0]

STORAGE = {
    "https://signed/doc": (b"rotto", "text/markdown"),
    "https://signed/img": (b"png", "image/png"),
}


def _embed_texts(texts):
    if "rotto" in texts:
        raise RuntimeError("Invalid parameter combination")

    return [VECTOR for _ in texts]


def _failing_image(data, content_type):
    raise RuntimeError("image rejected by the provider")


def _pipelines(configuration, chunker):
    return Pipelines(
        embed_texts=_embed_texts,
        chunk=lambda text: text.split(),
        fetch=lambda url: STORAGE[url],
        embed_image=_failing_image,
    )


def _consume(stub, request):
    received = []

    with pytest.raises(grpc.RpcError) as error:
        for chunk in stub.EmbedContent(request):
            received.append(chunk)

    return received, error.value


def test_text_ref_embedding_error_aborts_with_internal(make_stub):
    stub = make_stub(_pipelines)
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        text="uno",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/doc", fileId="doc", contentType="text/markdown"
            )
        ],
    )

    received, error = _consume(stub, request)

    # the text chunk was already streamed; the failed ref ends the RPC
    assert [chunk.text for chunk in received] == ["uno"]
    assert error.code() == grpc.StatusCode.INTERNAL
    assert "Invalid parameter combination" in error.details()


def test_image_ref_embedding_error_aborts_with_internal(make_stub):
    stub = make_stub(_pipelines)
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/img", fileId="img", contentType="image/png"
            )
        ],
    )

    received, error = _consume(stub, request)

    assert received == []
    assert error.code() == grpc.StatusCode.INTERNAL
