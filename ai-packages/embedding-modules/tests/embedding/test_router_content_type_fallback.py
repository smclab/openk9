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

"""Content-type fallback: when the MediaRef carries no
contentType, the HTTP response Content-Type drives the modality."""

import pytest

from app.embedding.router import SkipRef, ref_pieces


def test_missing_ref_content_type_falls_back_to_http(make_pipelines):
    # ref has no contentType; the HTTP response says image/png
    storage = {"https://signed/blob": (b"png-bytes", "image/png")}
    ref = {"url": "https://signed/blob", "fileId": "blob-1", "contentType": ""}

    pieces = ref_pieces(ref, make_pipelines(storage=storage))

    assert len(pieces) == 1
    assert pieces[0].file_id == "blob-1"


def test_fallback_to_unknown_content_type_is_skipped(make_pipelines):
    storage = {"https://signed/blob": (b"data", "application/octet-stream")}
    ref = {"url": "https://signed/blob", "fileId": "blob-2", "contentType": None}

    with pytest.raises(SkipRef):
        ref_pieces(ref, make_pipelines(storage=storage))
