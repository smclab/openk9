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
Content-Type returned by the fetch (image/png in the fake store)."""

from app.external_services.grpc.embedding import embedding_pb2


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
