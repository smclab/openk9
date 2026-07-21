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

from app.embedding.quantization import (
    l2_normalize,
    quantize_binary,
    quantize_int8,
)


def dequantize_int8(data: bytes) -> np.ndarray:
    """Decode int8 bytes back to floats in [-1, 1]; test-only.

    Reverses quantize_int8 (divide by 127) so a test can compare the
    decoded vector against the original and measure how much precision
    the int8 encoding lost (the "roundtrip error").
    """
    array = np.frombuffer(data, dtype=np.int8).astype(np.float32)

    return array / 127.0


# --- L2 normalization -------------------------------------------------


def test_l2_normalization_is_idempotent():
    vector = np.array([3.0, 4.0, 0.0, 0.0], dtype=np.float32)

    once = l2_normalize(vector)
    twice = l2_normalize(once)

    assert np.linalg.norm(once) == pytest.approx(1.0)
    np.testing.assert_allclose(once, twice, rtol=1e-6)


def test_l2_normalization_of_zero_vector_is_safe():
    vector = np.zeros(8, dtype=np.float32)

    np.testing.assert_array_equal(l2_normalize(vector), vector)


# --- int8 -------------------------------------------------------------


def test_int8_roundtrip_error_is_bounded():
    # int8 quantization is lossy: each component of a unit vector (a float
    # in [-1, 1]) is rounded to one of 255 integer levels (-127..127), so
    # precision is thrown away. "Roundtrip" = encode then decode back to
    # float; the roundtrip error is how far the decoded value drifted from
    # the original. This asserts the loss is no larger than it must be, so
    # a broken scale factor, missing rounding, or wrong dtype would fail.
    rng = np.random.default_rng(42)
    vector = l2_normalize(rng.normal(size=1024))

    restored = dequantize_int8(quantize_int8(vector))

    # quantize rounds c*127 to the nearest integer (error <= 0.5); dividing
    # back by 127 bounds the per-component error at 0.5/127. +1e-6 is slack.
    assert np.max(np.abs(restored - vector)) <= 0.5 / 127.0 + 1e-6


def test_int8_preserves_nearest_neighbour_ranking():
    # recall sanity on synthetic vectors: the top-1 neighbour by float
    # cosine must survive int8 quantization almost always
    rng = np.random.default_rng(7)
    documents = np.stack([l2_normalize(v) for v in rng.normal(size=(500, 256))])
    queries = np.stack([l2_normalize(v) for v in rng.normal(size=(50, 256))])

    quantized = np.stack(
        [
            np.frombuffer(quantize_int8(document), dtype=np.int8).astype(np.float32)
            for document in documents
        ]
    )

    hits = 0
    for query in queries:
        top_float = np.argmax(documents @ query)
        top_int8 = np.argmax(quantized @ query)
        hits += int(top_float == top_int8)

    assert hits / len(queries) >= 0.9


def test_int8_normalizes_input_internally():
    unit = np.array([0.6, 0.8, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0], dtype=np.float32)

    # halving the magnitude must not change the output: quantize_int8
    # L2-normalizes internally, collapsing both to the same unit vector
    assert quantize_int8(unit) == quantize_int8(unit * 0.5)


def test_int8_maps_unit_component_to_symmetric_max():
    # a one-hot vector is already unit-norm, so its single non-zero
    # component is exactly +1 (or -1) -- the largest magnitude any
    # component of a unit vector can reach. It must map to the extreme
    # byte +127 (or -127): this pins the scale factor (1.0 -> 127) and
    # confirms both ends of the symmetric range are representable.
    one_hot = np.zeros(8, dtype=np.float32)
    one_hot[0] = 1.0

    positive = np.frombuffer(quantize_int8(one_hot), dtype=np.int8)
    negative = np.frombuffer(quantize_int8(-one_hot), dtype=np.int8)

    assert positive[0] == 127
    assert negative[0] == -127


@pytest.mark.parametrize(
    "vector",
    [
        [1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],        # one-hot, max component
        [1e30, -1e30, 1e30, -1e30, 0.0, 0.0, 0.0, 0.0],  # huge magnitudes
        [1e-30, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],      # near-degenerate norm
        [1.0, 1e-7, -1e-7, 0.0, 0.0, 0.0, 0.0, 0.0],     # single dominant axis
    ],
)
def test_int8_output_stays_in_symmetric_range(vector):
    # quantize_int8 relies on L2-normalization to keep every component in
    # [-1, 1], so rint(c * 127) always lands in [-127, 127]. This is the
    # tripwire for that invariant: whatever the input magnitude, the
    # encoded bytes must never hit -128 (int8's extra negative value, which
    # the symmetric mapping deliberately never uses). If a change ever
    # weakened normalization, an out-of-range component would surface here.
    decoded = np.frombuffer(quantize_int8(vector), dtype=np.int8)

    assert decoded.min() >= -127  # -128 would mean the invariant broke


# --- binary -----------------------------------------------------------


def test_binary_packs_signs_msb_first():
    vector = np.array([0.5, -0.5, 0.5, -0.5, -0.5, -0.5, -0.5, 0.5])

    packed = quantize_binary(vector)

    # signs 1010 0001 -> 0xA1
    assert packed == bytes([0b10100001])


def test_binary_requires_dimension_multiple_of_8():
    with pytest.raises(ValueError):
        quantize_binary(np.ones(10))
