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

"""Vector quantization primitives for the embedding module.

Each quantizer L2-normalizes its input first, so the fixed symmetric
range [-1, +1] is safe for int8 quantization and the sign threshold is
meaningful for the binary arm. Quantizing here (rather than relying on
the provider) keeps a single code path across providers. The int8 range
is fixed and there is no centroid: per-tenant re-centering is out of
Phase 1.

FLOAT32 is not a quantized arm: the caller passes the raw float vector
through unchanged. Selecting the arm from the contract's vectorDataType
is done by the RPC wiring (C2/C3), which switches on the generated
embedding.VectorDataType enum directly.
"""

import numpy as np


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


def quantize_int8(vector) -> bytes:
    """L2-normalizes, then scalar int8 quantization over [-1, +1].

    Maps [-1, +1] linearly onto [-127, 127]; with L2-normalized input no
    component can clip. Returns one signed byte per component.
    """
    normalized = l2_normalize(vector)
    scaled = np.clip(np.rint(normalized * 127.0), -127, 127)

    return scaled.astype(np.int8).tobytes()


def quantize_binary(vector) -> bytes:
    """L2-normalizes, then binary quantization: one sign bit per
    component, packed MSB-first.

    Normalization is applied for uniformity with the int8 arm; the packed
    output depends only on the component signs, which normalization
    preserves. The dimension must be a multiple of 8, as required by
    OpenSearch binary knn_vector fields.
    """
    array = np.asarray(vector, dtype=np.float32)

    if array.size % 8 != 0:
        raise ValueError(
            f"binary quantization needs a dimension multiple of 8, got {array.size}"
        )

    bits = (l2_normalize(array) > 0.0).astype(np.uint8)

    return np.packbits(bits).tobytes()
