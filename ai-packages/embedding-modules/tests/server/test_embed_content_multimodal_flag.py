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

"""The multimodal flag is read from the EmbeddingModel.multimodal proto
field (not from jsonConfig) and forwarded into the pipeline config."""

from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2


def _capturing_pipelines(seen):
    def build_pipelines(configuration, chunker, is_query=False):
        seen["multimodal"] = configuration["multimodal"]
        return Pipelines(
            embed_texts=lambda texts: [[1.0, 0, 0, 0, 0, 0, 0, 0] for _ in texts],
            chunk=lambda text: text.split(),
            fetch=lambda url: (b"", None),
            embed_image=None,
        )

    return build_pipelines


def test_multimodal_true_from_proto_field(make_stub):
    seen = {}
    stub = make_stub(_capturing_pipelines(seen))
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        embeddingModel=embedding_pb2.EmbeddingModel(multimodal=True),
        text="uno",
    )

    list(stub.EmbedContent(request))

    assert seen["multimodal"] is True


def test_multimodal_defaults_to_false(make_stub):
    seen = {}
    stub = make_stub(_capturing_pipelines(seen))
    request = embedding_pb2.EmbedContentRequest(tenantId="mew", text="uno")

    list(stub.EmbedContent(request))

    assert seen["multimodal"] is False
