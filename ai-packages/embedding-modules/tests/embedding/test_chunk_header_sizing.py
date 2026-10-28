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
"""build_header_chunker leaves room for the chunk header: chunk_size shrinks
by the header length in the unit of the chunker, a header over 25% of
chunk_size is refused, a chunker without chunk_size takes it unchecked."""

import logging

import pytest
from app.embedding import chunking

HEADER = "title: Manuale motore\n"


def test_no_header_builds_the_chunker_as_before(monkeypatch):
    calls = []
    monkeypatch.setattr(
        chunking,
        "build_chunker",
        lambda chunk_type, config: calls.append((chunk_type, config)) or "built",
    )

    chunker = chunking.build_header_chunker(1, {"chunk_size": 400}, "", [])

    assert chunker == "built"
    assert calls == [(1, {"chunk_size": 400})]


def test_text_splitter_subtracts_characters():
    chunker = chunking.build_header_chunker(1, {"chunk_size": 400}, HEADER, ["title"])

    assert chunker._chunk_size == 400 - len(HEADER)


def test_fast_chunker_subtracts_utf8_bytes():
    header = "title: Città\n"

    chunker = chunking.build_header_chunker(10, {"chunk_size": 400}, header, ["title"])

    assert chunker.chunk_size == 400 - len(header.encode("utf-8"))
    assert len(header.encode("utf-8")) == len(header) + 1


def test_token_chunker_subtracts_tokens_from_the_default_chunk_size():
    # no chunk_size in the configuration: the default of the chunker applies
    default = chunking.build_chunker(2, {}).chunk_size

    chunker = chunking.build_header_chunker(2, {}, HEADER, ["title"])

    assert chunker.chunk_size == default - chunker.tokenizer.count_tokens(HEADER)


def test_header_over_a_quarter_of_chunk_size_is_refused():
    header = "title: " + "x" * 94 + "\n"  # 102 characters

    with pytest.raises(chunking.HeaderTooLong) as error:
        chunking.build_header_chunker(1, {"chunk_size": 400}, header, ["title"])

    message = str(error.value)
    assert "102 characters" in message
    assert "100 characters" in message
    assert "title" in message


def test_header_over_a_quarter_is_measured_in_bytes_by_the_fast_chunker():
    header = "title: " + "à" * 50 + "\n"  # 58 characters, 108 bytes

    with pytest.raises(chunking.HeaderTooLong) as error:
        chunking.build_header_chunker(10, {"chunk_size": 400}, header, ["title"])

    message = str(error.value)
    assert "108 bytes" in message
    assert "100 bytes" in message


def test_header_at_a_quarter_of_chunk_size_is_accepted():
    header = "title: " + "x" * 92 + "\n"  # 100 characters

    chunker = chunking.build_header_chunker(1, {"chunk_size": 400}, header, ["title"])

    assert chunker._chunk_size == 300


class _NoChunkSize:
    def __init__(self, min_characters_per_chunk: int = 10):
        self.min_characters_per_chunk = min_characters_per_chunk


def test_chunker_without_chunk_size_takes_the_header_with_a_warning(
    monkeypatch, caplog
):
    monkeypatch.setattr(chunking, "_chunker_class", lambda chunk_type: _NoChunkSize)

    with caplog.at_level(logging.WARNING):
        chunker = chunking.build_header_chunker(9, {}, HEADER, ["title"])

    assert isinstance(chunker, _NoChunkSize)
    assert "has no chunk_size" in caplog.text
    assert "is not checked" in caplog.text


def test_header_and_chunk_stay_within_chunk_size():
    header = "title: Manuale motore\ncategory: Barche\n"
    text = " ".join(f"parola{index}" for index in range(2000))

    chunker = chunking.build_header_chunker(
        1, {"chunk_size": 512, "chunk_overlap": 0}, header, ["title", "category"]
    )
    chunks = chunking.chunk_text(chunker, text)

    assert len(chunks) > 1
    assert all(len(header + chunk) <= 512 for chunk in chunks)
