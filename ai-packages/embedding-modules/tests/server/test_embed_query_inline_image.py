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

"""An inline image query returns a single vector in the same space as the
indexed documents (no chunking, no storage)."""

import numpy as np
import pytest

from app.external_services.grpc.embedding import embedding_pb2


def test_inline_image_query_returns_single_vector(stub):
    request = embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        inline=embedding_pb2.InlineMedia(data=b"png-bytes", contentType="image/png"),
    )

    vector = stub.EmbedQuery(request)

    assert vector.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_FLOAT32
    assert vector.dimension == 8
    # fake image vector [0,1,0,...] is already unit-norm
    assert np.linalg.norm(vector.f32.values) == pytest.approx(1.0)
    assert vector.f32.values[1] == pytest.approx(1.0)
