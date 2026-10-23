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

"""What OpenSearchRetriever asks the searcher and OpenSearch.

The retriever hands its configuration to the gRPC query parser, then runs the
query it gets back on the returned indices, with the query parameters only for
a HYBRID search. Without a token list of its own it searches the query text as
a single filter token of its retrieve type.
"""

from unittest.mock import MagicMock, patch

from app.rag.retrievers import retriever as retriever_module
from app.rag.retrievers.retriever import OpenSearchRetriever

QUERY = "zephyr"
BODY = b'{"query": {}}'
INDICES = ["tenant-1-index-a", "tenant-1-index-b"]


def _retriever(retrieve_type="TEXT"):
    # every field gets a distinct value so a swapped or dropped one shows up
    return OpenSearchRetriever(
        search_text=QUERY,
        range_values=[0, 5],
        after_key="after-key",
        suggest_keyword="suggest-keyword",
        suggestion_category_id=7,
        tenant_id="tenant-1",
        jwt="a-jwt",
        extra={"extra-key": "extra-value"},
        sort=[{"date": "desc"}],
        sort_after_key="sort-after-key",
        language="it_IT",
        context_window=100_000,
        retrieve_type=retrieve_type,
        opensearch_host="http://opensearch:9200",
        grpc_host="searcher:50051",
    )


def _run(retriever, index_name=INDICES):
    query_data = {
        "query": BODY,
        "index_name": index_name,
        "query_parameters": {"search_pipeline": "a-pipeline"},
    }
    client = MagicMock()
    client.search.return_value = {"hits": {"hits": []}}

    with (
        patch.object(
            retriever_module, "query_parser", return_value=query_data
        ) as mock_query_parser,
        patch.object(
            retriever_module, "get_opensearch_client", return_value=client
        ) as mock_get_client,
    ):
        documents = retriever.invoke(QUERY)

    return documents, mock_query_parser, mock_get_client, client


def test_query_parser_receives_the_retriever_configuration():
    _, mock_query_parser, _, _ = _run(_retriever())

    mock_query_parser.assert_called_once_with(
        search_query=[
            {
                "entityType": "",
                "entityName": "",
                "tokenType": "TEXT",
                "keywordKey": "",
                "values": [QUERY],
                "extra": {},
                "filter": True,
            }
        ],
        range_values=[0, 5],
        after_key="after-key",
        suggest_keyword="suggest-keyword",
        suggestion_category_id=7,
        tenant_id="tenant-1",
        jwt="a-jwt",
        extra={"extra-key": "extra-value"},
        sort=[{"date": "desc"}],
        sort_after_key="sort-after-key",
        language="it_IT",
        grpc_host="searcher:50051",
    )


def test_text_search_runs_the_parsed_query_without_parameters():
    _, _, mock_get_client, client = _run(_retriever("TEXT"))

    mock_get_client.assert_called_once_with("http://opensearch:9200")
    client.search.assert_called_once_with(body=BODY, index=INDICES, params=None)


def test_knn_search_runs_without_parameters():
    # the parameters carry the hybrid search pipeline: a KNN search is
    # vectorial too, but must not get them
    _, _, _, client = _run(_retriever("KNN"))

    client.search.assert_called_once_with(body=BODY, index=INDICES, params=None)


def test_hybrid_search_forwards_the_query_parameters():
    _, _, _, client = _run(_retriever("HYBRID"))

    client.search.assert_called_once_with(
        body=BODY, index=INDICES, params={"search_pipeline": "a-pipeline"}
    )


def test_no_index_skips_the_search():
    documents, _, mock_get_client, client = _run(_retriever(), index_name=[])

    mock_get_client.assert_not_called()
    client.search.assert_not_called()
    assert documents == []
