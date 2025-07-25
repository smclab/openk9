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

from textwrap import wrap
from typing import List

from langchain_text_splitters import TextSplitter


class DerivedTextSplitter(TextSplitter):
    def split_text_cutting_words(self, text: str) -> List[str]:
        """Split incoming text and return chunks cutting words."""
        text_len = len(text)
        chunks = []

        for i in range(0, text_len, self._chunk_size):
            chunks.append(text[i : i + self._chunk_size + self._chunk_overlap])

        return chunks

    def split_text(self, text: str) -> List[str]:
        """Split incoming text and return chunks without cutting words."""
        chunks = wrap(text, self._chunk_size)

        if self._chunk_overlap > 0:
            items = []
            chunks_number = len(chunks)

            for index, chunk in enumerate(chunks, start=0):
                if index < chunks_number - 1:
                    item = chunk + " " + wrap(chunks[index + 1], self._chunk_overlap)[0]
                    items.append(item)
            items.append(chunks[chunks_number - 1])

            return items
        else:
            return chunks
