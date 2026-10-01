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

"""Multimodal embedders and the registry of the models they serve.

A multimodal embedder embeds text and images through the SAME model, so
both land in one coherent vector space. Whether a model is multimodal is
decided by the model itself: the registry below lists the models that can
embed images, each with the factory of the embedder that serves it. A model
that is not listed goes to langchain and embeds text only.

A listed model is served by its embedder for every modality (text, images,
mixed query), so its index and its queries stay in one vector space. A model
enters the registry, or leaves it for langchain, with a measure on that model.

Every embedder exposes the same interface, so the router treats them
interchangeably:

    embed_texts(texts, input_type) -> list[list[float]]
    embed_image(data, content_type) -> list[float]
    embed_mixed(text, data, content_type) -> list[float]   (optional)
"""

from typing import Callable, NamedTuple, Protocol, runtime_checkable


@runtime_checkable
class MultimodalEmbedder(Protocol):
    def embed_texts(self, texts, input_type=None): ...

    def embed_image(self, data, content_type): ...


def _build_bedrock(configuration):
    from app.embedding.bedrock import BedrockMultimodalEmbedder

    aws_bedrock = configuration.get("aws_bedrock") or {}

    return BedrockMultimodalEmbedder(
        model_id=configuration.get("model"),
        region_name=aws_bedrock.get("region_name"),
        dimension=aws_bedrock.get("output_dimension"),
    )


def _build_vertex(configuration):
    from app.embedding.vertex import VertexMultimodalEmbedder

    model_garden = configuration.get("chat_vertex_ai_model_garden") or {}
    credentials = model_garden.get("credentials") or {}

    return VertexMultimodalEmbedder(
        model_id=configuration.get("model"),
        project=model_garden.get("project") or credentials.get("quota_project_id"),
        location=model_garden.get("location"),
        dimension=model_garden.get("dimension"),
    )


class RegisteredModel(NamedTuple):
    """A model that embeds images, and the embedder that serves it."""

    name: str
    provider: str
    matches: Callable[[str], bool]
    factory: Callable


# The direct Bedrock client speaks the Cohere Embed v4 schema only, and the
# Vertex one the multimodalembedding family only. The Cohere v4 match is a
# substring so the inference profile ids (eu.cohere.embed-v4:0, ...) match too.
MULTIMODAL_EMBEDDERS = [
    RegisteredModel(
        "bedrock-cohere-v4",
        "aws_bedrock",
        lambda model: "cohere.embed-v4" in model,
        _build_bedrock,
    ),
    RegisteredModel(
        "vertex-multimodalembedding",
        "chat_vertex_ai",
        lambda model: model.startswith("multimodalembedding"),
        _build_vertex,
    ),
]


def find_multimodal_embedder(configuration):
    """The registry entry of the configured model, or None for a model that
    embeds text only."""
    provider = configuration.get("model_type")
    model = configuration.get("model") or ""

    for entry in MULTIMODAL_EMBEDDERS:
        if entry.provider == provider and entry.matches(model):
            return entry

    return None
