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

"""The search payload is validated before it reaches the pipeline.

A query needs its tokens and its text; everything else has a default the
pipeline relies on. A token needs its type and values, and an inline media
both its data and content type: format and size are left to the datasource.
"""

import pytest
from pydantic import ValidationError

from app.models.models import SearchQuery, SearchToken


def _errors(model, payload):
    with pytest.raises(ValidationError) as raised:
        model.model_validate(payload)
    return {error["loc"] for error in raised.value.errors()}


def test_minimal_query_gets_the_pipeline_defaults():
    query = SearchQuery.model_validate({"searchQuery": [], "searchText": "OpenK9?"})

    assert query.searchText == "OpenK9?"
    assert query.reformulate is True
    assert query.extra == {}
    assert query.datasourceIds is None
    assert query.language is None


def test_query_without_tokens_or_text_is_refused():
    assert _errors(SearchQuery, {}) == {("searchQuery",), ("searchText",)}


def test_datasource_ids_must_be_integers():
    query = SearchQuery.model_validate(
        {"searchQuery": [], "searchText": "q", "datasourceIds": [1, "2"]}
    )
    assert query.datasourceIds == [1, 2]

    assert _errors(
        SearchQuery, {"searchQuery": [], "searchText": "q", "datasourceIds": ["uno"]}
    ) == {("datasourceIds", 0)}


def test_minimal_token_gets_its_defaults():
    token = SearchToken.model_validate({"tokenType": "TEXT", "values": ["gatto"]})

    assert token.keywordKey == ""
    assert token.filter is False
    assert token.entityType == ""
    assert token.entityName == ""
    assert token.extra == {}
    assert token.media is None


def test_token_without_type_or_values_is_refused():
    assert _errors(SearchToken, {}) == {("tokenType",), ("values",)}


def test_token_values_must_be_a_list():
    assert _errors(SearchToken, {"tokenType": "TEXT", "values": "gatto"}) == {
        ("values",)
    }


def test_media_needs_data_and_content_type():
    token = SearchToken.model_validate(
        {
            "tokenType": "KNN",
            "values": [],
            "media": {"data": "iVBORw0KGgo=", "contentType": "image/png"},
        }
    )
    assert token.media.contentType == "image/png"

    assert _errors(
        SearchToken, {"tokenType": "KNN", "values": [], "media": {"data": "x"}}
    ) == {("media", "contentType")}


def test_tokens_inside_a_query_are_validated():
    assert _errors(
        SearchQuery, {"searchQuery": [{"tokenType": "TEXT"}], "searchText": "q"}
    ) == {("searchQuery", 0, "values")}
