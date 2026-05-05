from chonkie import TokenChunker

EPS = 1e-8


def calculate_rb(chunks, doc):
    chunker = TokenChunker(tokenizer="gpt2")

    def count_tokens(text):
        chunks_out = chunker.chunk(text)
        if not chunks_out:
            return 0
        return chunks_out[0].token_count

    chunks_tokens_sum = sum(count_tokens(chunk.text) for chunk in chunks)
    total_tokens = count_tokens(doc)

    if total_tokens == 0:
        return {"redundancy": 0.0}

    redundancy = (chunks_tokens_sum / (total_tokens + EPS)) - 1

    return {"redundancy": redundancy}
