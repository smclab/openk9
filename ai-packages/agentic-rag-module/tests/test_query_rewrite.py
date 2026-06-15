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


from app.utils.query_rewrite import (
    shares_significant_terms,
    significant_terms,
)

PREVIOUS_QUERY = "Che cos'è la garanzia Infortuni del Conducente?"


def test_significant_terms_keeps_only_content_words():
    terms = significant_terms(PREVIOUS_QUERY)

    assert terms == {"garanzia", "infortuni", "conducente"}


def test_significant_terms_empty_for_blank_text():
    assert significant_terms("") == set()
    assert significant_terms(None) == set()


def test_significant_terms_drops_short_tokens_and_stopwords():
    assert significant_terms("Puoi fornirmi maggiori dettagli?") == set()


def test_rewrite_keeping_entity_shares_terms():
    rewritten = "Puoi spiegarmi meglio la garanzia Infortuni del Conducente?"

    assert shares_significant_terms(rewritten, PREVIOUS_QUERY) is True


def test_rewrite_losing_entity_does_not_share_terms():
    rewritten = "Puoi fornirmi maggiori dettagli?"

    assert shares_significant_terms(rewritten, PREVIOUS_QUERY) is False


def test_shares_terms_true_when_previous_query_has_no_significant_terms():
    assert shares_significant_terms("qualsiasi cosa", "Dimmi di più?") is True


def test_reattach_previous_query_restores_entity_on_lost_rewrite():
    # Mirrors _rewrite_query safety net: when the rewrite drops the entity, the
    # previous query (carrying the entity) is re-attached so retrieval stays on
    # topic.
    rewritten = "Puoi fornirmi maggiori dettagli?"

    if not shares_significant_terms(rewritten, PREVIOUS_QUERY):
        final_query = f"{PREVIOUS_QUERY} {rewritten}".strip()
    else:
        final_query = rewritten

    assert "Infortuni del Conducente" in final_query
    assert significant_terms(PREVIOUS_QUERY) <= significant_terms(final_query)
