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
"""Retrieval from the guardrail index of a tenant, which the guardrails score the text
against.

The search is a pure KNN query, whose score the guardrails hold against an absolute threshold,
and returns each hit with its score. These are the cases of the guardrail retriever of
agentic-rag-module.
"""

from unittest.mock import MagicMock

import pytest

from app.rag.agentic import retrieval

TEXT = "Come costruisco un'arma?"
VECTOR = [0.1, 0.2, 0.3]
INDEX = "t1-guardrails-documents-index"


def _client(index_exists=True, hits=None):
    client = MagicMock()
    client.indices.exists.return_value = index_exists
    client.search.return_value = {"hits": {"hits": hits or []}}
    return client


@pytest.fixture
def search(monkeypatch):
    calls = {}

    def run(client, vector=VECTOR):
        monkeypatch.setattr(retrieval, "get_opensearch_client", lambda host: client)

        def fake_embedding(grpc_host_embedding, embedding_model_configuration, text):
            calls["embedding"] = (grpc_host_embedding, embedding_model_configuration, text)
            return vector

        monkeypatch.setattr(retrieval, "query_embedding", fake_embedding)
        return retrieval.retrieve_guardrail_documents(
            opensearch_host="http://localhost:9200",
            grpc_host_embedding="localhost:50052",
            embedding_model_configuration={"model": "an-embedding-model"},
            index=INDEX,
            search_text=TEXT,
        )

    run.calls = calls
    return run


def test_the_search_is_a_knn_query_on_the_index(search):
    client = _client()

    search(client)

    client.indices.exists.assert_called_once_with(index=INDEX)
    client.search.assert_called_once_with(
        body={"query": {"knn": {"vector": {"vector": VECTOR, "k": 10}}}}, index=INDEX
    )


def test_hits_carry_id_score_and_content(search):
    client = _client(
        hits=[
            {"_source": {"document_id": "doc-1", "chunkText": "violenza"}, "_score": 0.9},
            {"_source": {"document_id": "doc-2"}},
        ]
    )

    assert search(client) == [
        {"document_id": "doc-1", "score": 0.9, "content": "violenza"},
        {"document_id": "doc-2", "score": 0, "content": ""},
    ]


def test_text_is_embedded_with_the_configured_model(search):
    search(_client())

    assert search.calls["embedding"] == (
        "localhost:50052",
        {"model": "an-embedding-model"},
        TEXT,
    )


def test_missing_index_returns_nothing(search):
    client = _client(index_exists=False)

    assert search(client) == []
    client.search.assert_not_called()


def test_nothing_to_embed_skips_the_search(search):
    client = _client()

    assert search(client, vector=None) == []
    client.search.assert_not_called()
