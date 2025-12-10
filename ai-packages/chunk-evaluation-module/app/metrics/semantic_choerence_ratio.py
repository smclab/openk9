from typing import List

import numpy as np
from chonkie import (
    BaseRefinery,
    EmbeddingsRefinery,
    SentenceChunker,
)
from chonkie.types import Chunk
from sklearn.metrics.pairwise import cosine_similarity


class IntraSimilarityRefinery(BaseRefinery):
    def __init__(self, embedding_model="", sentence_splitter=""):
        pass

    def refine(self, chunks: List[Chunk]) -> List[Chunk]:
        em_refinery = EmbeddingsRefinery(
            embedding_model="minishlab/potion-base-32M",  # Required
        )
        sentence_splitter = SentenceChunker(
            chunk_size=1,  # Dimensione molto piccola
            min_sentences_per_chunk=1,  # Esattamente 1 frase per chunk
            delim=[".", "!", "?", "\n"],  # Delimitatori di frase
        )
        refined_chunks = []
        for chunk in chunks:
            sentences = sentence_splitter.chunk(chunk.text)
            sentences_ref = em_refinery(sentences)
            intra_similarity = self._intra_sim_calc(sentences_ref)
            chunk.intra_similarity = intra_similarity
            chunk.sentence_embeddings = [embs.embedding for embs in sentences_ref]
            refined_chunks.append(chunk)
        return refined_chunks

    def _intra_sim_calc(self, chunk_sentences):
        embeddings = [chunk.embedding for chunk in chunk_sentences]
        sim_matrix = cosine_similarity(embeddings)
        n = sim_matrix.shape[0]
        mask = ~np.eye(n, dtype=bool)
        intragroup_similarity = sim_matrix[mask].mean()
        return intragroup_similarity


def inter_sim_calc(chunk_A, chunk_B):
    sim_matrix = cosine_similarity(chunk_A, chunk_B)
    return np.mean(sim_matrix)


def calculate_scr(chunks):
    intra_refinery = IntraSimilarityRefinery()
    refined_chunks = intra_refinery.refine(chunks)

    intra_sim = [float(aaa.intra_similarity) for aaa in refined_chunks]
    sent_embs = [aaa.sentence_embeddings for aaa in refined_chunks]

    adjacent_similarities = []
    for i in range(len(sent_embs) - 1):
        sim = inter_sim_calc(sent_embs[i], sent_embs[i + 1])
        adjacent_similarities.append(float(sim))

    forward = [
        intra_sim[i] / (adjacent_similarities[i] + 0.0000001)
        for i in range(len(intra_sim) - 1)
    ]

    backward = [
        intra_sim[i + 1] / (adjacent_similarities[i] + 0.0000001)
        for i in range(len(intra_sim) - 1)
    ]

    return {"forward": np.mean(forward), "backward": np.mean(backward)}
