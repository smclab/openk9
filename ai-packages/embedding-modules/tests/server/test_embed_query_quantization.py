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

"""The query vector is quantized with the same vectorDataType as the
index, so it is KNN-comparable: BYTE -> one signed byte per component,
BINARY -> packed sign bits. The fake text vector normalizes to
[0.6, 0, 0, 0, 0.8, 0, 0, 0]."""

import numpy as np

from app.external_services.grpc.embedding import embedding_pb2


def test_byte_query_vector_is_int8(stub):
    request = embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        text="un gatto",
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_BYTE,
    )

    vector = stub.EmbedQuery(request)

    assert vector.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_BYTE
    assert len(vector.i8) == 8
    int8 = np.frombuffer(vector.i8, dtype=np.int8)
    assert int8[0] == round(0.6 * 127)
    assert int8[4] == round(0.8 * 127)


def test_binary_query_vector_packs_sign_bits(stub):
    request = embedding_pb2.EmbedQueryRequest(
        tenantId="mew",
        text="un gatto",
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_BINARY,
    )

    vector = stub.EmbedQuery(request)

    assert vector.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_BINARY
    # dimension 8 -> a single byte, positive components at index 0 and 4
    assert len(vector.bits) == 8 // 8
    assert vector.bits == bytes([0b10001000])
