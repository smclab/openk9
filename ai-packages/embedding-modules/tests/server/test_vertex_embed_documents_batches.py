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

"""The Vertex AI text embeddings send a document's chunks in batches the
provider accepts, instead of one request langchain-google-vertexai 3.x
would send with all of them."""

import pytest
from langchain_google_vertexai import VertexAIEmbeddings

from app import server as server_module
from app.embedding import vertex_batching


@pytest.fixture
def requests(monkeypatch):
    """Replaces the langchain request with a fake that rejects what Vertex
    rejects, and records every batch it is sent."""
    sent = []

    def fake_embed_documents(self, texts, **kwargs):
        if len(texts) > vertex_batching.MAX_TEXTS_PER_REQUEST:
            raise ValueError("250 instance(s) is allowed per prediction")
        sent.append(list(texts))
        return [[float(text.split()[0])] for text in texts]

    monkeypatch.setattr(VertexAIEmbeddings, "embed_documents", fake_embed_documents)

    return sent


def test_a_long_document_is_embedded_in_batches_in_order(requests):
    embeddings = server_module.BatchedVertexAIEmbeddings.model_construct()
    texts = [f"{i} chunk" for i in range(2065)]

    vectors = embeddings.embed_documents(texts)

    assert vectors == [[float(i)] for i in range(2065)]
    assert len(requests) == 9
    assert all(
        len(batch) <= vertex_batching.MAX_TEXTS_PER_REQUEST for batch in requests
    )


def test_the_vertex_model_is_built_batched(monkeypatch):
    # no credentials file written by the test
    monkeypatch.setattr(
        server_module, "save_google_application_credentials", lambda credentials: None
    )
    monkeypatch.setattr(
        server_module.BatchedVertexAIEmbeddings,
        "__init__",
        lambda self, **kwargs: None,
    )

    embeddings = server_module.initialize_embedding_model(
        {
            "model_type": "chat_vertex_ai",
            "model": "text-embedding-005",
            "chat_vertex_ai_model_garden": {
                "credentials": {"quota_project_id": "project"}
            },
        }
    )

    assert isinstance(embeddings, server_module.BatchedVertexAIEmbeddings)
