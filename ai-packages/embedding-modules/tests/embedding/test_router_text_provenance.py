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

"""Provenance marking: inline-text chunks carry no fileId,
text-ref chunks carry the ref's fileId."""

from app.embedding.router import ref_pieces, text_pieces


def test_inline_text_chunks_have_no_file_id(make_pipelines):
    pieces = text_pieces("uno|due|tre", make_pipelines())

    assert [piece.text for piece in pieces] == ["uno", "due", "tre"]
    assert all(piece.file_id is None for piece in pieces)


def test_text_ref_chunks_carry_the_file_id(make_pipelines):
    storage = {"https://signed/note": (b"alfa|beta", "text/plain")}
    ref = {
        "url": "https://signed/note",
        "fileId": "note-1",
        "contentType": "text/plain",
    }

    pieces = ref_pieces(ref, make_pipelines(storage=storage))

    assert [piece.text for piece in pieces] == ["alfa", "beta"]
    assert all(piece.file_id == "note-1" for piece in pieces)
