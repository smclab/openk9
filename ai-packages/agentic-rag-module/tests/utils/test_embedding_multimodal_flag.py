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

"""The multimodal flag and the tenant reach the embedding module.

The searcher configuration is the only place that knows whether the model is
multimodal. If the flag stopped here, the module would serve a multimodal
model on the text-only path, which is what used to break the chat.
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
    "multimodal": True,
}


def test_the_indexing_path_forwards_the_flag_and_the_tenant():
    with patch.object(embedding, "generate_documents_embeddings") as generate:
        embedding.documents_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration=CONFIGURATION,
            document={"text": "un gatto"},
        )

    _, tenant_id, _, embedding_model, _ = generate.call_args.args

    assert tenant_id == "mew"
    assert embedding_model["multimodal"] is True


def test_the_query_path_forwards_the_flag_and_the_tenant():
    with patch.object(embedding, "generate_query_embedding") as generate:
        embedding.query_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration=CONFIGURATION,
            text="un gatto",
        )

    _, tenant_id, embedding_model, text = generate.call_args.args

    assert tenant_id == "mew"
    assert embedding_model["multimodal"] is True
    assert text == "un gatto"


def test_a_configuration_without_the_flag_stays_text_only():
    configuration = {
        key: value for key, value in CONFIGURATION.items() if key != "multimodal"
    }

    with patch.object(embedding, "generate_query_embedding") as generate:
        embedding.query_embedding(
            grpc_host_embedding="localhost:50053",
            embedding_model_configuration=configuration,
            text="un gatto",
        )

    _, _, embedding_model, _ = generate.call_args.args

    assert embedding_model["multimodal"] is False
