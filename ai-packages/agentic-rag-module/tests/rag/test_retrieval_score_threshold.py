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

from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever


def _build_retriever(score_threshold):
    return OpenSearchRetriever(
        search_text="zephyr",
        range_values=[0, 5],
        tenant_id="tenant-1",
        # Large enough that the context window budget never interferes with the
        # score cutoff under test.
        context_window=100_000,
        retrieve_type="TEXT",
        score_threshold=score_threshold,
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )


def _run(retriever, scores):
    """Run the retriever against a mocked OpenSearch returning one hit per
    score, and return the ids of the documents that survived the cutoff."""
    client = MagicMock()
    client.search.return_value = {
        "hits": {
            "hits": [
                {
                    "_source": {
                        "contentId": f"doc-{index}",
                        "rawContent": "some content",
                    },
                    "_score": score,
                }
                for index, score in enumerate(scores)
            ]
        }
    }

    query_data = {
        "query": b"{}",
        "index_name": ["test-index"],
        "query_parameters": {},
    }

    with patch.object(
        retriever_module, "OpenSearch", return_value=client
    ), patch.object(retriever_module, "query_parser", return_value=query_data):
        documents = retriever.invoke("zephyr")

    return [document.metadata["document_id"] for document in documents]


def test_hits_below_relative_cutoff_are_discarded():
    # threshold 0.3 on a top score of 10 -> everything under 3.0 is dropped
    retriever = _build_retriever(0.3)

    document_ids = _run(retriever, [10.0, 5.0, 2.9, 1.0])

    assert document_ids == ["doc-0", "doc-1"]


def test_hit_exactly_on_the_cutoff_is_kept():
    # the cutoff is inclusive: top_score * 0.3 == 3.0 must survive
    retriever = _build_retriever(0.3)

    document_ids = _run(retriever, [10.0, 3.0])

    assert document_ids == ["doc-0", "doc-1"]


def test_no_cutoff_when_threshold_is_absent():
    # no threshold configured -> no relative cut, every hit is kept
    retriever = _build_retriever(None)

    document_ids = _run(retriever, [10.0, 5.0, 0.01])

    assert document_ids == ["doc-0", "doc-1", "doc-2"]


def test_no_cutoff_when_threshold_is_zero():
    # 0 disables the cut rather than dropping everything
    retriever = _build_retriever(0)

    document_ids = _run(retriever, [10.0, 0.0])

    assert document_ids == ["doc-0", "doc-1"]


def test_top_score_is_taken_across_all_hits():
    # A custom sort overrides relevance ordering, so the best score is not
    # necessarily the first hit: with a cutoff of 10.0 * 0.5 only that hit
    # survives. Reading the top score from hits[0] would set the cutoff at 1.0
    # and let everything through.
    retriever = _build_retriever(0.5)

    document_ids = _run(retriever, [2.0, 10.0, 1.0])

    assert document_ids == ["doc-1"]


def test_empty_result_set_is_handled():
    # no hits -> no top score to derive the cutoff from, and no crash
    retriever = _build_retriever(0.3)

    assert _run(retriever, []) == []


def test_null_score_is_treated_as_zero_alongside_scored_hits():
    # A hit without a score cannot be shown to clear the cutoff, so it drops.
    retriever = _build_retriever(0.3)

    document_ids = _run(retriever, [10.0, None])

    assert document_ids == ["doc-0"]
