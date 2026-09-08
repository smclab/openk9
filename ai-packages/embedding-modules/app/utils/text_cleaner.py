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

import logging
import warnings

import regex
from bs4 import BeautifulSoup, MarkupResemblesLocatorWarning

logger = logging.getLogger(__name__)

# clean_text also runs on query text, which is often a bare URL or a single
# word: bs4 warns on those inputs, and the warning says nothing here.
warnings.filterwarnings("ignore", category=MarkupResemblesLocatorWarning)

# Characters to keep, by Unicode property, mirroring the Java counterpart in
# core/app/tika/.../TextCleaner.java: letters of every script, digits, math
# symbols (+ = | < >), currency (€ $ £ …), every form of dash, and the
# punctuation that carries meaning. Three deviations from the Java list:
# `\p{Pc}`, the underscore, because dropping it would glue snake_case
# identifiers exactly like the missing apostrophe glued words, and
# `\p{Pi}\p{Pf}`, the typographic quotes “ ” « », which the ASCII pair does
# not cover and which the Java list loses as well. The degree sign is listed
# by hand rather than by its category: "360°" and "20°C" change meaning
# without it, while the rest of `\p{So}` is decoration.
UNWANTED_CHARACTERS = regex.compile(
    r"[^\p{L}\p{Nd}\p{Sm}\p{Sc}\p{Pd}\p{Pc}\p{Pi}\p{Pf}\s.,;:?!\"'()/%@&°]"
)

# Summary and figure-list entries docling emits as markdown links with an empty
# target, e.g. "[3.2 Attachments](.)".
DOCLING_SUMMARY_LINK = regex.compile(r"(?m)^[ \t]*[-*]?[ \t]*\[[^\]]*\]\(\.\)[ \t]*$\n?")

# A hyphen splitting one word across two lines, e.g. "assicura- \nzione".
HYPHENATED_LINE_BREAK = regex.compile(r"(\p{L})-[ \t]*\n[ \t]*(\p{L})")

LEADER_DOTS = regex.compile(r"\.{4,}")
HORIZONTAL_WHITESPACE = regex.compile(r"[ \t]+")
EXCESS_BLANK_LINES = regex.compile(r"\n{3,}")


def clean_text(raw_text):
    """
    Cleans the input text by performing several text processing steps.

    This function strips HTML markup and decodes its entities, rejoins words
    hyphenated across lines, replaces table-of-contents leader dots, drops the
    empty-target markdown links docling emits for summaries, and removes
    unwanted characters while preserving the letters of every script, currency
    symbols, apostrophes and URLs. It collapses runs of spaces and tabs but
    keeps newlines, which mark the boundary between list entries and the
    paragraphs the chunkers split on.

    Cleaning must never cost a document: if any step raises, the raw text is
    returned unchanged so it still reaches the index.

    Parameters:
    raw_text (str): The raw input text that needs to be cleaned.

    Returns:
    str: The cleaned text with unwanted elements removed and formatting corrected.
    """
    try:
        # 1. Strip HTML markup and decode its entities. A parser, not a regex:
        # it discards the content of <script>/<style> instead of leaving it as
        # text, and leaves "a < b" alone.
        soup = BeautifulSoup(raw_text, "html.parser")
        for element in soup(["script", "style"]):
            element.decompose()
        # separator: adjacent elements are distinct words, "<p>a</p><p>b</p>"
        # must not become "ab"
        cleaned_text = soup.get_text(" ")

        # 2. Drop the docling summary lines. This has to run HERE, before step
        # 5 strips the square brackets and makes them indistinguishable from
        # prose: on a real corpus they were 33% of the indexed chunks.
        cleaned_text = DOCLING_SUMMARY_LINK.sub("", cleaned_text)

        # 3. Correct word breaks like "assicura- \nzione" -> "assicurazione".
        # Only between letters: "Covid-\n19" keeps its hyphen, which belongs to
        # the term rather than to the line break.
        cleaned_text = HYPHENATED_LINE_BREAK.sub(r"\1\2", cleaned_text)

        # 4. Replace sequences of long dots (more than 3) like "..........."
        # with a space: removing them merged the two sides of a table-of-
        # contents line, turning "Capitolo 1......5" into "Capitolo 15".
        cleaned_text = LEADER_DOTS.sub(" ", cleaned_text)

        # 5. Remove unwanted characters. Every slash survives: the previous
        # rule kept them only between digits, which left every URL path glued
        # into a single untokenizable word.
        cleaned_text = UNWANTED_CHARACTERS.sub("", cleaned_text)

        # 6. Collapse horizontal whitespace but KEEP newlines: they are the
        # boundary between list entries, and collapsing them merged distinct
        # entries into a single line, which moved a qualifier onto the wrong
        # item (e.g. "draft only" ended up applying to the entry above it).
        cleaned_text = HORIZONTAL_WHITESPACE.sub(" ", cleaned_text)
        cleaned_text = EXCESS_BLANK_LINES.sub("\n\n", cleaned_text)

        return cleaned_text.strip()
    except Exception:
        logger.exception("clean_text failed, indexing the raw text unchanged")
        return raw_text
