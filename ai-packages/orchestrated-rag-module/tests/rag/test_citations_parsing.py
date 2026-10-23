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


"""Parsing of the inline markers and of the quotes tail that may follow them.

The markers decide which sources survive; the quotes only add evidence to
them. The two are parsed by one call so that a tail which is missing or
malformed costs the evidence and never the attribution.
"""

from app.rag.agentic_rag import RagGraph

# Three chunks over two sources: doc-1 is numbered 1 and holds two chunks,
# doc-2 is numbered 2. Numbering is per source, not per chunk.
ORDINALS = {"doc-1": 1, "doc-2": 2}
SOURCE_TEXT = {
    "1": (
        "La garanzia Infortuni del Conducente copre il conducente.\n"
        "Il secondo chunk dello stesso documento parla di franchigia."
    ),
    "2": "La garanzia Cristalli copre la rottura dei vetri.",
}


def _parse(answer, tail=""):
    return RagGraph._parse_citations(answer, ORDINALS, tail, SOURCE_TEXT)


# --- markers alone: the attribution ----------------------------------------


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


# --- quotes: the evidence --------------------------------------------------


def test_quotes_are_attached_to_the_source_that_was_marked():
    citations, _ = _parse(
        "I vetri sono coperti [2].", '{"2": ["copre la rottura dei vetri"]}'
    )

    assert citations == {"doc-2": ["copre la rottura dei vetri"]}


def test_quotes_are_matched_across_every_chunk_of_the_source():
    # The quote comes from doc-1's second chunk; both chunks are source 1.
    citations, _ = _parse("La franchigia [1].", '{"1": ["parla di franchigia"]}')

    assert citations == {"doc-1": ["parla di franchigia"]}


def test_a_quote_absent_from_its_source_is_dropped_but_the_source_stays():
    citations, _ = _parse("I vetri [2].", '{"2": ["una frase inventata"]}')

    assert citations == {"doc-2": []}


def test_quotes_are_matched_ignoring_case_punctuation_and_whitespace():
    citations, _ = _parse("I vetri [2].", '{"2": ["COPRE,  la   ROTTURA"]}')

    assert citations == {"doc-2": ["COPRE,  la   ROTTURA"]}


def test_quotes_are_capped_per_source():
    tail = '{"1": ["copre il conducente", "parla di franchigia", "La garanzia"]}'
    citations, _ = _parse("Il conducente [1].", tail)

    assert len(citations["doc-1"]) == 2


def test_a_markdown_fence_around_the_json_is_tolerated():
    tail = '```json\n{"2": ["copre la rottura dei vetri"]}\n```'
    citations, _ = _parse("I vetri [2].", tail)

    assert citations == {"doc-2": ["copre la rottura dei vetri"]}


def test_quotes_for_a_source_that_was_never_marked_are_dropped():
    # The two mechanisms disagree about which sources were used: the markers
    # win, because they are what the reader sees next to the sentence.
    citations, ordinals = _parse(
        "I vetri [2].", '{"1": ["copre il conducente"], "2": ["rottura dei vetri"]}'
    )

    assert citations == {"doc-2": ["rottura dei vetri"]}
    assert "doc-1" not in citations


# --- a bad tail costs the evidence, never the attribution ------------------


def test_a_malformed_tail_keeps_the_markers_without_quotes():
    citations, ordinals = _parse("Il conducente [1]. I vetri [2].", '{"1": [,,,')

    assert citations == {"doc-1": [], "doc-2": []}
    assert ordinals == {1: "doc-1", 2: "doc-2"}


def test_a_tail_that_is_not_an_object_keeps_the_markers():
    citations, _ = _parse("I vetri [2].", '["copre la rottura"]')

    assert citations == {"doc-2": []}


def test_no_tail_at_all_keeps_the_markers():
    citations, ordinals = _parse("I vetri [2].")

    assert citations == {"doc-2": []}
    assert ordinals == {2: "doc-2"}


def test_non_numeric_quote_keys_are_ignored():
    citations, _ = _parse(
        "I vetri [2].", '{"doc-2": ["rottura"], "2": ["rottura dei vetri"]}'
    )

    assert citations == {"doc-2": ["rottura dei vetri"]}


def test_non_string_quotes_are_ignored():
    citations, _ = _parse("I vetri [2].", '{"2": [null, 7, "rottura dei vetri"]}')

    assert citations == {"doc-2": ["rottura dei vetri"]}


def test_a_single_quote_not_in_a_list_is_accepted():
    citations, _ = _parse("I vetri [2].", '{"2": "copre la rottura dei vetri"}')

    assert citations == {"doc-2": ["copre la rottura dei vetri"]}
