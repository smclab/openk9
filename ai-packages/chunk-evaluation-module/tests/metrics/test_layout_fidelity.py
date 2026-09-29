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


from chonkie.types import Chunk

from metrics.layout_fidelity import calculate_lf, matching_percentage

TABLE = "| a | b |\n|---|---|\n| 1 | 2 |\n"
DOC = f"# Titolo\n\nTesto introduttivo.\n\n{TABLE}\nTesto finale.\n"


def test_matching_percentage_counts_the_whole_span_when_contained():
    assert matching_percentage(["x", "a", "b", "c"], ["a", "b", "c"]) == 3


def test_matching_percentage_counts_partial_overlap():
    assert matching_percentage(["x", "a", "b"], ["a", "b", "c"]) == 2


def test_matching_percentage_is_zero_for_empty_inputs():
    assert matching_percentage([], ["a"]) == 0
    assert matching_percentage(["a"], []) == 0


def test_calculate_lf_is_full_when_a_chunk_holds_the_whole_table():
    chunks = [Chunk(text="Testo introduttivo."), Chunk(text=TABLE)]

    assert calculate_lf(chunks, DOC) == {"coverage": 1.0}


def test_calculate_lf_is_zero_when_the_table_is_split_across_chunks():
    chunks = [Chunk(text="| a | b |\n|---|---|"), Chunk(text="| 1 | 2 |")]

    assert calculate_lf(chunks, DOC) == {"coverage": 0.0}


def test_calculate_lf_is_zero_for_a_document_without_tables():
    chunks = [Chunk(text="Solo testo.")]

    assert calculate_lf(chunks, "Solo testo.") == {"coverage": 0.0}
