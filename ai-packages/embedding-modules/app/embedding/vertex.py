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

"""Vertex AI multimodal embedder.

Text and images go through the same model, into one shared vector space
(the configured dimension). The Vertex multimodal model has no
search_document/search_query asymmetry, so input_type is accepted for
interface parity and ignored. The embedding model and the Image wrapper
are injectable, so the wiring is testable without the Vertex SDK.
"""


class VertexMultimodalEmbedder:
    def __init__(
        self,
        model_id,
        project=None,
        location=None,
        dimension=None,
        client=None,
        image_factory=None,
    ):
        self.dimension = int(dimension) if dimension is not None else None
        self._image_factory = image_factory

        if client is not None:
            self.client = client
        else:
            import vertexai
            from vertexai.vision_models import MultiModalEmbeddingModel

            if project:
                vertexai.init(project=project, location=location or "us-central1")
            self.client = MultiModalEmbeddingModel.from_pretrained(model_id)

    def _wrap_image(self, data):
        if self._image_factory is not None:
            return self._image_factory(data)

        from vertexai.vision_models import Image

        return Image(image_bytes=data)

    def _kwargs(self, **kwargs):
        if self.dimension:
            kwargs["dimension"] = self.dimension

        return kwargs

    def embed_texts(self, texts, input_type=None):
        """Embeds a batch of texts (input_type ignored); one vector each."""
        return [
            list(self.client.get_embeddings(**self._kwargs(contextual_text=text)).text_embedding)
            for text in texts
        ]

    def embed_image(self, data, content_type):
        """Embeds a single image (raw bytes); returns its float vector."""
        result = self.client.get_embeddings(
            **self._kwargs(image=self._wrap_image(data))
        )

        return list(result.image_embedding)
