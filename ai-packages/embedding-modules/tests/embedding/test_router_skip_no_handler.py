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

"""Skip semantics: a ref with no phase-1 handler (audio,
video, unknown) or that the model cannot embed (image on a text-only
model, non-decodable text) raises SkipRef; the caller skips it."""

import pytest

from app.embedding.router import SkipRef, classify, ref_pieces


def test_classify_maps_modalities():
    assert classify("image/png") == "image"
    assert classify("text/markdown; charset=utf-8") == "text"
    assert classify("audio/mpeg") == "audio"
    assert classify("video/mp4") == "video"
    assert classify("application/pdf") == "unknown"
    assert classify(None) == "unknown"


def test_audio_ref_is_skipped_before_fetching(make_pipelines):
    # empty storage: a fetch attempt would KeyError, proving the skip
    # happens on the authoritative content type, before any download
    ref = {
        "url": "https://signed/clip",
        "fileId": "clip-1",
        "contentType": "audio/wav",
    }

    with pytest.raises(SkipRef):
        ref_pieces(ref, make_pipelines())


def test_image_ref_on_text_only_model_is_skipped(make_pipelines):
    storage = {"https://signed/img": (b"png-bytes", "image/png")}
    ref = {
        "url": "https://signed/img",
        "fileId": "img-1",
        "contentType": "image/png",
    }

    with pytest.raises(SkipRef):
        ref_pieces(ref, make_pipelines(storage=storage, embed_image=None))


def test_non_utf8_text_ref_is_skipped(make_pipelines):
    storage = {"https://signed/bin": (b"\xff\xfe\x00", "text/plain")}
    ref = {
        "url": "https://signed/bin",
        "fileId": "bin-1",
        "contentType": "text/plain",
    }

    with pytest.raises(SkipRef):
        ref_pieces(ref, make_pipelines(storage=storage))
