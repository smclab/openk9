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

from app.utils import text_cleaner
from app.utils.text_cleaner import clean_text


def test_keeps_accented_letters():
    """An ASCII-only keep-list silently mutilated Italian: "entità" lost its
    final vowel, so the indexed text no longer matched what users type."""
    assert clean_text("perché l'attività è già conclusa") == (
        "perché l'attività è già conclusa"
    )


def test_keeps_both_apostrophe_forms():
    """Without the apostrophes in the keep-list, words glue together:
    "dell'istanza" becomes "dellistanza"."""
    assert clean_text("dell'istanza") == "dell'istanza"
    assert clean_text("dell’istanza") == "dell’istanza"


def test_drops_docling_table_of_contents_lines():
    """Docling emits summary and figure-list entries as markdown links with an
    empty target. They carry no information and accounted for a third of the
    indexed chunks on a real corpus."""
    cleaned = clean_text(
        "Introduzione al procedimento\n"
        "[3.2 Allegati](.)\n"
        "- [4 Conclusioni](.)\n"
        "Testo successivo\n"
    )

    assert "Allegati" not in cleaned
    assert "Conclusioni" not in cleaned
    assert "Introduzione al procedimento" in cleaned
    assert "Testo successivo" in cleaned


def test_keeps_link_text_when_the_target_is_real():
    """Only the empty-target form is summary noise; a genuine link still
    carries its label as content."""
    cleaned = clean_text("vedi [la guida](https://example.org/guida) per i dettagli")

    assert "la guida" in cleaned


def test_preserves_newline_between_list_items():
    """Collapsing every whitespace run into a single space merged distinct list
    entries into one line, which moved a qualifier onto the wrong item."""
    cleaned = clean_text("- duplicazione dell'istanza\n- annullamento, solo in bozza")

    assert cleaned.count("\n") == 1


def test_collapses_horizontal_runs_and_excess_blank_lines():
    cleaned = clean_text("prima\t  voce\n\n\n\nseconda voce")

    assert "prima voce" in cleaned
    assert "\n\n\n" not in cleaned


def test_keeps_currency_symbols_and_signs():
    """An ASCII-only keep-list dropped every euro sign, so "140€" became
    indistinguishable from any other number in an insurance corpus."""
    assert clean_text("Costo: 1.500 € (+20%)") == "Costo: 1.500 € (+20%)"


def test_keeps_email_addresses_intact():
    """Without "@" in the keep-list every contact became unreachable:
    "info@axa-mpsdanni.it" was indexed as "infoaxa-mpsdanni.it"."""
    assert clean_text("Scrivi a info@axa-mpsdanni.it") == (
        "Scrivi a info@axa-mpsdanni.it"
    )


def test_keeps_the_slashes_of_a_url():
    """Slashes used to survive only between digits, which fused every URL path
    into one untokenizable word."""
    assert clean_text("Vedi https://login.axa.it/login-pcc") == (
        "Vedi https://login.axa.it/login-pcc"
    )


def test_keeps_non_latin_scripts():
    """CJK, cyrillic, greek and arabic were erased wholesale: a non-latin
    tenant would index empty strings and embed vectors computed on nothing."""
    assert clean_text("Ricerca semantica 语义搜索 семантический поиск") == (
        "Ricerca semantica 语义搜索 семантический поиск"
    )


def test_leader_dots_become_a_space():
    """Removing the dots outright merged the two sides of a table-of-contents
    line, so "Capitolo 1......5" was indexed as "Capitolo 15"."""
    assert clean_text("Capitolo 1......5") == "Capitolo 1 5"


def test_keeps_a_hyphen_that_does_not_split_a_word():
    """The rule fired on any hyphen before a newline, so "Covid-\\n19" lost the
    hyphen that belongs to the term."""
    assert "Covid-" in clean_text("Covid-\n19")


def test_rejoins_a_word_split_across_lines():
    assert clean_text("assicura- \nzione") == "assicurazione"


def test_drops_the_content_of_script_and_style():
    """A regex over tags left the script body behind as text."""
    cleaned = clean_text("<p>testo</p><script>var x = 1;</script>")

    assert "testo" in cleaned
    assert "var x" not in cleaned


def test_decodes_html_entities():
    assert clean_text("Prezzo &lt; 100 &amp; disponibile") == (
        "Prezzo < 100 & disponibile"
    )


def test_keeps_paragraph_boundaries():
    """The structure-aware chunkers split on blank lines: flattening them
    leaves the chunker nothing to work with."""
    cleaned = clean_text("Primo paragrafo.\n\nSecondo paragrafo.")

    assert cleaned == "Primo paragrafo.\n\nSecondo paragrafo."


def test_returns_the_raw_text_when_cleaning_raises(monkeypatch):
    """Losing the document is worse than indexing it uncleaned."""
    def explode(*args, **kwargs):
        raise RuntimeError("parser exploded")

    monkeypatch.setattr(text_cleaner, "BeautifulSoup", explode)

    assert clean_text("testo <b>grezzo</b>") == "testo <b>grezzo</b>"
