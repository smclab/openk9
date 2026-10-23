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

from unittest.mock import MagicMock, patch

import pytest

from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever


def _build_retriever(retrieve_type="TEXT"):
    return OpenSearchRetriever(
        search_text="zephyr",
        range_values=[0, 5],
        tenant_id="tenant-1",
        context_window=100_000,
        retrieve_type=retrieve_type,
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )


def _run(scores):
    """Run the retriever against a mocked OpenSearch returning one hit per
    score, and return the ids of the documents it produced."""
    documents = _retrieve(
        [
            {
                "_source": {
                    "contentId": f"doc-{index}",
                    "rawContent": "some content",
                },
                "_score": score,
            }
            for index, score in enumerate(scores)
        ]
    )

    return [document.metadata["document_id"] for document in documents]


def _retrieve(hits, retrieve_type="TEXT"):
    client = MagicMock()
    client.search.return_value = {"hits": {"hits": hits}}

    query_data = {
        "query": b"{}",
        "index_name": ["test-index"],
        "query_parameters": {},
    }

    with patch.object(
        retriever_module, "get_opensearch_client", return_value=client
    ), patch.object(retriever_module, "query_parser", return_value=query_data):
        return _build_retriever(retrieve_type).invoke("zephyr")


def test_null_scores_do_not_break_retrieval():
    # OpenSearch skips relevance and reports "_score": null on every hit when
    # the query carries a field sort. Reading it with a .get default is not
    # enough: the default only covers a missing key, not an explicit null, so
    # the score comparison used to raise TypeError and fail the whole request.
    assert _run([None, None]) == ["doc-0", "doc-1"]


def test_null_score_mixed_with_numeric_scores():
    # A single unscored hit among scored ones must not break the batch either.
    assert _run([10.0, None, 5.0]) == ["doc-0", "doc-1", "doc-2"]


def test_null_score_is_reported_as_zero():
    documents = _retrieve(
        [{"_source": {"contentId": "doc-0", "chunkText": "chunk"}, "_score": None}],
        retrieve_type="HYBRID",
    )

    assert documents[0].metadata["score"] == 0


@pytest.mark.parametrize("retrieve_type", ["TEXT", "HYBRID"])
def test_hit_without_content_becomes_an_empty_document(retrieve_type):
    # neither rawContent nor chunkText: the hit is still returned, empty
    documents = _retrieve(
        [{"_source": {"contentId": "doc-0"}, "_score": 1.0}],
        retrieve_type=retrieve_type,
    )

    assert [document.page_content for document in documents] == [""]
