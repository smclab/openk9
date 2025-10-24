from textwrap import wrap
from typing import List

from chonkie import TokenChunker
from chonkie.types import Chunk


class CharacterTextChunker(TokenChunker):
    def __init__(self, *args, **kwargs):
        # Blocca il parametro param2
        kwargs["tokenizer"] = "character"
        super().__init__(*args, **kwargs)
