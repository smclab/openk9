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


from unittest.mock import patch

from app.models import models
from app.rag.retrievers.retriever import OpenSearchRetriever

QUERY = "qual e' la policy ferie?"

BASE = dict(
    search_text=QUERY,
    range_values=[],
    tenant_id="t1",
    context_window=4096,
    retrieve_type="HYBRID",
    opensearch_host="http://os:9200",
    grpc_host="grpc:9000",
)


def _captured_search_query(search_query):
    """Run _get_relevant_documents and return the search_query list that the
    retriever passes to query_parser. An empty index_name makes the retriever
    skip the OpenSearch call, so no cluster is needed."""
    fake = {"query": b"{}", "index_name": set(), "query_parameters": {}}
    with patch(
        "app.rag.retrievers.retriever.query_parser", return_value=fake
    ) as query_parser:
        retriever = OpenSearchRetriever(search_query=search_query, **BASE)
        retriever._get_relevant_documents(QUERY, run_manager=None)

    return query_parser.call_args.kwargs["search_query"]


def _is_query_token(token):
    return (
        token["tokenType"] == "HYBRID"
        and token["values"] == [QUERY]
        and token["keywordKey"] == ""
        and token["filter"] is True
    )


def test_domain_detected_query_token_plus_domain_filter():
    """Query con dominio rilevato -> body con clausola query + filtro domain."""
    domain_filter = models.SearchToken(
        tokenType="TEXT", keywordKey="domain", values=["hr"], filter=True
    )

    search_query = _captured_search_query([domain_filter])

    assert len(search_query) == 2
    assert _is_query_token(search_query[0])
    assert search_query[1]["keywordKey"] == "domain"
    assert search_query[1]["values"] == ["hr"]


def test_no_domain_only_query_token():
    """Query senza dominio -> comportamento invariato (solo token query)."""
    search_query = _captured_search_query(None)

    assert len(search_query) == 1
    assert _is_query_token(search_query[0])


def test_config_search_query_query_token_plus_config_filters():
    """search_query di config non vuota -> token query + filtri di config."""
    config_filter = models.SearchToken(
        tokenType="TEXT", keywordKey="category", values=["faq"]
    )

    search_query = _captured_search_query([config_filter])

    assert len(search_query) == 2
    assert _is_query_token(search_query[0])
    assert search_query[1]["keywordKey"] == "category"
    assert search_query[1]["values"] == ["faq"]
