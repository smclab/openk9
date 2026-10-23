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

"""The retrieved documents fit the model context window.

OpenSearchRetriever estimates each document at one token every 3.5 characters
and keeps the hits, in order, while their running total stays under 85% of the
context window.
"""

from unittest.mock import MagicMock, patch

import pytest

from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever


def _run(retrieve_type, content_key, contents, context_window):
    retriever = OpenSearchRetriever(
        search_text="zephyr",
        range_values=[],
        tenant_id="tenant-1",
        context_window=context_window,
        retrieve_type=retrieve_type,
        opensearch_host="http://localhost:9200",
        grpc_host="localhost:50051",
    )
    client = MagicMock()
    client.search.return_value = {
        "hits": {
            "hits": [
                {
                    "_source": {"contentId": f"doc-{index}", content_key: content},
                    "_score": 1.0,
                }
                for index, content in enumerate(contents)
            ]
        }
    }
    query_data = {"query": b"{}", "index_name": ["an-index"], "query_parameters": {}}

    with (
        patch.object(retriever_module, "get_opensearch_client", return_value=client),
        patch.object(retriever_module, "query_parser", return_value=query_data),
    ):
        documents = retriever.invoke("zephyr")

    return [document.metadata["document_id"] for document in documents]


@pytest.mark.parametrize(
    "retrieve_type, content_key",
    [("TEXT", "rawContent"), ("HYBRID", "chunkText")],
    ids=["text", "vectorial"],
)
def test_documents_are_kept_while_the_running_total_fits(retrieve_type, content_key):
    # context window 10 -> budget 8.5 tokens; 14 and 7 characters are 4 and 2
    # tokens, so the running total is 4, 8, 10 and the third hit is left out
    document_ids = _run(
        retrieve_type, content_key, ["a" * 14, "b" * 14, "c" * 7], context_window=10
    )

    assert document_ids == ["doc-0", "doc-1"]
