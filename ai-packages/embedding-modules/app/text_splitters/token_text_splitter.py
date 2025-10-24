from chonkie import TokenChunker


class TokenTextChunker(TokenChunker):
    def __init__(self, *args, **kwargs):
        # Blocca il parametro param2
        kwargs["tokenizer"] = "gpt2"
        super().__init__(*args, **kwargs)
