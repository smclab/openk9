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

"""Multimodal embedders and their provider registry.

A multimodal embedder embeds text and images through the SAME model, so
both land in one coherent vector space. The model is selected explicitly
by an opt-in flag in the model jsonConfig; the provider then picks the
concrete embedder from the registry below. Adding a new multimodal model
means registering a factory here — no change to the routing.

Every embedder exposes the same interface, so the router treats them
interchangeably:

    embed_texts(texts, input_type) -> list[list[float]]
    embed_image(data, content_type) -> list[float]
"""

from typing import Protocol, runtime_checkable


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
        project=credentials.get("quota_project_id"),
        dimension=model_garden.get("dimension"),
    )


# provider -> factory(configuration) -> MultimodalEmbedder
MULTIMODAL_EMBEDDERS = {
    "aws_bedrock": _build_bedrock,
    "chat_vertex_ai": _build_vertex,
}


def build_multimodal_embedder(configuration):
    """Selects the multimodal embedder for the configured provider.

    Raises ValueError when the provider has no multimodal embedder
    registered (e.g. a text-only provider marked multimodal by mistake).
    """
    provider = configuration.get("model_type")
    factory = MULTIMODAL_EMBEDDERS.get(provider)

    if factory is None:
        raise ValueError(
            f"no multimodal embedder registered for provider {provider!r}; "
            f"available: {sorted(MULTIMODAL_EMBEDDERS)}"
        )

    return factory(configuration)
