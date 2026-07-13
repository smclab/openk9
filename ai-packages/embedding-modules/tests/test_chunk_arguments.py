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

from typing import Union

from app.utils.chunk_arguments import build_chunk_arguments, coerce_argument

# Signature mirroring chonkie's TokenChunker.__init__ type hints, as reported
# by get_type_hints in server.py (see the issue's possible_arguments log).
TOKEN_CHUNKER_SIGNATURE = {
    "tokenizer": Union[str, object],
    "chunk_size": int,
    "chunk_overlap": Union[int, float],
}


def test_float_int_like_is_coerced_to_int():
    """chunk_size=10000.0 must reach the chunker as int 10000."""
    args = build_chunk_arguments({"chunk_size": 10000.0}, TOKEN_CHUNKER_SIGNATURE)
    assert args["chunk_size"] == 10000
    assert type(args["chunk_size"]) is int


def test_str_int_like_is_coerced_to_int():
    """chunk_size="512" must be converted to int 512."""
    args = build_chunk_arguments({"chunk_size": "512"}, TOKEN_CHUNKER_SIGNATURE)
    assert args["chunk_size"] == 512
    assert type(args["chunk_size"]) is int


def test_native_int_is_unchanged():
    """chunk_size=256 must remain int 256."""
    args = build_chunk_arguments({"chunk_size": 256}, TOKEN_CHUNKER_SIGNATURE)
    assert args["chunk_size"] == 256
    assert type(args["chunk_size"]) is int


def test_union_float_is_preserved():
    """chunk_overlap=0.25 with Union[int, float] must stay float."""
    args = build_chunk_arguments({"chunk_overlap": 0.25}, TOKEN_CHUNKER_SIGNATURE)
    assert args["chunk_overlap"] == 0.25
    assert type(args["chunk_overlap"]) is float


def test_range_step_is_usable_after_coercion():
    """Regression: chunk_size - chunk_overlap must be a valid range() step.

    Reproduces the TypeError raised inside TokenChunker._token_group_generator
    (range step must be int) when chunk_size arrives as a float.
    """
    args = build_chunk_arguments(
        {"chunk_size": 10000.0, "chunk_overlap": 0}, TOKEN_CHUNKER_SIGNATURE
    )
    step = args["chunk_size"] - args["chunk_overlap"]
    # Would raise "TypeError: 'float' object cannot be interpreted as an integer"
    # before the fix.
    assert list(range(0, 3, step)) == [0]


def test_unknown_and_wrong_type_arguments_are_dropped():
    args = build_chunk_arguments(
        {"unknown_key": 1, "chunk_size": "not-a-number"}, TOKEN_CHUNKER_SIGNATURE
    )
    assert args == {}


def test_coerce_argument_rejects_non_int_like():
    accepted, value = coerce_argument("abc", int)
    assert accepted is False
    assert value is None
