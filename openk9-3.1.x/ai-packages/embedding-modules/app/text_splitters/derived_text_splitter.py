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

from textwrap import wrap
from typing import List

from chonkie.chunker.base import BaseChunker
from chonkie.types import Chunk


class DerivedTextSplitter(BaseChunker):
    def __init__(self, chunk_size: int = 2048, chunk_overlap: int = 20):
        self._chunk_size = chunk_size
        self._chunk_overlap = chunk_overlap

    def chunk(self, text: str) -> List[Chunk]:
        """Split incoming text and return chunks without cutting words."""

        chunks = wrap(text, self._chunk_size)

        if self._chunk_overlap <= 0:
            return [Chunk(text=c) for c in chunks]

        items = []
        chunks_number = len(chunks)

        for index, chunk in enumerate(chunks):
            if index < chunks_number - 1:
                overlap = wrap(chunks[index + 1], self._chunk_overlap)[0]
                chunk_text = chunk + " " + overlap
                items.append(Chunk(text=chunk_text))
            else:
                items.append(Chunk(text=chunk))

        return items
