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
"""Parsing of the inline markers the answer carries.

The markers decide which sources survive: a source the answer never marked
is not shown, and an answer that marks nothing usable falls back to showing
every retrieved source rather than none at all.
"""

from app.rag.agentic_rag import RagGraph

# Two sources, numbered in the order they appear in the context. Numbering is
# per source, not per chunk, so two chunks of doc-1 share the number 1.
ORDINALS = {"doc-1": 1, "doc-2": 2}


def _parse(answer):
    return RagGraph._parse_citations(answer, ORDINALS)


def test_markers_select_the_cited_sources():
    citations, ordinals = _parse("I vetri sono coperti [2].")

    assert citations == {"doc-2": []}
    assert ordinals == {2: "doc-2"}


def test_every_marked_source_is_kept_in_the_order_it_appears():
    citations, ordinals = _parse("Il conducente [1]. I vetri [2].")

    assert list(ordinals) == [1, 2]
    assert citations == {"doc-1": [], "doc-2": []}


def test_a_source_marked_twice_is_kept_once():
    citations, ordinals = _parse("Il conducente [1]. La franchigia [1].")

    assert citations == {"doc-1": []}
    assert ordinals == {1: "doc-1"}


def test_an_answer_with_no_marker_falls_back_to_all_sources():
    # Worse than a short list of sources is an answer with none, so the
    # caller is told to keep everything.
    assert _parse("Non lo so.") == (None, None)


def test_an_invented_number_is_ignored():
    citations, ordinals = _parse("Il conducente [1]. Inventato [9].")

    assert citations == {"doc-1": []}
    assert 9 not in ordinals


def test_only_invented_numbers_fall_back_to_all_sources():
    assert _parse("Tutto inventato [9].") == (None, None)
