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

"""Vertex AI request batches: at most 250 texts and 20k estimated tokens
each, in the original order."""

from app.embedding import vertex_batching


def test_texts_over_the_count_limit_split_into_batches_of_250():
    texts = [f"t{i}" for i in range(600)]

    batches = list(vertex_batching.batches(texts))

    assert [len(batch) for batch in batches] == [250, 250, 100]
    assert [text for batch in batches for text in batch] == texts


def test_long_texts_split_on_the_token_limit():
    # 1000 words -> 2000 segments (words and spaces) -> ~4000 tokens
    text = "parola " * 1000
    texts = [text] * 12

    batches = list(vertex_batching.batches(texts))

    assert [len(batch) for batch in batches] == [5, 5, 2]
    assert all(
        sum(vertex_batching.estimated_tokens(t) for t in batch)
        <= vertex_batching.MAX_TOKENS_PER_REQUEST
        for batch in batches
    )


def test_a_text_over_the_token_limit_gets_a_batch_of_its_own():
    huge = "parola " * 20000

    batches = list(vertex_batching.batches(["a", huge, "b"]))

    assert batches == [["a"], [huge], ["b"]]


def test_no_texts_no_batches():
    assert list(vertex_batching.batches([])) == []
