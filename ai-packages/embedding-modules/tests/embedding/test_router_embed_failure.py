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

"""A model that fails on the content of a ref is not a property of the ref:
it raises EmbedFailed, which the caller does not treat as a skip, while
what the ref itself cannot be embedded as stays a SkipRef."""

import pytest

from app.embedding.router import EmbedFailed, Pipelines, SkipRef, ref_pieces


def _raise(*args):
    raise RuntimeError("Invalid parameter combination")


def _pipelines(storage, embed_texts=None, embed_image=None):
    return Pipelines(
        embed_texts=embed_texts or (lambda texts: [[1.0, 0.0] for _ in texts]),
        chunk=lambda text: text.split("|"),
        fetch=lambda url: storage[url],
        embed_image=embed_image or (lambda data, content_type: [0.0, 1.0]),
    )


def test_text_ref_embedding_error_raises_embed_failed():
    storage = {"https://signed/doc": (b"uno|due", "text/plain")}
    ref = {"url": "https://signed/doc", "fileId": "doc", "contentType": "text/plain"}

    with pytest.raises(EmbedFailed, match="Invalid parameter combination"):
        ref_pieces(ref, _pipelines(storage, embed_texts=_raise))


def test_image_ref_embedding_error_raises_embed_failed():
    storage = {"https://signed/img": (b"png", "image/png")}
    ref = {"url": "https://signed/img", "fileId": "img", "contentType": "image/png"}

    with pytest.raises(EmbedFailed, match="Invalid parameter combination"):
        ref_pieces(ref, _pipelines(storage, embed_image=_raise))


def test_undecodable_text_ref_stays_a_skip():
    storage = {"https://signed/bin": (b"\xff\xfe\xfa", "text/plain")}
    ref = {"url": "https://signed/bin", "fileId": "bin", "contentType": "text/plain"}

    with pytest.raises(SkipRef):
        ref_pieces(ref, _pipelines(storage, embed_texts=_raise))
