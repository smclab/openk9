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

import base64
import json


class BedrockMultimodalEmbedder:
    """Direct Bedrock client for a multimodal embedding model.

    The langchain Bedrock wrapper is text-only; images need the raw
    InvokeModel API. Text and images share one vector space: both go
    through the same model with the same output dimension. A boto3 client
    can be injected for tests.
    """

    def __init__(self, model_id, region_name, dimension=None, client=None):
        self.model_id = model_id
        # jsonConfig reaches the module as a gRPC Struct, whose numbers
        # decode to float; the provider can reject a float output_dimension
        # ("Malformed request"), so coerce it to int.
        self.dimension = int(dimension) if dimension is not None else None

        if client is not None:
            self.client = client
        else:
            import boto3

            self.client = boto3.client("bedrock-runtime", region_name=region_name)

    def _invoke(self, body):
        if self.dimension:
            body["output_dimension"] = self.dimension

        response = self.client.invoke_model(
            modelId=self.model_id, body=json.dumps(body)
        )
        payload = json.loads(response["body"].read())

        return payload["embeddings"]["float"]

    def embed_texts(self, texts, input_type="search_document"):
        """Embeds a batch of texts; returns one float vector per text."""
        return self._invoke(
            {
                "texts": list(texts),
                "input_type": input_type,
                "embedding_types": ["float"],
            }
        )

    def embed_image(self, data, content_type):
        """Embeds a single image (raw bytes); returns its float vector."""
        data_uri = (
            f"data:{content_type};base64,{base64.b64encode(data).decode('ascii')}"
        )
        vectors = self._invoke(
            {
                "images": [data_uri],
                "input_type": "image",
                "embedding_types": ["float"],
            }
        )

        return vectors[0]

    def embed_mixed(self, text, data, content_type, input_type="search_query"):
        """Embeds text and an image together as one query, returning a
        single fused vector. Cohere Embed v4 takes both in one input via a
        content array and the model produces one vector for the pair (no
        module-side fusion). The exact wire shape is validated by the
        query smoke test.
        """
        data_uri = (
            f"data:{content_type};base64,{base64.b64encode(data).decode('ascii')}"
        )
        vectors = self._invoke(
            {
                "inputs": [
                    {
                        "content": [
                            {"type": "text", "text": text},
                            {"type": "image_url", "image_url": {"url": data_uri}},
                        ]
                    }
                ],
                "input_type": input_type,
                "embedding_types": ["float"],
            }
        )

        return vectors[0]
