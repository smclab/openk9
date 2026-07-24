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

"""A text-only request streams one chunk per split, numbered, with the
informative total, no fileId, and an L2-normalized float32 vector."""

import numpy as np
import pytest

from app.external_services.grpc.embedding import embedding_pb2


def test_text_only_streams_numbered_normalized_chunks(stub):
    request = embedding_pb2.EmbedContentRequest(tenantId="mew", text="uno due tre")

    chunks = list(stub.EmbedContent(request))

    assert [chunk.number for chunk in chunks] == [1, 2, 3]
    assert all(chunk.total == 3 for chunk in chunks)
    assert [chunk.text for chunk in chunks] == ["uno", "due", "tre"]
    # text chunks carry no fileId
    assert all(not chunk.HasField("fileId") for chunk in chunks)

    # float32 by default, L2-normalized in the module
    values = chunks[0].f32.values
    assert chunks[0].dimension == 8
    assert np.linalg.norm(values) == pytest.approx(1.0)
