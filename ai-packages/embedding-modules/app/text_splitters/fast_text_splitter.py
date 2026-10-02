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

import chonkie_core
from chonkie import FastChunker
from chonkie.types import Chunk


def _is_char_start(text_bytes, offset):
    return offset >= len(text_bytes) or text_bytes[offset] & 0xC0 != 0x80


class Utf8FastChunker(FastChunker):
    """FastChunker that never cuts a multi-byte character.

    chonkie-core splits on byte offsets: with no delimiter in the window
    it cuts at chunk_size bytes, possibly inside a character, and
    FastChunker then fails to decode the chunk. Such a cut is dropped and
    the span it falls in is split again at chunk_size bytes, each cut
    moved back to the start of its character; every other chonkie-core
    cut is kept as it is.
    """

    def chunk(self, text: str) -> list[Chunk]:
        if not text:
            return []

        kwargs = {
            "size": self.chunk_size,
            "prefix": self.prefix,
            "consecutive": self.consecutive,
            "forward_fallback": self.forward_fallback,
        }
        if self.pattern:
            kwargs["pattern"] = self.pattern
        else:
            kwargs["delimiters"] = self.delimiters

        text_bytes = text.encode("utf-8")
        offsets = chonkie_core.chunk_offsets(text_bytes, **kwargs)

        cuts = []
        start = 0
        dropped = False
        for _, end in offsets:
            if not _is_char_start(text_bytes, end):
                dropped = True
                continue

            if dropped:
                cuts.extend(self._split_at_char_starts(text_bytes, start, end))
                dropped = False
            else:
                cuts.append(end)
            start = end

        chunks = []
        start = 0
        char_pos = 0
        for end in cuts:
            chunk_text = text_bytes[start:end].decode("utf-8")
            chunks.append(
                Chunk(
                    text=chunk_text,
                    start_index=char_pos,
                    end_index=char_pos + len(chunk_text),
                    token_count=0,
                )
            )
            char_pos += len(chunk_text)
            start = end

        return chunks

    def _split_at_char_starts(self, text_bytes, start, end):
        cuts = []
        while start < end:
            cut = min(end, start + self.chunk_size)
            while not _is_char_start(text_bytes, cut):
                cut -= 1
            # a chunk_size smaller than the character takes it whole
            if cut == start:
                cut += 1
                while not _is_char_start(text_bytes, cut):
                    cut += 1
            cuts.append(cut)
            start = cut

        return cuts
