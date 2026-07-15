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

"""Vector quantization performed inside the embedding module.

The module always L2-normalizes before quantizing, so the fixed
symmetric range [-1, +1] is safe for int8 quantization and the sign
threshold is meaningful for the binary arm. Quantizing here (rather than
relying on the provider) keeps a single code path across providers. The
int8 range is hard-coded and there is no centroid: per-tenant
re-centering is out of Phase 1.
"""

import numpy as np

# Vector representation selected per content/query. These mirror the
# embedding.VectorDataType values of the gRPC contract, kept as plain
# ints so this module stays independent of the generated stubs.
VECTOR_DATA_TYPE_FLOAT32 = 0
VECTOR_DATA_TYPE_BYTE = 1
VECTOR_DATA_TYPE_BINARY = 2


def l2_normalize(vector) -> np.ndarray:
    """Returns the L2-normalized copy of the vector as float32.

    Normalization is idempotent, which makes quantization independent
    from whether the provider already normalizes its output. A zero
    vector is returned unchanged (no division by zero).
    """
    array = np.asarray(vector, dtype=np.float32)
    norm = np.linalg.norm(array)

    if norm == 0.0:
        return array

    return array / norm


def quantize_int8(vector, range_: float = 1.0) -> bytes:
    """Scalar int8 quantization over a fixed symmetric range.

    Maps [-range_, +range_] linearly onto [-127, 127], clipping
    outliers. With L2-normalized input and range_=1.0 no component can
    clip. Returns one signed byte per component.
    """
    array = np.asarray(vector, dtype=np.float32)
    scaled = np.clip(np.rint(array / range_ * 127.0), -127, 127)

    return scaled.astype(np.int8).tobytes()


def dequantize_int8(data: bytes, range_: float = 1.0) -> np.ndarray:
    """Inverse of quantize_int8; used to measure the roundtrip error."""
    array = np.frombuffer(data, dtype=np.int8).astype(np.float32)

    return array / 127.0 * range_


def quantize_binary(vector) -> bytes:
    """Binary quantization: one bit per component, packed MSB-first.

    Bits are the sign (> 0) of the components. The dimension must be a
    multiple of 8, as required by OpenSearch binary knn_vector fields.
    """
    array = np.asarray(vector, dtype=np.float32)

    if array.size % 8 != 0:
        raise ValueError(
            f"binary quantization needs a dimension multiple of 8, got {array.size}"
        )

    bits = (array > 0.0).astype(np.uint8)

    return np.packbits(bits).tobytes()


def quantize(vector, vector_data_type: int):
    """L2-normalizes then encodes the vector per the given vectorDataType.

    Returns the payload for the matching contract field:
      FLOAT32 -> list[float]  (FloatVector.values / f32)
      BYTE    -> bytes        (i8, one signed byte per component)
      BINARY  -> bytes        (bits, dimension/8 bytes, MSB first)

    FLOAT32 is not a quantized arm, so it is passed through unchanged
    (matching the v1 float path); BYTE and BINARY are L2-normalized
    before quantization.
    """
    if vector_data_type == VECTOR_DATA_TYPE_FLOAT32:
        return np.asarray(vector, dtype=np.float32).tolist()
    if vector_data_type == VECTOR_DATA_TYPE_BYTE:
        return quantize_int8(l2_normalize(vector))
    if vector_data_type == VECTOR_DATA_TYPE_BINARY:
        return quantize_binary(l2_normalize(vector))

    raise ValueError(f"unknown vectorDataType {vector_data_type}")
