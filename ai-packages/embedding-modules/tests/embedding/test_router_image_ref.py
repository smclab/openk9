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

"""An image ref is fetched from its URL and embedded into a single chunk
carrying its fileId and an empty text."""

from app.embedding.router import ref_pieces


def test_image_ref_is_fetched_and_embedded(make_pipelines):
    storage = {"https://signed/img": (b"png-bytes", "image/png")}
    ref = {
        "url": "https://signed/img",
        "fileId": "img-1",
        "contentType": "image/png",
    }

    pieces = ref_pieces(ref, make_pipelines(storage=storage))

    assert len(pieces) == 1
    assert pieces[0].file_id == "img-1"
    assert pieces[0].text == ""
    assert pieces[0].vector == [0.0, 1.0, 0.0, 0.0]
