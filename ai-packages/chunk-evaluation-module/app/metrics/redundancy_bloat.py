from chonkie import TokenChunker


def calculate_rb(chunks, doc):
    chunks_tokens = []
    for chunk in chunks:
        chunker = TokenChunker(tokenizer="gpt2", chunk_size=len(chunk.text))
        total_tokens = chunker.chunk(chunk.text)
        chunks_tokens.append(total_tokens[0].token_count)
    chunks_tokens_sum = sum(chunks_tokens)

    chunker = TokenChunker(tokenizer="gpt2", chunk_size=len(doc))
    total_tokens = chunker.chunk(doc)
    total_tokens = [chunk.token_count for chunk in total_tokens][0]

    redundancy = (chunks_tokens_sum / total_tokens) - 1
    return {"redundancy": redundancy}
