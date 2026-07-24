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

"""chunk_text flattens a chunker's output to plain strings; build_chunker
selects and coerces only the config entries matching the chunker
signature. Fakes stand in for the real chonkie chunkers."""

from app.embedding import chunking


class _FakeChunk:
    def __init__(self, text):
        self.text = text


class _FakeChunker:
    def chunk(self, text):
        return [_FakeChunk(piece) for piece in text.split()]


def test_chunk_text_returns_plain_strings():
    assert chunking.chunk_text(_FakeChunker(), "alfa beta gamma") == [
        "alfa",
        "beta",
        "gamma",
    ]


def test_build_chunker_coerces_and_filters_via_signature(monkeypatch):
    class _TypedChunker:
        def __init__(self, chunk_size: int = 0):
            self.chunk_size = chunk_size

        def chunk(self, text):
            return []

    monkeypatch.setattr(chunking, "_chunker_class", lambda chunk_type: _TypedChunker)

    # "512" is coerced to int, "unknown" is dropped (not in the signature)
    chunker = chunking.build_chunker(0, {"chunk_size": "512", "unknown": 1})

    assert chunker.chunk_size == 512
