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
