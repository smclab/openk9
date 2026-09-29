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


import pytest
from chonkie import TokenChunker
from chonkie.types import Chunk

import metrics.redundancy_bloat as redundancy_bloat


@pytest.fixture(autouse=True)
def word_tokenizer(monkeypatch):
    # The gpt2 tokenizer comes from Hugging Face: count words instead, so the
    # expected scores are easy to derive and no download is needed.
    monkeypatch.setattr(
        redundancy_bloat,
        "TokenChunker",
        lambda tokenizer, chunk_size: TokenChunker(
            tokenizer="word", chunk_size=chunk_size
        ),
    )


def test_calculate_rb_is_zero_when_chunks_partition_the_document():
    chunks = [Chunk(text="uno due"), Chunk(text="tre quattro")]

    assert redundancy_bloat.calculate_rb(chunks, "uno due tre quattro") == {
        "redundancy": 0.0
    }


def test_calculate_rb_measures_the_overlap_between_chunks():
    chunks = [Chunk(text="uno due tre"), Chunk(text="tre quattro")]

    assert redundancy_bloat.calculate_rb(chunks, "uno due tre quattro") == {
        "redundancy": 0.25
    }
