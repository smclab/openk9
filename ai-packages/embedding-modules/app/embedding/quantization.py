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

The int8 arm L2-normalizes first, so the fixed symmetric range [-1, +1]
is safe; the binary arm keeps only the component sign and needs no
normalization. The int8 range is fixed and there is no centroid
subtraction.
"""

import numpy as np


def l2_normalize(vector) -> np.ndarray:
    """Scales the vector to unit L2 norm, returned as float32.

    Divides every component by the Euclidean norm
    ``||v|| = sqrt(sum(v_i ** 2))``, so the result has length 1 (its
    squares sum to 1).

    Normalization is idempotent, so quantization does not depend on
    whether the provider already normalizes its output.
    A zero vector is returned unchanged.
    """
    array = np.asarray(vector, dtype=np.float32)
    norm = np.linalg.norm(array)

    if norm == 0.0:
        return array

    return array / norm


def quantize_int8(vector) -> bytes:
    """L2-normalizes, then scalar int8 quantization over [-1, +1].

    Maps [-1, +1] linearly onto [-127, 127], one signed byte per
    component.
    """
    normalized = l2_normalize(vector)
    scaled = np.rint(normalized * 127.0)

    return scaled.astype(np.int8).tobytes()


def quantize_binary(vector) -> bytes:
    """Binary quantization: one sign bit per
    component, packed MSB-first.

    The dimension must be a multiple of 8.
    """
    array = np.asarray(vector, dtype=np.float32)

    if array.size % 8 != 0:
        raise ValueError(
            f"binary quantization needs a dimension multiple of 8, got {array.size}"
        )

    bits = (array > 0.0).astype(np.uint8)

    return np.packbits(bits).tobytes()
