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
