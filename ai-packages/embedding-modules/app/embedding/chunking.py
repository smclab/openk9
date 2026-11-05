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

import logging
from typing import get_type_hints

from app.utils.chunk_arguments import build_chunk_arguments

logger = logging.getLogger(__name__)

SUPPORTED_CHUNK_TYPES = frozenset(range(11))

# Defaults of chonkie 1.4 that 1.7 changed, kept so a jsonConfig that does
# not set them chunks as before; a value in the jsonConfig wins.
LEGACY_DEFAULTS = {
    7: {"tokenizer": "character", "chunk_size": 2048},
    8: {"embedding_model": "sentence-transformers/all-MiniLM-L6-v2"},
}


class UnsupportedChunkType(ValueError):
    pass


# the share of chunk_size a chunk header may take: above it the chunks would
# keep little text and a vector ruled by the header
HEADER_SHARE_MAX = 0.25


class ChunkHeaderError(ValueError):
    """A chunk header the chunker cannot make room for: the request is
    invalid, and sending it again gives the same error."""


class HeaderTooLong(ChunkHeaderError):
    """A chunk header longer than HEADER_SHARE_MAX of chunk_size."""

    def __init__(
        self, length: int, limit: int, unit: str, chunk_size: int, fields: list[str]
    ):
        super().__init__(
            f"The chunk header is {length} {unit} long, over the limit of "
            f"{limit} {unit} ({HEADER_SHARE_MAX:.0%} of chunk_size {chunk_size}); "
            f"header fields: {', '.join(fields)}"
        )


class HeaderLeavesNoRoomForOverlap(ChunkHeaderError):
    """A chunk_size that, reduced by the chunk header, is no longer above
    chunk_overlap."""

    def __init__(
        self, length: int, unit: str, chunk_size: int, overlap: int, fields: list[str]
    ):
        super().__init__(
            f"The chunk header is {length} {unit} long: chunk_size {chunk_size} "
            f"minus the header leaves {chunk_size - length} {unit}, not more than "
            f"chunk_overlap {overlap}; header fields: {', '.join(fields)}"
        )


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
        from app.text_splitters.fast_text_splitter import Utf8FastChunker

        return Utf8FastChunker
    if chunk_type in (0, 6):
        from chonkie import RecursiveChunker

        return RecursiveChunker

    raise UnsupportedChunkType(f"Unsupported chunk type: {chunk_type}")


def chunker_arguments(chunk_type, json_config):
    """Returns (chunker_class, arguments, signature): the chunker of the
    chunk type and the json_config entries, over its legacy defaults,
    that match the constructor signature of that chunker."""
    chunker_class = _chunker_class(chunk_type)

    signature = {
        name: hint
        for name, hint in get_type_hints(chunker_class.__init__).items()
        if name != "return"
    }
    config = {**LEGACY_DEFAULTS.get(chunk_type, {}), **json_config}

    return chunker_class, build_chunk_arguments(config, signature), signature


def build_chunker(chunk_type, json_config):
    """Instantiates the configured chunker with chunker_arguments; raises
    UnsupportedChunkType for a type outside SUPPORTED_CHUNK_TYPES."""
    chunker_class, arguments, _ = chunker_arguments(chunk_type, json_config)

    return chunker_class(**arguments)


def chunk_text(chunker, text):
    """Splits text into a list of chunk strings."""
    return [chunk.text for chunk in chunker.chunk(text)]


def effective_chunk_size(chunker) -> int | None:
    """The chunk_size the chunker applies, its default when the jsonConfig
    sets none, or None for a chunker without one."""
    for name in ("chunk_size", "_chunk_size"):
        size = getattr(chunker, name, None)
        if isinstance(size, int):
            return size

    return None


def header_length(chunk_type: int, chunker, header: str) -> int:
    """The length of header in the unit of the chunker: characters for the
    text splitter, UTF-8 bytes for the fast chunker, tokens of its tokenizer
    for the others."""
    if chunk_type == 1:
        return len(header)
    if chunk_type == 10:
        return len(header.encode("utf-8"))

    return chunker.tokenizer.count_tokens(header)


def header_unit(chunk_type: int) -> str:
    """The unit of header_length for chunk_type."""
    if chunk_type == 1:
        return "characters"
    if chunk_type == 10:
        return "bytes"

    return "tokens"


def whole_overlap(overlap) -> int | None:
    """The chunk_overlap as an int when it is a whole number of units, also
    written as a float of at least 1; None for a share or no overlap."""
    if isinstance(overlap, bool):
        return None
    if isinstance(overlap, int):
        return overlap
    if isinstance(overlap, float) and overlap >= 1 and overlap.is_integer():
        return int(overlap)

    return None


def build_header_chunker(
    chunk_type: int, json_config: dict, header: str, fields: list[str]
):
    """Builds the chunker of build_chunker with room for the chunk header:
    its chunk_size is reduced by the length of the header, so header plus
    chunk stays within chunk_size. Raises HeaderTooLong when the header
    takes more than HEADER_SHARE_MAX of chunk_size, and
    HeaderLeavesNoRoomForOverlap when the reduced chunk_size is no longer
    above the chunk_overlap of the jsonConfig. A chunker without chunk_size
    is built as it is, with a warning."""
    if not header:
        return build_chunker(chunk_type, json_config)

    chunker_class, arguments, signature = chunker_arguments(chunk_type, json_config)
    chunker = chunker_class(**arguments)
    size = effective_chunk_size(chunker) if "chunk_size" in signature else None

    if size is None:
        logger.warning(
            "Chunk type %s has no chunk_size: the chunk header (%s) is "
            "embedded with every chunk and its length is not checked",
            chunk_type,
            ", ".join(fields),
        )
        return chunker

    length = header_length(chunk_type, chunker, header)
    limit = int(size * HEADER_SHARE_MAX)

    if length > limit:
        raise HeaderTooLong(length, limit, header_unit(chunk_type), size, fields)

    # only a whole overlap of the jsonConfig is checked, also as a float (the
    # jsonConfig is a Struct: 90 arrives as 90.0); a share of chunk_size
    # shrinks with it, the default overlap of a chunker is not in the
    # arguments, and an overlap not below the whole chunk_size is a
    # configuration broken without the header too, left to the chunker
    overlap = whole_overlap(arguments.get("chunk_overlap"))
    if overlap is not None and size - length <= overlap < size:
        raise HeaderLeavesNoRoomForOverlap(
            length, header_unit(chunk_type), size, overlap, fields
        )

    # semantic and late chunkers load their model twice here: accepted, the
    # header is known only per request
    return chunker_class(**{**arguments, "chunk_size": size - length})
