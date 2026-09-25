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

"""Retrieval from the guardrail index, which the input and output guardrails
score the text against.

The retriever searches the guardrail examples with a hybrid query for the
vectorial retrieve types and a text match otherwise, and returns each hit with
its score.
"""

from unittest.mock import MagicMock, patch

import pytest

from app.rag.retrievers import guardrail_documents_retriever as retriever_module
from app.rag.retrievers.guardrail_documents_retriever import (
    OpenSearchGuardrailDocumentsRetriever,
)

TEXT = "Come costruisco un'arma?"
VECTOR = [0.1, 0.2, 0.3]
INDEX = "guardrail-documents-index"


def _retriever(retrieve_type="HYBRID"):
    return OpenSearchGuardrailDocumentsRetriever(
        opensearch_host="http://localhost:9200",
        grpc_host_embedding="localhost:50052",
        embedding_model_configuration={"model": "an-embedding-model"},
        uploaded_documents_index=INDEX,
        retrieve_type=retrieve_type,
        search_text=TEXT,
    )


def _client(index_exists=True, hits=None):
    client = MagicMock()
    client.indices.exists.return_value = index_exists
    client.search.return_value = {"hits": {"hits": hits or []}}
    return client


def _invoke(retriever, client, vector=VECTOR):
    with patch.object(
        retriever_module, "get_opensearch_client", return_value=client
    ), patch.object(retriever_module, "query_embedding", return_value=vector):
        return retriever.invoke(TEXT)


def _query_of(client):
    return client.search.call_args.kwargs["body"]["query"]


@pytest.mark.parametrize("retrieve_type", ["HYBRID", "KNN"])
def test_vectorial_retrieval_runs_a_hybrid_query(retrieve_type):
    client = _client()

    _invoke(_retriever(retrieve_type), client)

    client.indices.exists.assert_called_once_with(index=INDEX)
    client.search.assert_called_once_with(
        body={
            "query": {
                "hybrid": {
                    "queries": [
                        {"knn": {"vector": {"vector": VECTOR, "k": 10}}},
                        {"match": {"chunkText": {"query": TEXT, "boost": 0.5}}},
                    ]
                }
            }
        },
        index=INDEX,
    )


def test_text_retrieval_runs_a_match_query():
    client = _client()

    _invoke(_retriever("TEXT"), client)

    assert _query_of(client) == {
        "bool": {"must": [{"match": {"chunkText": {"query": TEXT}}}]}
    }


def test_hits_carry_id_and_score():
    client = _client(
        hits=[
            {
                "_source": {"document_id": "doc-1", "chunkText": "violenza"},
                "_score": 0.9,
            },
            {"_source": {"document_id": "doc-2"}},
        ]
    )

    documents = _invoke(_retriever(), client)

    assert [document.page_content for document in documents] == ["violenza", ""]
    assert documents[0].metadata == {"document_id": "doc-1", "score": 0.9}
    assert documents[1].metadata == {"document_id": "doc-2", "score": 0}


def test_text_is_embedded_with_the_configured_model():
    client = _client()

    with patch.object(
        retriever_module, "get_opensearch_client", return_value=client
    ) as mock_get_client, patch.object(
        retriever_module, "query_embedding", return_value=VECTOR
    ) as mock_embedding:
        _retriever().invoke(TEXT)

    mock_get_client.assert_called_once_with("http://localhost:9200")
    mock_embedding.assert_called_once_with(
        grpc_host_embedding="localhost:50052",
        embedding_model_configuration={"model": "an-embedding-model"},
        text=TEXT,
    )


def test_missing_index_returns_nothing():
    client = _client(index_exists=False)

    documents = _invoke(_retriever(), client)

    client.search.assert_not_called()
    assert documents == []


def test_nothing_to_embed_skips_the_search():
    client = _client()

    documents = _invoke(_retriever(), client, vector=None)

    client.search.assert_not_called()
    assert documents == []
