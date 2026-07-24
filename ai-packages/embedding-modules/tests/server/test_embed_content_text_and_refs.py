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

"""text + refs: text chunks are emitted first (no fileId), then the ref
chunks (with fileId), numbered progressively over the whole stream. With
refs present the total is not known upfront, so it is left unset."""

from app.external_services.grpc.embedding import embedding_pb2


def test_text_chunks_precede_ref_chunks_with_correct_marking(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        text="uno due",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/img-1", fileId="img-1", contentType="image/png"
            )
        ],
    )

    chunks = list(stub.EmbedContent(request))

    assert [chunk.number for chunk in chunks] == [1, 2, 3]
    # first the two text chunks (no fileId), then the image chunk (fileId)
    assert [chunk.HasField("fileId") for chunk in chunks] == [False, False, True]
    assert chunks[2].fileId == "img-1"
    assert chunks[2].text == ""
    # total is unknown when refs are present
    assert all(not chunk.HasField("total") for chunk in chunks)
