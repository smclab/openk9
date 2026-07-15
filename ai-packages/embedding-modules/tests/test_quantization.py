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
    VECTOR_DATA_TYPE_BINARY,
    VECTOR_DATA_TYPE_BYTE,
    VECTOR_DATA_TYPE_FLOAT32,
    dequantize_int8,
    l2_normalize,
    quantize,
    quantize_binary,
    quantize_int8,
)


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
    rng = np.random.default_rng(42)
    vector = l2_normalize(rng.normal(size=1024))

    restored = dequantize_int8(quantize_int8(vector))

    # half a quantization step over [-1, 1] mapped on 127 levels
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


# --- binary -----------------------------------------------------------


def test_binary_packs_signs_msb_first():
    vector = np.array([0.5, -0.5, 0.5, -0.5, -0.5, -0.5, -0.5, 0.5])

    packed = quantize_binary(vector)

    # signs 1010 0001 -> 0xA1
    assert packed == bytes([0b10100001])


def test_binary_requires_dimension_multiple_of_8():
    with pytest.raises(ValueError):
        quantize_binary(np.ones(10))


# --- dispatcher (selection from vectorDataType) -----------------------


def test_quantize_float32_returns_plain_floats():
    vector = [1.0, 2.0, 3.0, 4.0]

    result = quantize(vector, VECTOR_DATA_TYPE_FLOAT32)

    assert isinstance(result, list)
    assert all(isinstance(component, float) for component in result)
    assert result == pytest.approx(vector)


def test_quantize_byte_normalizes_then_int8():
    vector = [3.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]

    result = quantize(vector, VECTOR_DATA_TYPE_BYTE)

    # one signed byte per component, normalized before quantization
    assert result == quantize_int8(l2_normalize(vector))
    assert len(result) == len(vector)


def test_quantize_binary_normalizes_then_packs():
    vector = [3.0, -4.0, 1.0, -1.0, 2.0, -2.0, 5.0, -5.0]

    result = quantize(vector, VECTOR_DATA_TYPE_BINARY)

    assert result == quantize_binary(l2_normalize(vector))
    assert len(result) == len(vector) // 8


def test_quantize_rejects_unknown_type():
    with pytest.raises(ValueError):
        quantize([0.0] * 8, 99)
