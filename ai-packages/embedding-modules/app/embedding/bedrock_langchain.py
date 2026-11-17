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

"""Bedrock models served by langchain-aws for text and images.

BedrockEmbeddings knows the request of every Bedrock family that embeds
images and picks it from the model id, so a model of those families needs no
client of its own: this module is the only place that knows the langchain
signature. The same BedrockEmbeddings serves the text-only Bedrock models, so
the text of a model is embedded the same way whether it is in the registry or
not, and an index built before keeps its vectors.

No output dimension is passed: the text request of some families rejects it,
and the vectors of an index built without it would change size.

An image the model cannot take, by format or by size, is skipped: the rest of
the document is indexed, as on a text-only model.
"""

from langchain_aws import BedrockEmbeddings

from app.embedding.router import SkipRef


def bedrock_embeddings(configuration):
    """The langchain client of the configured Bedrock model."""
    aws_bedrock = configuration.get("aws_bedrock") or {}

    return BedrockEmbeddings(
        model_id=configuration.get("model"),
        region_name=aws_bedrock.get("region_name"),
    )


def _is_supported_image(data):
    """True for PNG, JPEG, GIF and WebP bytes: langchain reads the format
    from the bytes and sends anything else as JPEG, which the provider
    rejects."""
    return data.startswith((b"\x89PNG", b"\xff\xd8\xff", b"GIF87a", b"GIF89a")) or (
        data[:4] == b"RIFF" and data[8:12] == b"WEBP"
    )


# Cohere Embed v3 on Bedrock takes an image as a data URI of at most 5 MiB:
# the bytes, in base64, behind the data: prefix. A larger image is rejected
# ("Invalid parameter combination"), and with it the whole request.
COHERE_V3_MAX_IMAGE_BYTES = (5 * 1024 * 1024 - 32) * 3 // 4


class LangchainBedrockEmbedder:
    """Text and images of a Bedrock model through BedrockEmbeddings. There is
    no mixed input: a query with text and image together is refused.
    max_image_bytes, when given, is the largest image the model takes."""

    def __init__(self, embeddings, max_image_bytes=None):
        self.embeddings = embeddings
        self.max_image_bytes = max_image_bytes

    def embed_texts(self, texts, input_type="search_document"):
        """One vector per text, in order; queries go through embed_query,
        which some families serve with a task type of their own."""
        if input_type == "search_query":
            return [self.embeddings.embed_query(text) for text in texts]

        return self.embeddings.embed_documents(list(texts))

    def embed_image(self, data, content_type):
        """Embeds a single image (raw bytes); SkipRef for a format other than
        PNG, JPEG, GIF and WebP, or for an image larger than the model
        takes, so the ref is skipped as on a text-only model."""
        if not _is_supported_image(data):
            raise SkipRef(
                f"Bedrock embeds PNG, JPEG, GIF and WebP images only: the bytes "
                f"of this {content_type} are none of them"
            )

        if self.max_image_bytes is not None and len(data) > self.max_image_bytes:
            raise SkipRef(
                f"the image is {len(data)} bytes, more than the "
                f"{self.max_image_bytes} the model takes"
            )

        return self.embeddings.embed_image(data)
