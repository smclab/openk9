from chonkie import TokenChunker


def calculate_rb(chunks, doc):

    def count_tokens(text):
        chunker = TokenChunker(tokenizer="gpt2", chunk_size=len(text))
        return chunker.chunk(text)[0].token_count

    chunks_tokens_sum = sum(count_tokens(chunk.text) for chunk in chunks)

    total_tokens = count_tokens(doc)

    redundancy = (chunks_tokens_sum / total_tokens) - 1

    return {"redundancy": redundancy}
