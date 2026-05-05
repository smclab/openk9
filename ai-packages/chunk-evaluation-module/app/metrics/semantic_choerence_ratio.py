from typing import List

import numpy as np
from chonkie import BaseRefinery, EmbeddingsRefinery, SentenceChunker
from chonkie.types import Chunk

EPS = 1e-8


class IntraSimilarityRefinery(BaseRefinery):
    def __init__(self):
        self.em_refinery = EmbeddingsRefinery(
            embedding_model="minishlab/potion-base-32M",
        )
        self.sentence_splitter = SentenceChunker(
            chunk_size=1,
            min_sentences_per_chunk=1,
            delim=[".", "!", "?", "\n"],
        )

    def refine(self, chunks: List[Chunk]) -> List[Chunk]:
        all_sentences = []
        chunk_map = []

        for idx, chunk in enumerate(chunks):
            sentences = self.sentence_splitter.chunk(chunk.text)
            for s in sentences:
                all_sentences.append(s)
                chunk_map.append(idx)

        all_embeddings = self.em_refinery(all_sentences)

        chunk_embeddings = [[] for _ in chunks]

        for emb, idx in zip(all_embeddings, chunk_map):
            chunk_embeddings[idx].append(emb.embedding)

        for i, chunk in enumerate(chunks):
            embs = np.array(chunk_embeddings[i])

            if len(embs) <= 1:
                intra = 0.0
            else:
                norms = np.linalg.norm(embs, axis=1, keepdims=True) + EPS
                embs_norm = embs / norms

                sim_matrix = embs_norm @ embs_norm.T

                n = sim_matrix.shape[0]
                mask = ~np.eye(n, dtype=bool)
                values = sim_matrix[mask]

                intra = float(values.mean()) if values.size > 0 else 0.0

            chunk.intra_similarity = intra
            chunk.sentence_embeddings = embs

        return chunks


def inter_sim_calc(A, B):
    if len(A) == 0 or len(B) == 0:
        return 0.0

    A = np.array(A)
    B = np.array(B)

    A = A / (np.linalg.norm(A, axis=1, keepdims=True) + EPS)
    B = B / (np.linalg.norm(B, axis=1, keepdims=True) + EPS)

    return float((A @ B.T).mean())


def calculate_scr(chunks):
    refinery = IntraSimilarityRefinery()
    refined_chunks = refinery.refine(chunks)

    intra_sim = np.array([c.intra_similarity for c in refined_chunks])
    sent_embs = [c.sentence_embeddings for c in refined_chunks]

    if len(sent_embs) < 2:
        return {"forward": 0.0, "backward": 0.0}

    adjacent_sim = np.array(
        [
            inter_sim_calc(sent_embs[i], sent_embs[i + 1])
            for i in range(len(sent_embs) - 1)
        ]
    )

    forward = intra_sim[:-1] / (adjacent_sim + EPS)
    backward = intra_sim[1:] / (adjacent_sim + EPS)

    return {
        "forward": float(forward.mean()),
        "backward": float(backward.mean()),
    }
