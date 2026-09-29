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

"""The number of chunks of a document is not a limit: a document of 1000
chunks embedded through the Bedrock client, whose provider takes at most
96 texts per request, streams all of its chunks, as inline text and as a
ref alike."""

import io
import json

from app.embedding.bedrock import BedrockMultimodalEmbedder
from app.embedding.router import Pipelines
from app.external_services.grpc.embedding import embedding_pb2

DOCUMENT = " ".join(f"scheda{index}" for index in range(1000))


class _FakeLimitedClient:
    """Rejects more than 96 texts per request, like Bedrock does."""

    def invoke_model(self, modelId, body):
        texts = json.loads(body)["texts"]

        if len(texts) > 96:
            raise ValueError("Invalid parameter combination")

        vectors = [[3.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0] for _ in texts]
        payload = {"embeddings": {"float": vectors}}

        return {"body": io.BytesIO(json.dumps(payload).encode())}


def _bedrock_pipelines(configuration, chunker):
    embedder = BedrockMultimodalEmbedder(
        "cohere.embed-v4:0", region_name="us-east-1", client=_FakeLimitedClient()
    )

    return Pipelines(
        embed_texts=lambda texts: embedder.embed_texts(texts, "search_document"),
        chunk=lambda text: text.split(),
        fetch=lambda url: (DOCUMENT.encode(), "text/markdown"),
        embed_image=embedder.embed_image,
    )


def test_inline_text_of_1000_chunks_is_streamed_whole(make_stub):
    stub = make_stub(_bedrock_pipelines)
    request = embedding_pb2.EmbedContentRequest(tenantId="mew", text=DOCUMENT)

    chunks = list(stub.EmbedContent(request))

    assert [chunk.number for chunk in chunks] == list(range(1, 1001))
    assert all(chunk.total == 1000 for chunk in chunks)
    assert chunks[-1].text == "scheda999"


def test_ref_of_1000_chunks_is_streamed_whole(make_stub):
    stub = make_stub(_bedrock_pipelines)
    request = embedding_pb2.EmbedContentRequest(
        tenantId="mew",
        refs=[
            embedding_pb2.MediaRef(
                url="https://signed/doc-1", fileId="doc-1", contentType="text/markdown"
            )
        ],
    )

    chunks = list(stub.EmbedContent(request))

    assert len(chunks) == 1000
    assert all(chunk.fileId == "doc-1" for chunk in chunks)
    assert [chunk.number for chunk in chunks] == list(range(1, 1001))
