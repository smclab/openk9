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

"""The filters of a search are tokens added to the query, never replacing it:
the domains of the intent detection (#2137), the datasources of the request
(#1883) and the filters of its search query."""

from unittest.mock import MagicMock

import pytest

from app.models.models import SearchToken
from app.rag.agentic import retrieval

TEXT_TOKEN = {
    "entityType": "",
    "entityName": "",
    "tokenType": "HYBRID",
    "keywordKey": "",
    "values": ["come si installa"],
    "extra": {},
    "filter": True,
}


@pytest.fixture
def query_parser(monkeypatch):
    query_parser = MagicMock(
        return_value={"query": "{}", "index_name": [], "query_parameters": {}}
    )
    monkeypatch.setattr(retrieval, "query_parser", query_parser)
    return query_parser


def _tokens(query_parser, **filters):
    retrieval.retrieve_documents(
        query="come si installa",
        search_text="come si installa",
        retrieve_type="HYBRID",
        tenant_id="tenant-1",
        opensearch_host="http://os",
        grpc_host="ds:9000",
        context_window=4096,
        range_values=[0, 10],
        **filters,
    )
    return query_parser.call_args.kwargs["search_query"]


def test_without_filters_the_query_is_the_text_alone(query_parser):
    assert _tokens(query_parser) == [TEXT_TOKEN]


def test_domains_are_added_after_the_text(query_parser):
    assert _tokens(query_parser, domains=["how-to"]) == [
        TEXT_TOKEN,
        {**TEXT_TOKEN, "tokenType": "TEXT", "keywordKey": "domain", "values": ["how-to"]},
    ]


def test_datasources_are_added_as_strings(query_parser):
    assert _tokens(query_parser, datasource_ids=[3, 7]) == [
        TEXT_TOKEN,
        {**TEXT_TOKEN, "tokenType": "DATASOURCE", "values": ["3", "7"]},
    ]


def test_request_filters_are_added_as_they_are(query_parser):
    request_filter = SearchToken(
        tokenType="DOCTYPE", keywordKey="type", values=["pdf"], filter=True
    )

    tokens = _tokens(query_parser, filter_tokens=[request_filter])

    assert tokens[0] == TEXT_TOKEN
    assert tokens[1]["tokenType"] == "DOCTYPE"
    assert tokens[1]["values"] == ["pdf"]


def test_search_query_of_the_request_replaces_the_text_token(query_parser):
    token = {**TEXT_TOKEN, "tokenType": "TEXT", "values": ["openk9"]}

    assert _tokens(query_parser, search_query=[token]) == [token]
