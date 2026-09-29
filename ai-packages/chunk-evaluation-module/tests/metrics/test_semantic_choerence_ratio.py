#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#


import numpy as np
import pytest
from chonkie.types import Chunk

import metrics.semantic_choerence_ratio as scr

# Deterministic sentence embeddings, keyed by sentence text: they replace the
# Hugging Face model the refinery would otherwise download.
EMBEDDINGS = {
    "Il gatto dorme.": [1.0, 0.0],
    "Il gatto sogna.": [1.0, 0.0],
    "Il cane abbaia.": [0.6, 0.8],
    "Il cane corre.": [0.6, 0.8],
}


class FakeEmbeddingsRefinery:
    def __init__(self, **kwargs):
        pass

    def __call__(self, chunks):
        for chunk in chunks:
            chunk.embedding = np.array(EMBEDDINGS[chunk.text.strip()])
        return chunks


@pytest.fixture(autouse=True)
def fake_embeddings(monkeypatch):
    monkeypatch.setattr(scr, "EmbeddingsRefinery", FakeEmbeddingsRefinery)


def test_inter_sim_calc_is_one_for_identical_embeddings():
    assert scr.inter_sim_calc([[1.0, 0.0]], [[2.0, 0.0]]) == pytest.approx(1.0)


def test_inter_sim_calc_is_zero_for_orthogonal_embeddings():
    assert scr.inter_sim_calc([[1.0, 0.0]], [[0.0, 1.0]]) == pytest.approx(0.0)


def test_inter_sim_calc_is_zero_when_a_side_is_empty():
    assert scr.inter_sim_calc([], [[1.0, 0.0]]) == 0.0


def test_refine_sets_the_mean_similarity_between_the_sentences_of_a_chunk():
    chunks = [
        Chunk(text="Il gatto dorme. Il gatto sogna."),
        Chunk(text="Il gatto dorme. Il cane abbaia."),
        Chunk(text="Il cane corre."),
    ]

    refined = scr.IntraSimilarityRefinery().refine(chunks)

    assert refined[0].intra_similarity == pytest.approx(1.0)
    assert refined[1].intra_similarity == pytest.approx(0.6)
    # A single sentence has no pair to compare.
    assert refined[2].intra_similarity == 0.0
    assert refined[0].sentence_embeddings.shape == (2, 2)


def test_calculate_scr_compares_intra_to_adjacent_similarity():
    chunks = [
        Chunk(text="Il gatto dorme. Il gatto sogna."),
        Chunk(text="Il cane abbaia. Il cane corre."),
    ]

    # Both chunks are internally coherent (1.0) and similar to each other
    # by 0.6, so each ratio is 1 / 0.6.
    result = scr.calculate_scr(chunks)

    assert result["forward"] == pytest.approx(1 / 0.6)
    assert result["backward"] == pytest.approx(1 / 0.6)


def test_calculate_scr_is_zero_for_a_single_chunk():
    chunks = [Chunk(text="Il gatto dorme. Il gatto sogna.")]

    assert scr.calculate_scr(chunks) == {"forward": 0.0, "backward": 0.0}
