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

"""A ref that fails (fetch error) or has no phase-1 handler (audio) emits
no chunk; the stream continues with the remaining refs."""

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2


def test_failed_fetch_skips_only_that_ref(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            # not in the fake store -> fetch raises KeyError -> skip
            embedding_pb2.MediaRef(
                url="https://signed/missing", fileId="gone", contentType="image/png"
            ),
            embedding_pb2.MediaRef(
                url="https://signed/img-1", fileId="img-1", contentType="image/png"
            ),
        ],
    )

    chunks = list(stub.EmbedContent(request))

    assert [chunk.fileId for chunk in chunks] == ["img-1"]
    assert [chunk.number for chunk in chunks] == [1]


def test_ref_without_handler_is_skipped(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/clip-1", fileId="clip-1", contentType="audio/wav"
            ),
            embedding_pb2.MediaRef(
                url="https://signed/img-1", fileId="img-1", contentType="image/png"
            ),
        ],
    )

    chunks = list(stub.EmbedContent(request))

    assert [chunk.fileId for chunk in chunks] == ["img-1"]


def test_skip_in_the_middle_keeps_numbers_contiguous(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/img-1", fileId="img-1", contentType="image/png"
            ),
            # skipped (audio, no handler) between two embeddable refs
            embedding_pb2.MediaRef(
                url="https://signed/clip-1", fileId="clip-1", contentType="audio/wav"
            ),
            embedding_pb2.MediaRef(
                url="https://signed/img-2", fileId="img-2", contentType="image/png"
            ),
        ],
    )

    chunks = list(stub.EmbedContent(request))

    # the skipped ref leaves no gap in the progressive numbering
    assert [chunk.fileId for chunk in chunks] == ["img-1", "img-2"]
    assert [chunk.number for chunk in chunks] == [1, 2]


def test_ref_encoding_error_is_skipped_not_fatal(make_stub):
    # the image embedder returns a dimension that binary quantization cannot
    # pack (not a multiple of 8): building that chunk raises, and the ref must
    # be skipped like any other ref error, not break the whole stream.
    def pipelines_with_bad_image(configuration, chunker, is_query=False):
        return Pipelines(
            embed_texts=lambda texts: [[3.0, 0, 0, 0, 4.0, 0, 0, 0] for _ in texts],
            chunk=lambda text: text.split(),
            fetch=lambda url: (b"png", "image/png"),
            embed_image=lambda data, content_type: [0.1, 0.2, 0.3, 0.4, 0.5],
        )

    stub = make_stub(pipelines_with_bad_image)
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        text="uno",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/bad", fileId="bad-img", contentType="image/png"
            )
        ],
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_BINARY,
    )

    chunks = list(stub.EmbedContent(request))

    # the text chunk survives; the bad image ref is skipped, stream completes
    assert len(chunks) == 1
    assert not chunks[0].HasField("fileId")
