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

"""The tenant and the embedding model reach the embedding module.

Both the indexing and the query path forward the tenant of the searcher
configuration. The embedding model carries no multimodal flag: the embedding
module tells a multimodal model from its provider and model.
"""

from unittest.mock import patch

from app.utils import embedding

CONFIGURATION = {
    "tenant_id": "mew",
    "api_url": "http://embedding.local",
    "api_key": "secret",
    "model_type": "aws_bedrock",
    "model": "cohere.embed-v4:0",
    "vector_size": 1024,
    "json_config": {},
}


def test_the_indexing_path_forwards_the_tenant():
    with patch.object(embedding, "generate_documents_embeddings") as generate:
        embedding.documents_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration=CONFIGURATION,
            document={"text": "un gatto"},
        )

    _, tenant_id, _, embedding_model, _ = generate.call_args.args

    assert tenant_id == "mew"
    assert "multimodal" not in embedding_model


def test_the_query_path_forwards_the_tenant():
    with patch.object(embedding, "generate_query_embedding") as generate:
        embedding.query_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration=CONFIGURATION,
            text="un gatto",
        )

    host, tenant_id, embedding_model, text = generate.call_args.args

    assert host == "localhost:50053"
    assert tenant_id == "mew"
    assert "multimodal" not in embedding_model
    assert text == "un gatto"

