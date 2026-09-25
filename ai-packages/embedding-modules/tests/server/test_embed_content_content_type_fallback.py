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

"""When a MediaRef carries no contentType, the server routes it using the
Content-Type returned by the fetch (image/png in the fake store). When it
carries one, that is authoritative and the fetched type is ignored."""

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2


def _pipelines_fetching_as(response_content_type, embedded):
    """Pipelines whose fetch answers the given Content-Type, recording the
    content type the image embedder is called with."""

    def embed_image(data, content_type):
        embedded.append(content_type)
        return [0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]

    return lambda configuration, chunker: Pipelines(
        embed_texts=lambda texts: [],
        chunk=lambda text: [],
        fetch=lambda url: (b"png", response_content_type),
        embed_image=embed_image,
    )


def test_ref_without_content_type_uses_fetched_type(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            # no contentType on the ref -> fall back to the fetched image/png
            embedding_pb2.MediaRef(url="https://signed/img-1", fileId="img-1")
        ],
    )

    chunks = list(stub.EmbedContent(request))

    assert len(chunks) == 1
    assert chunks[0].fileId == "img-1"
    # routed as an image: empty text, image vector
    assert chunks[0].text == ""


def test_ref_content_type_wins_over_a_generic_fetched_type(make_stub):
    embedded = []
    stub = make_stub(_pipelines_fetching_as("application/octet-stream", embedded))
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/img", fileId="img", contentType="image/png"
            )
        ],
    )

    chunks = list(stub.EmbedContent(request))

    # routed and embedded as the image the ref declares
    assert [chunk.fileId for chunk in chunks] == ["img"]
    assert embedded == ["image/png"]


def test_ref_content_type_wins_over_a_fetched_image_type(make_stub):
    embedded = []
    stub = make_stub(_pipelines_fetching_as("image/png", embedded))
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/blob",
                fileId="blob",
                contentType="application/octet-stream",
            )
        ],
    )

    chunks = list(stub.EmbedContent(request))

    # the ref says it is no image: skipped, whatever the fetch answers
    assert chunks == []
    assert embedded == []
