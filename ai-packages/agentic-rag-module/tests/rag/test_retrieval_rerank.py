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

"""Reranking of the retrieved documents.

With rerank on, OpenSearchRetriever sends the retrieved documents to the
reranker and returns them in the order it gives back.
"""

from unittest.mock import MagicMock, patch

from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever

QUERY = "zephyr"
RERANKER_URL = "http://reranker:8000/rerank"


def _retriever(rerank):
    return OpenSearchRetriever(
        search_text=QUERY,
        range_values=[0, 5],
        tenant_id="tenant-1",
        context_window=100_000,
        retrieve_type="TEXT",
        rerank=rerank,
        reranker_api_url=RERANKER_URL,
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )


def _run(retriever, reranked_ids):
    client = MagicMock()
    client.search.return_value = {
        "hits": {
            "hits": [
                {
                    "_source": {
                        "contentId": f"doc-{index}",
                        "rawContent": f"content {index}",
                    },
                    "_score": 10 - index,
                }
                for index in range(3)
            ]
        }
    }
    query_data = {"query": b"{}", "index_name": ["test-index"], "query_parameters": {}}
    reranker = MagicMock()
    reranker.return_value.json.return_value = {
        "context": [{"document_id": document_id} for document_id in reranked_ids]
    }

    with patch.object(
        retriever_module, "get_opensearch_client", return_value=client
    ), patch.object(
        retriever_module, "query_parser", return_value=query_data
    ), patch.object(
        retriever_module.requests, "get", reranker
    ):
        documents = retriever.invoke(QUERY)

    return [document.metadata["document_id"] for document in documents], reranker


def test_documents_come_back_in_the_reranker_order():
    document_ids, reranker = _run(_retriever(True), ["doc-2", "doc-0", "doc-1"])

    assert document_ids == ["doc-2", "doc-0", "doc-1"]
    assert reranker.call_args.args == (RERANKER_URL,)
    assert reranker.call_args.kwargs["json"] == {
        "query": QUERY,
        "context": [
            {"document_id": "doc-0", "content": "content 0"},
            {"document_id": "doc-1", "content": "content 1"},
            {"document_id": "doc-2", "content": "content 2"},
        ],
        "limit": 3,
        "threshold": 0,
        "max_length": 512,
    }


def test_reranker_is_not_called_when_rerank_is_off():
    document_ids, reranker = _run(_retriever(False), [])

    reranker.assert_not_called()
    assert document_ids == ["doc-0", "doc-1", "doc-2"]
