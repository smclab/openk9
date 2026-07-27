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

"""The requested vectorDataType drives the quantization of the emitted
chunks: BYTE -> one signed byte per component, BINARY -> packed sign bits.
The fake text vector normalizes to [0.6, 0, 0, 0, 0.8, 0, 0, 0]."""

import numpy as np

from app.external_services.grpc.embedding import embedding_pb2


def test_byte_quantization_yields_one_int8_per_component(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        text="unico",
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_BYTE,
    )

    chunk = list(stub.EmbedContent(request))[0]

    assert chunk.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_BYTE
    assert len(chunk.i8) == 8

    int8 = np.frombuffer(chunk.i8, dtype=np.int8)
    assert int8[0] == round(0.6 * 127)
    assert int8[4] == round(0.8 * 127)


def test_binary_quantization_packs_sign_bits(stub):
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        text="unico",
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_BINARY,
    )

    chunk = list(stub.EmbedContent(request))[0]

    assert chunk.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_BINARY
    assert len(chunk.bits) == 8 // 8
    # positive components at index 0 and 4 -> 1000 1000
    assert chunk.bits == bytes([0b10001000])


def test_byte_quantization_applies_to_ref_chunks(stub):
    # quantization must also run on chunks coming from a ref, not just text
    refs = [
        embedding_pb2.MediaRef(
            url="https://signed/img-1", fileId="img-1", contentType="image/png"
        )
    ]
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=refs,
        vectorDataType=embedding_pb2.VECTOR_DATA_TYPE_BYTE,
    )

    chunk = list(stub.EmbedContent(request))[0]

    assert chunk.fileId == "img-1"
    assert chunk.vectorDataType == embedding_pb2.VECTOR_DATA_TYPE_BYTE
    assert len(chunk.i8) == 8
    # the fake image vector [0, 1, 0, ...] is already unit-norm -> 1.0 -> 127
    int8 = np.frombuffer(chunk.i8, dtype=np.int8)
    assert int8[1] == 127
