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

"""Chunker factory driven by the ChunkType enum of the gRPC contract.

Imports are lazy: the heavyweight chunkers (semantic, neural, late) pull
in torch, and this module must stay importable in tests and in
deployments that only use the light splitters. Argument selection reuses
build_chunk_arguments; GetMessages (v1) and EmbedContent share it.
"""

from typing import get_type_hints

from app.utils.chunk_arguments import build_chunk_arguments


SUPPORTED_CHUNK_TYPES = frozenset(range(11))

# Defaults of chonkie 1.4 that 1.7 changed, kept so a jsonConfig that does
# not set them chunks as before; a value in the jsonConfig wins.
LEGACY_DEFAULTS = {
    7: {"tokenizer": "character", "chunk_size": 2048},
    8: {"embedding_model": "sentence-transformers/all-MiniLM-L6-v2"},
}


class UnsupportedChunkType(ValueError):
    pass


def is_supported(chunk_type):
    return chunk_type in SUPPORTED_CHUNK_TYPES


def _chunker_class(chunk_type):
    if chunk_type == 1:
        from app.text_splitters.derived_text_splitter import DerivedTextSplitter

        return DerivedTextSplitter
    if chunk_type in (2, 3):
        from chonkie import TokenChunker

        return TokenChunker
    if chunk_type == 4:
        from chonkie import SemanticChunker

        return SemanticChunker
    if chunk_type == 5:
        from chonkie import SentenceChunker

        return SentenceChunker
    if chunk_type == 7:
        from chonkie import TableChunker

        return TableChunker
    if chunk_type == 8:
        from chonkie import LateChunker

        return LateChunker
    if chunk_type == 9:
        from chonkie import NeuralChunker

        return NeuralChunker
    if chunk_type == 10:
        from chonkie import FastChunker

        return FastChunker
    if chunk_type in (0, 6):
        from chonkie import RecursiveChunker

        return RecursiveChunker

    raise UnsupportedChunkType(f"Unsupported chunk type: {chunk_type}")


def chunker_arguments(chunk_type, json_config):
    """Returns (arguments, signature): the json_config entries, over the
    legacy defaults of the chunk type, that match the constructor
    signature of its chunker."""
    chunker_class = _chunker_class(chunk_type)

    signature = {
        name: hint
        for name, hint in get_type_hints(chunker_class.__init__).items()
        if name != "return"
    }
    config = {**LEGACY_DEFAULTS.get(chunk_type, {}), **json_config}

    return build_chunk_arguments(config, signature), signature


def build_chunker(chunk_type, json_config):
    """Instantiates the configured chunker with chunker_arguments; raises
    UnsupportedChunkType for a type outside SUPPORTED_CHUNK_TYPES."""
    arguments, _ = chunker_arguments(chunk_type, json_config)

    return _chunker_class(chunk_type)(**arguments)


def chunk_text(chunker, text):
    """Splits text into a list of chunk strings."""
    return [chunk.text for chunk in chunker.chunk(text)]
