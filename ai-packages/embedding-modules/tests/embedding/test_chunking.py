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
signature, over the chonkie 1.4 defaults the 1.7 upgrade changed. Fakes
stand in for the chunkers, except where the real split is checked."""

import sys
import types

import chonkie
import pytest
from app.embedding import chunking
from app.text_splitters.derived_text_splitter import DerivedTextSplitter


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


@pytest.mark.parametrize(
    "chunk_type, chunker_class, size_attribute",
    [
        (0, chonkie.RecursiveChunker, "chunk_size"),
        (1, DerivedTextSplitter, "_chunk_size"),
        (2, chonkie.TokenChunker, "chunk_size"),
        (3, chonkie.TokenChunker, "chunk_size"),
        (5, chonkie.SentenceChunker, "chunk_size"),
        (6, chonkie.RecursiveChunker, "chunk_size"),
        (7, chonkie.TableChunker, "chunk_size"),
    ],
)
def test_build_chunker_selects_the_class_and_coerces_via_signature(
    chunk_type, chunker_class, size_attribute
):
    # "512" is coerced to int, "unknown" is dropped (not in the signature)
    chunker = chunking.build_chunker(chunk_type, {"chunk_size": "512", "unknown": 1})

    assert type(chunker) is chunker_class
    assert getattr(chunker, size_attribute) == 512


class _RecordingChunker:
    def __init__(
        self, tokenizer: str = "", chunk_size: int = 0, embedding_model: str = ""
    ):
        self.arguments = {
            "tokenizer": tokenizer,
            "chunk_size": chunk_size,
            "embedding_model": embedding_model,
        }


def test_table_chunker_keeps_the_chonkie_1_4_defaults(monkeypatch):
    monkeypatch.setattr(
        chunking, "_chunker_class", lambda chunk_type: _RecordingChunker
    )

    chunker = chunking.build_chunker(7, {})

    assert chunker.arguments["tokenizer"] == "character"
    assert chunker.arguments["chunk_size"] == 2048


def test_late_chunker_keeps_the_chonkie_1_4_model(monkeypatch):
    monkeypatch.setattr(
        chunking, "_chunker_class", lambda chunk_type: _RecordingChunker
    )

    chunker = chunking.build_chunker(8, {})

    assert (
        chunker.arguments["embedding_model"] == "sentence-transformers/all-MiniLM-L6-v2"
    )


def test_json_config_overrides_the_legacy_defaults(monkeypatch):
    monkeypatch.setattr(
        chunking, "_chunker_class", lambda chunk_type: _RecordingChunker
    )

    chunker = chunking.build_chunker(7, {"tokenizer": "row", "chunk_size": 3})

    assert chunker.arguments["tokenizer"] == "row"
    assert chunker.arguments["chunk_size"] == 3


def test_small_table_stays_in_one_chunk_as_in_chonkie_1_4():
    table = "| a | b |\n|---|---|\n" + "".join(f"| {i} | x |\n" for i in range(10))

    assert len(chunking.chunk_text(chunking.build_chunker(7, {}), table)) == 1


def test_recursive_chunker_splits_as_before():
    text = "Prima frase. Seconda frase!\nTerza riga? " * 50

    pieces = chunking.chunk_text(chunking.build_chunker(6, {"chunk_size": 64}), text)

    assert len(pieces) > 1
    assert "".join(pieces) == text


def test_unknown_chunk_type_is_refused():
    assert not chunking.is_supported(99)

    with pytest.raises(chunking.UnsupportedChunkType):
        chunking.build_chunker(99, {})


@pytest.mark.parametrize(
    "chunk_type, class_name",
    [(4, "SemanticChunker"), (8, "LateChunker"), (9, "NeuralChunker")],
)
def test_heavyweight_chunk_types_resolve_their_class(
    monkeypatch, chunk_type, class_name
):
    # a stand-in chonkie: resolving the class must not load any model
    fake_chonkie = types.ModuleType("chonkie")
    for name in ("SemanticChunker", "LateChunker", "NeuralChunker"):
        setattr(fake_chonkie, name, type(name, (), {}))
    monkeypatch.setitem(sys.modules, "chonkie", fake_chonkie)

    assert chunking._chunker_class(chunk_type) is getattr(fake_chonkie, class_name)
