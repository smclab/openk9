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

"""The text is embedded batch by batch: each batch is one embed_texts call,
and its Pieces come out in order before the next batch is embedded."""

from app.embedding import router
from app.embedding.router import Pipelines


def _recording_pipelines(calls):
    def embed_texts(texts):
        calls.append(list(texts))
        return [[float(len(text))] for text in texts]

    return Pipelines(embed_texts=embed_texts, chunk=lambda text: text.split())


def test_chunks_are_embedded_in_batches_of_the_given_size():
    calls = []
    pipelines = _recording_pipelines(calls)

    batches = list(
        router.text_piece_batches(
            ["a", "bb", "ccc", "dddd", "eeeee"], pipelines, 2, file_id="f-1"
        )
    )

    assert calls == [["a", "bb"], ["ccc", "dddd"], ["eeeee"]]
    assert [[piece.text for piece in batch] for batch in batches] == calls
    assert [piece.vector for batch in batches for piece in batch] == [
        [1.0],
        [2.0],
        [3.0],
        [4.0],
        [5.0],
    ]
    assert all(piece.file_id == "f-1" for batch in batches for piece in batch)


def test_a_batch_is_embedded_only_when_the_previous_one_is_consumed():
    calls = []
    batches = router.text_piece_batches(["a", "b", "c"], _recording_pipelines(calls), 2)

    next(batches)

    assert calls == [["a", "b"]]


def test_no_chunks_no_calls():
    calls = []

    assert list(router.text_piece_batches([], _recording_pipelines(calls), 2)) == []
    assert calls == []
