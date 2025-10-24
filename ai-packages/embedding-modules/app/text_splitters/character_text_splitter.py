from chonkie import TokenChunker


class CharacterTextChunker(TokenChunker):
    def __init__(self, *args, **kwargs):
        # Blocca il parametro param2
        kwargs["tokenizer"] = "character"
        super().__init__(*args, **kwargs)
