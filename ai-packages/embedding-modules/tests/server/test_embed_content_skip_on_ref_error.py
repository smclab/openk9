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
