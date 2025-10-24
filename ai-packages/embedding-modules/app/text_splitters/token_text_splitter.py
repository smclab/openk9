from textwrap import wrap
from typing import List

from chonkie import TokenChunker
from chonkie.types import Chunk


class TokenTextChunker(TokenChunker):
    def __init__(self, *args, **kwargs):
        # Blocca il parametro param2
        kwargs["tokenizer"] = "gpt2"
        super().__init__(*args, **kwargs)
