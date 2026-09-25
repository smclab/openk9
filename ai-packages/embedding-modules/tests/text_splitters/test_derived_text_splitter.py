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

"""DerivedTextSplitter (chunk type 1) splits on word boundaries within
chunk_size and appends to every chunk but the last the head of the next
one, at most chunk_overlap characters of whole words."""

from app.text_splitters.derived_text_splitter import DerivedTextSplitter

TEXT = "alfa beta gamma delta epsilon"


def _texts(splitter, text):
    return [chunk.text for chunk in splitter.chunk(text)]


def test_chunks_break_between_words_within_chunk_size():
    splitter = DerivedTextSplitter(chunk_size=10, chunk_overlap=0)

    assert _texts(splitter, TEXT) == ["alfa beta", "gamma", "delta", "epsilon"]


def test_every_chunk_but_the_last_carries_the_head_of_the_next():
    splitter = DerivedTextSplitter(chunk_size=10, chunk_overlap=7)

    assert _texts(splitter, TEXT) == [
        "alfa beta gamma",
        "gamma delta",
        "delta epsilon",
        "epsilon",
    ]


def test_the_overlap_keeps_only_the_words_that_fit():
    splitter = DerivedTextSplitter(chunk_size=11, chunk_overlap=5)

    # the next chunk is "gamma delta": only "gamma" fits in 5 characters
    assert _texts(splitter, "alfa beta gamma delta") == [
        "alfa beta gamma",
        "gamma delta",
    ]


def test_a_one_character_overlap_still_applies():
    splitter = DerivedTextSplitter(chunk_size=4, chunk_overlap=1)

    # the next chunk is the one-letter word "b", which fits exactly
    assert _texts(splitter, "alfa b") == ["alfa b", "b"]


def test_a_text_shorter_than_chunk_size_is_one_chunk():
    splitter = DerivedTextSplitter(chunk_size=100, chunk_overlap=20)

    assert _texts(splitter, TEXT) == [TEXT]


def test_an_empty_text_has_no_chunk():
    splitter = DerivedTextSplitter(chunk_size=10, chunk_overlap=5)

    assert _texts(splitter, "") == []
