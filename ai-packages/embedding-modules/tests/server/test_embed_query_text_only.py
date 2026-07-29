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

"""A text query returns a single L2-normalized float32 EmbeddedVector."""

import numpy as np
import pytest

from app.external_services.grpc.embedding import embedding_pb2


def test_text_query_returns_single_normalized_vector(stub):
    request = embedding_pb2.EmbedQueryRequest(tenantId="mew", text="un gatto")

    vector = stub.EmbedQuery(request)

    assert vector.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_FLOAT32
    assert vector.dimension == 8
    # fake text vector [3,0,0,0,4,0,0,0] -> [0.6,0,0,0,0.8,0,0,0]
    assert np.linalg.norm(vector.f32.values) == pytest.approx(1.0)
    assert vector.f32.values[0] == pytest.approx(0.6)
    assert vector.f32.values[4] == pytest.approx(0.8)
