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

"""Every RPC decodes the EmbeddingModel wire message into the same
configuration dict: each field lands under its own key, the jsonConfig
entries are read by name, and the dict is what the model builders get."""

import pytest

from app import server as server_module
from app.embedding.query import QueryCapabilities
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

MODEL_GARDEN = {
    "credentials": {"quota_project_id": "project-id"},
    "location": "europe-west1",
}


def _complete_model():
    """An EmbeddingModel with every field set, each to a distinct value, so a
    field read into the wrong key is recognizable."""
    model = embedding_pb2.EmbeddingModel(
        apiKey="secret",
        apiUrl="http://endpoint",
        providerModel=embedding_pb2.ProviderModel(
            provider="aws_bedrock", model="cohere.embed-v4"
        ),
        multimodal=True,
    )
    model.jsonConfig.update(
        {
            "watsonx_project_id": "watsonx-project",
            "chat_vertex_ai_model_garden": MODEL_GARDEN,
            "aws_bedrock": {"region_name": "eu-west-1"},
            "keep_alive": 60,
            "num_gpu": 0,
            "unrelated": "ignored",
        }
    )

    return model


# a gRPC Struct decodes every number to float
COMPLETE_CONFIGURATION = {
    "api_key": "secret",
    "api_url": "http://endpoint",
    "model_type": "aws_bedrock",
    "model": "cohere.embed-v4",
    "watsonx_project_id": "watsonx-project",
    "chat_vertex_ai_model_garden": MODEL_GARDEN,
    "aws_bedrock": {"region_name": "eu-west-1"},
    "keep_alive": 60.0,
    "num_gpu": 0.0,
    "multimodal": True,
}


def test_a_complete_embedding_model_maps_every_field():
    assert server_module._build_configuration(_complete_model()) == (
        COMPLETE_CONFIGURATION
    )


def test_an_empty_embedding_model_maps_to_the_proto_defaults():
    assert server_module._build_configuration(embedding_pb2.EmbeddingModel()) == {
        "api_key": "",
        "api_url": "",
        "model_type": "",
        "model": "",
        "watsonx_project_id": None,
        "chat_vertex_ai_model_garden": None,
        "aws_bedrock": None,
        "keep_alive": None,
        "num_gpu": None,
        "multimodal": False,
    }


def _embed_content(stub):
    list(
        stub.EmbedContent(
            embedding_pb2.EmbedContentRequest(
                tenantId="mew", embeddingModel=_complete_model(), text="uno"
            )
        )
    )


def _embed_query(stub):
    stub.EmbedQuery(
        embedding_pb2.EmbedQueryRequest(
            tenantId="mew", embeddingModel=_complete_model(), text="un gatto"
        )
    )


def _get_messages(stub):
    stub.GetMessages(
        embedding_pb2.EmbeddingRequest(
            chunk=embedding_pb2.RequestChunk(type=1),
            embeddingModel=_complete_model(),
            text="pikachu",
        )
    )


@pytest.mark.parametrize(
    "call",
    [
        pytest.param(_embed_content, id="EmbedContent"),
        pytest.param(_embed_query, id="EmbedQuery"),
        pytest.param(_get_messages, id="GetMessages"),
    ],
)
def test_every_rpc_hands_the_decoded_configuration_to_its_builder(
    make_stub, monkeypatch, call
):
    seen = []

    def build_pipelines(configuration, chunker):
        seen.append(configuration)
        return Pipelines(
            embed_texts=lambda texts: [[1.0] for _ in texts],
            chunk=lambda text: text.split(),
        )

    def build_query_capabilities(configuration):
        seen.append(configuration)
        return QueryCapabilities(embed_text=lambda text: [1.0])

    def build_text_embed_texts(configuration):
        seen.append(configuration)
        return lambda texts: [[1.0] for _ in texts]

    monkeypatch.setattr(server_module, "build_text_embed_texts", build_text_embed_texts)
    stub = make_stub(build_pipelines, build_query_capabilities)

    call(stub)

    assert seen == [COMPLETE_CONFIGURATION]
