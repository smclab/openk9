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

"""Several refs: one chunk per binary, each carrying its own fileId, in
the order of the refs list."""

from app.external_services.grpc.embedding import embedding_pb2


def test_each_ref_chunk_carries_its_file_id_in_order(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/img-1", fileId="img-1", contentType="image/png"
            ),
            embedding_pb2.MediaRef(
                url="https://signed/img-2", fileId="img-2", contentType="image/png"
            ),
        ],
    )

    chunks = list(stub.EmbedContent(request))

    assert [chunk.number for chunk in chunks] == [1, 2]
    assert [chunk.fileId for chunk in chunks] == ["img-1", "img-2"]
