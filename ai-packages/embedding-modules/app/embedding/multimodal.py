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

"""Multimodal embedders and the registry of the models with a rule of their own.

A multimodal embedder embeds text and images through the SAME model, so
both land in one coherent vector space. Whether a model is multimodal is
decided by the model itself: the registry below lists the models that can
embed images, each with the factory of the embedder that serves it. A model
without a factory, listed or not, goes to langchain and embeds text only.

The registry also carries the batch size EmbedContent embeds the text with,
for the models whose throughput asks for one other than the default.

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
    """A model with a rule of its own: the embedder that serves it, when it
    embeds images (None leaves it to langchain), and the batch size of its
    text in EmbedContent (None leaves the default)."""

    name: str
    provider: str
    matches: Callable[[str], bool]
    factory: Callable | None = None
    batch_size: int | None = None


# The first entry that matches wins, so the specific models come before the
# catch-all of their provider. The direct Bedrock client speaks the Cohere Embed
# v4 schema only, and the Vertex one the multimodalembedding family only. The
# Cohere v4 match is a substring so the inference profile ids
# (eu.cohere.embed-v4:0, ...) match too; 96 texts is its limit per request.
# Ollama serves a long text faster in batches of 512 than of 256. The Vertex
# text models take at most 250 texts per request (vertex_batching): a batch of
# 250 is one request, not a measured optimum.
MODEL_REGISTRY = [
    RegisteredModel(
        "bedrock-cohere-v4",
        "aws_bedrock",
        lambda model: "cohere.embed-v4" in model,
        _build_bedrock,
        batch_size=96,
    ),
    RegisteredModel(
        "vertex-multimodalembedding",
        "chat_vertex_ai",
        lambda model: model.startswith("multimodalembedding"),
        _build_vertex,
    ),
    RegisteredModel("ollama", "ollama", lambda model: True, batch_size=512),
    RegisteredModel(
        "vertex-text", "chat_vertex_ai", lambda model: True, batch_size=250
    ),
]


def find_model(configuration):
    """The registry entry of the configured model, or None for a model with
    no rule of its own."""
    provider = configuration.get("model_type")
    model = configuration.get("model") or ""

    for entry in MODEL_REGISTRY:
        if entry.provider == provider and entry.matches(model):
            return entry

    return None


def find_multimodal_embedder(configuration):
    """The registry entry of the configured model when an embedder of its own
    serves it, or None for a model that embeds text only."""
    entry = find_model(configuration)

    return entry if entry is not None and entry.factory is not None else None
