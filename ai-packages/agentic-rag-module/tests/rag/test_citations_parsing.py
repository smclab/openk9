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


from langchain_core.documents import Document

from app.rag.agentic_rag import RagGraph

CONTEXT = [
    Document(
        page_content="La garanzia Infortuni del Conducente copre il conducente.",
        metadata={"document_id": "doc-1"},
    ),
    Document(
        page_content="Il secondo chunk dello stesso documento parla di franchigia.",
        metadata={"document_id": "doc-1"},
    ),
    Document(
        page_content="La garanzia Cristalli copre la rottura dei vetri.",
        metadata={"document_id": "doc-2"},
    ),
]


def _parse(tail):
    return RagGraph._parse_citations(tail, CONTEXT)


def test_well_formed_block_maps_quotes_by_document():
    tail = """
    [{"document_id": "doc-2", "quotes": ["copre la rottura dei vetri"]}]
    """

    assert _parse(tail) == {"doc-2": ["copre la rottura dei vetri"]}


def test_quotes_are_matched_across_all_chunks_of_the_document():
    # doc-1 was retrieved as two chunks: a quote taken from either one counts.
    tail = '[{"document_id": "doc-1", "quotes": ["parla di franchigia"]}]'

    assert _parse(tail) == {"doc-1": ["parla di franchigia"]}


def test_json_wrapped_in_a_markdown_fence_is_parsed():
    tail = """```json
    [{"document_id": "doc-1", "quotes": ["copre il conducente"]}]
    ```"""

    assert _parse(tail) == {"doc-1": ["copre il conducente"]}


def test_missing_block_falls_back_to_all_sources():
    assert _parse("") is None
    assert _parse("   \n") is None


def test_malformed_json_falls_back_to_all_sources():
    assert _parse('[{"document_id": "doc-1", "quotes": ["troncato"') is None
    assert _parse("Non ho citazioni da darti.") is None
    assert _parse('{"document_id": "doc-1"}') is None


def test_empty_array_falls_back_to_all_sources():
    # A well-formed "I used nothing" is treated like a failed attribution: the
    # user is never left without sources for an answer built on the context.
    assert _parse("[]") is None


def test_numeric_document_ids_are_matched():
    # Observed live: with numeric ids the model sometimes emits them unquoted,
    # so json.loads yields an int. Comparing it to the str keys of the retrieved
    # documents used to drop every citation and fall back to all sources.
    documents = [
        Document(
            page_content="Dichiarazioni dallamministratore delegato di SMC.",
            metadata={"document_id": "9460035979983399"},
        )
    ]
    tail = """[{"document_id": 9460035979983399,
                "quotes": ["dallamministratore delegato di SMC"]}]"""

    assert RagGraph._parse_citations(tail, documents) == {
        "9460035979983399": ["dallamministratore delegato di SMC"]
    }


def test_unknown_document_ids_are_dropped():
    tail = '[{"document_id": "doc-99", "quotes": ["inventata"]}]'

    # Nothing is attributed to a retrieved document, so all sources are kept.
    assert _parse(tail) is None


def test_ungrounded_quotes_are_dropped_but_the_source_is_kept():
    tail = """[{"document_id": "doc-2",
                "quotes": ["riassunto mio", "copre la rottura dei vetri"]}]"""

    assert _parse(tail) == {"doc-2": ["copre la rottura dei vetri"]}


def test_source_cited_with_no_grounded_quote_keeps_an_empty_quote_list():
    tail = '[{"document_id": "doc-1", "quotes": ["parafrasi inventata"]}]'

    assert _parse(tail) == {"doc-1": []}


def test_quotes_are_matched_against_ascii_stripped_sources():
    # Real corpora reach the context with their non-ASCII characters removed:
    # "l'eccellenza" is stored as "leccellenza" and "SMC è stata" as
    # "SMC stata". A correctly spelled quote must still ground against them.
    documents = [
        Document(
            page_content="Il premio riconosce leccellenza di SMC: SMC stata "
            "confermata come miglior partner, gi da oltre 40 anni.",
            metadata={"document_id": "doc-1"},
        )
    ]
    tail = """[{"document_id": "doc-1", "quotes": [
        "Il premio riconosce l'eccellenza di SMC",
        "SMC è stata confermata come miglior partner"
    ]}]"""

    assert RagGraph._parse_citations(tail, documents) == {
        "doc-1": [
            "Il premio riconosce l'eccellenza di SMC",
            "SMC è stata confermata come miglior partner",
        ]
    }


def test_quotes_are_matched_ignoring_whitespace_and_case():
    tail = """[{"document_id": "doc-1",
                "quotes": ["Garanzia  Infortuni\\ndel   conducente"]}]"""

    assert _parse(tail) == {
        "doc-1": ["Garanzia  Infortuni\ndel   conducente"],
    }


def test_quotes_are_capped_per_source():
    tail = """[{"document_id": "doc-1", "quotes": [
        "La garanzia Infortuni",
        "copre il conducente",
        "parla di franchigia"
    ]}]"""

    assert _parse(tail) == {
        "doc-1": ["La garanzia Infortuni", "copre il conducente"],
    }


def test_repeated_document_ids_are_merged():
    tail = """[
        {"document_id": "doc-1", "quotes": ["La garanzia Infortuni"]},
        {"document_id": "doc-1", "quotes": ["parla di franchigia"]}
    ]"""

    assert _parse(tail) == {
        "doc-1": ["La garanzia Infortuni", "parla di franchigia"],
    }


def test_non_object_entries_are_ignored():
    tail = '["doc-1", {"document_id": "doc-1", "quotes": ["copre il conducente"]}]'

    assert _parse(tail) == {"doc-1": ["copre il conducente"]}


def test_non_string_quotes_are_ignored():
    tail = '[{"document_id": "doc-1", "quotes": [42, null, "copre il conducente"]}]'

    assert _parse(tail) == {"doc-1": ["copre il conducente"]}
